package cn.scut.aicodesmell.core.ardoco;

import cn.scut.aicodesmell.common.*;
import cn.scut.aicodesmell.common.dto.OrderDetailDto;
import cn.scut.aicodesmell.common.response.Result;
import cn.scut.aicodesmell.common.response.Results;
import cn.scut.aicodesmell.config.CacheConfig;
import cn.scut.aicodesmell.core.Processor;
import cn.scut.aicodesmell.core.ardoco.task.*;
import cn.scut.aicodesmell.mapper.ComponentDocPhrasesMapper;
import cn.scut.aicodesmell.mapper.OrderDetailMapper;
import cn.scut.aicodesmell.mapper.OrderMapper;
import cn.scut.aicodesmell.service.ProcessOrderService;
import com.alibaba.fastjson2.JSON;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Link;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import edu.kit.kastel.mcse.ardoco.core.api.data.connectiongenerator.InstanceLink;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Word;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.ConnectionStateImpl;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.RecommendationStateImpl;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.collections.api.list.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author wanghy
 */
@Component("ArDoCo")
@Slf4j
public class ArDoCoProcessor implements Processor {

    @Autowired
    @Qualifier("processOrderThreadPool")
    private ThreadPoolTaskExecutor processOrderThreadPool;

    @Autowired
    @Qualifier("TaskPostProcessPool")
    private ThreadPoolTaskExecutor TaskPostProcessPool;

    @Value("${file-save.upload}")
    private String uploadFilePath;

    @Value("${file-save.download}")
    private String downloadFilePath;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private ComponentDocPhrasesMapper componentDocPhrasesMapper;

    @Autowired
    private CacheConfig cacheConfig;

    /*
        割词的时候避免把缩写当一句割开了
     */
    private static final Set<String> COMMON_ABBREVIATIONS = Set.of(
            "Mr.", "Mrs.", "Ms.", "Dr.", "Prof.", "Sr.", "Jr.",
            "e.g.", "i.e.", "etc.", "vs.", "Fig.", "U.S.", "Inc.",
            "xml, e.", "logic, ui.", "ui.", "xml, e. g."
    );

    @Override
    public void generateResult(String docUrl, String codeUrl) {
        Runnable task = () -> {
            String projectId = docUrl.substring(0, docUrl.lastIndexOf('.'));
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            //使用ArDoCo管道运行
            ArDoCoPipeline arDoCoPipeline = new ArDoCoPipeline();
            arDoCoPipeline.addTask(new CodeToPCM())
                    .addTask(new DocToText())
                    .addTask(new ArDoCoCore())
                    .addTask(new ResultToPDF());

            String cwd = System.getProperty("user.dir");
            String uploadPath = cwd + "/" + uploadFilePath;
            File doc = new File(uploadPath + docUrl);
            File code = new File(uploadPath + codeUrl);
            TaskContext context = new TaskContext(doc, code, projectId);
            context.setMainPackage(cacheConfig.getMainPackageCache(projectId));

            arDoCoPipeline.execute(context);

            File finalResult = context.getFinalResult();
            if (Objects.isNull(finalResult) || !finalResult.exists()) {
                //失败了
                orderMapper.setStatusByProjectId(projectId, "failed");
                return;
            }
            String resultUrl = finalResult.getName();

            //数据扫尾处理
            stopWatch.stop();
            long timeCost = stopWatch.getTotalTimeMillis();
            RecommendationStateImpl rsArchitecture = context.getRecommendationStates().getRecommendationState(Metamodel.ARCHITECTURE);
            //文档组件
            Set<String> componentsInDocument = new HashSet<>();
            //异步执行文本后处理任务
            CompletableFuture<Void> postProcessTask = CompletableFuture.runAsync(() -> {
                String text = null;
                try {
                    text = Files.readString(context.getTxt().toPath());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if (Objects.isNull(text)) {
                    return;
                }
                TreeMap<Integer, SentencePosition> positions = splitIntoSentences(text);
                if (!System.getProperty("os.name").startsWith("Windows")) {
                    //排除一个bug, 非windows系统割词会出问题
                    return;
                }
                //得到割好的句子, 和文档得到的组件构建关系
                for (RecommendedInstance recommendedInstance : rsArchitecture.getRecommendedInstances()) {
                    ImmutableList<NounMapping> nameMappings = recommendedInstance.getNameMappings();
                    List<String> phrasesMatching = new ArrayList<>();
                    for (NounMapping nounMapping : nameMappings) {
                        double confidence = nounMapping.getDistribution().get(MappingKind.TYPE).getConfidence();
                        if (confidence < 0.3) {
                            continue;
                        }
                        //设定可信度标准, 避免垃圾数据太多
                        componentsInDocument.add(recommendedInstance.getName());
                        for (Word word : nounMapping.getWords()) {
                            int position = word.getPosition();
                            // treeMap快速查找
                            Map.Entry<Integer, SentencePosition> entry = positions.floorEntry(position);
                            if (entry != null && position < entry.getValue().getEnd()) {
                                //去重防止干扰
                                if (!phrasesMatching.isEmpty()) {
                                    String lastMatchingSentence = phrasesMatching.get(phrasesMatching.size() - 1);
                                    if (!lastMatchingSentence.equals(entry.getValue().getSentence())) {
                                        phrasesMatching.add(entry.getValue().getSentence());
                                    }
                                } else {
                                    phrasesMatching.add(entry.getValue().getSentence());
                                }
                            }
                        }
                    }
                    //添加到数据库
                    log.info("projectId={}, component={}, phrasesMatching={}", context.getProjectId(), recommendedInstance.getName(), phrasesMatching);
                    if (!phrasesMatching.isEmpty()) {
                        componentDocPhrasesMapper.batchAdd(context.getProjectId(), recommendedInstance.getName(), phrasesMatching);
                    }
                }

                log.info("拆分句子完毕, orderId: {}", projectId);
            }, TaskPostProcessPool);

            List<MatchComponentEntity> matchComponents = new ArrayList<>();
            ConnectionStateImpl cSArchitecture = context.getConnectionStates().getConnectionState(Metamodel.ARCHITECTURE);
            for (InstanceLink instanceLink : cSArchitecture.getInstanceLinks()) {
                //code组件的名称
                String name = instanceLink.getModelInstance().getFullName();
                double probability = instanceLink.getProbability();
                String codeComponent = context.getCodeComponent2CodePackageMap().get(name);
                matchComponents.add(new MatchComponentEntity(name, probability, codeComponent));
            }

            //存上, 在这里需要先join, 否则jsonComponentsInDocument出不来
            postProcessTask.join();
            String jsonComponentsInDocument = JSON.toJSONString(componentsInDocument);
            orderMapper.setResult(projectId, resultUrl, jsonComponentsInDocument, timeCost);
            orderDetailMapper.batchAdd(projectId, matchComponents);
            doPDFOutPut(finalResult, context);
            log.info("task finished, orderId: {}", projectId);
        };

        //提交到线程池
        processOrderThreadPool.submit(task);
    }

     void doPDFOutPut(File pdf, TaskContext context) {
        try {
            PdfWriter writer = new PdfWriter(pdf);
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);
            String path = "C:/WINDOWS/Fonts/simkai.ttf";//windows里的字体资源路径
            // 标题字体
            var boldFont = PdfFontFactory.createFont(path);

            // 添加标题
            document.add(new Paragraph("基本信息")
                    .setFont(boldFont)
                    .setFontSize(16)
                    .setTextAlignment(TextAlignment.CENTER));

            Result orderDetail = getOrderDetail(context.getProjectId());
            OrderDetailedDto data = (OrderDetailedDto) orderDetail.getData();
            List<MatchEntity> documentComponent = data.getDocumentComponent();
            int cnt = 0;
            int doclen = documentComponent.size();
            for (MatchEntity entity : documentComponent) {
                if (entity.getCodeComponent() != null) {
                    cnt++;
                }
            }
            // 保留三位小数的字符串表示
            double ratiol = (double) cnt / doclen;
            String formattedRatiol = String.format("%.3f", ratiol);

            Result orderCodeComponents = getOrderCodeComponents(context.getProjectId());
            List<OrderDetailDto> codeComponents = (List<OrderDetailDto>) orderCodeComponents.getData();

            // 基本信息表格
            Table baseInfoTable = new Table(UnitValue.createPercentArray(new float[]{1, 2, 1, 2}))
                    .useAllAvailableWidth();

            baseInfoTable.addCell(new Cell().setFont(boldFont).add(new Paragraph("项目名称")));
            baseInfoTable.addCell(new Cell().setFont(boldFont).add(new Paragraph(data.getOrderName())));
            baseInfoTable.addCell(new Cell().setFont(boldFont).add(new Paragraph("项目文档")));
            baseInfoTable.addCell(new Cell().setFont(boldFont).add(new Paragraph(data.getOrderName() + ".doc")));
            baseInfoTable.addCell(new Cell().setFont(boldFont).add(new Paragraph("项目代码")));
            baseInfoTable.addCell(new Cell().setFont(boldFont).add(new Paragraph(data.getOrderName() + ".zip")));
            baseInfoTable.addCell(new Cell().setFont(boldFont).add(new Paragraph("项目结果")));
            baseInfoTable.addCell(new Cell().setFont(boldFont).add(new Paragraph(data.getOrderName() + ".pdf")));
            document.add(baseInfoTable);

            // 空行
            document.add(new Paragraph("\n"));

            // 检测结果
            document.add(new Paragraph("检测结果")
                    .setFont(boldFont)
                    .setFontSize(16)
                    .setTextAlignment(TextAlignment.CENTER));

            Table resultTable = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1, 1, 1, 1}))
                    .useAllAvailableWidth();

            resultTable.addCell(new Cell().setFont(boldFont).add(new Paragraph("检测时间")));
            resultTable.addCell(new Cell().setFont(boldFont).add(new Paragraph("检测用时")));
            resultTable.addCell(new Cell().setFont(boldFont).add(new Paragraph("文档中组件数")));
            resultTable.addCell(new Cell().setFont(boldFont).add(new Paragraph("代码中组件数")));
            resultTable.addCell(new Cell().setFont(boldFont).add(new Paragraph("匹配数")));
            resultTable.addCell(new Cell().setFont(boldFont).add(new Paragraph("匹配率")));

            resultTable.addCell(new Cell().setFont(boldFont).add(new Paragraph(data.getCreateTime().toString())));
            resultTable.addCell(new Cell().setFont(boldFont).add(new Paragraph(Double.valueOf((data.getTimeCost() / 1000)).toString())));
            resultTable.addCell(new Cell().setFont(boldFont).add(new Paragraph(String.valueOf(documentComponent.size()))));
            resultTable.addCell(new Cell().setFont(boldFont).add(new Paragraph(String.valueOf(codeComponents.size()))));
            resultTable.addCell(new Cell().setFont(boldFont).add(new Paragraph(String.valueOf(cnt))));
            resultTable.addCell(new Cell().setFont(boldFont).add(new Paragraph(formattedRatiol)));
            document.add(resultTable);

            // 空行
            document.add(new Paragraph("\n"));

            // 文档组件列表
            document.add(new Paragraph("文档组件列表")
                    .setFont(boldFont)
                    .setFontSize(16)
                    .setTextAlignment(TextAlignment.CENTER));

            Table docComponentTable = new Table(UnitValue.createPercentArray(new float[]{1, 3, 3}))
                    .useAllAvailableWidth();
            docComponentTable.addCell(new Cell().setFont(boldFont).add(new Paragraph("序号")));
            docComponentTable.addCell(new Cell().setFont(boldFont).add(new Paragraph("文档中的组件")));
            docComponentTable.addCell(new Cell().setFont(boldFont).add(new Paragraph("相关原句")));
            for (int i = 0; i < documentComponent.size(); i++) {
                docComponentTable.addCell(new Cell().setFont(boldFont).add(new Paragraph(String.valueOf(i + 1))));
                docComponentTable.addCell(new Cell().setFont(boldFont).add(new Paragraph(documentComponent.get(i).getDocComponent())));
                StringBuilder sentenceBuilder = new StringBuilder();
                List<String> byOrderIdAndComponentName = componentDocPhrasesMapper.getByOrderIdAndComponentName(context.getProjectId(), documentComponent.get(i).getDocComponent());
                for(String sentence : byOrderIdAndComponentName){
                    sentenceBuilder.append(sentence);
                    sentenceBuilder.append('\n');
                }
                docComponentTable.addCell(new Cell().setFont(boldFont).add(new Paragraph(sentenceBuilder.toString())));
            }
            document.add(docComponentTable);

            // 空行
            document.add(new Paragraph("\n"));

            // 代码组件列表
            document.add(new Paragraph("代码组件列表")
                    .setFont(boldFont)
                    .setFontSize(16)
                    .setTextAlignment(TextAlignment.CENTER));

            Table codeComponentTable = new Table(UnitValue.createPercentArray(new float[]{1, 3, 3}))
                    .useAllAvailableWidth();
            codeComponentTable.addCell(new Cell().setFont(boldFont).add(new Paragraph("序号")));
            codeComponentTable.addCell(new Cell().setFont(boldFont).add(new Paragraph("组件名")));
            codeComponentTable.addCell(new Cell().setFont(boldFont).add(new Paragraph("代码包")));
            for (int i = 0; i < codeComponents.size(); i++) {
                codeComponentTable.addCell(new Cell().setFont(boldFont).add(new Paragraph(String.valueOf(i + 1))));
                codeComponentTable.addCell(new Cell().setFont(boldFont).add(new Paragraph(codeComponents.get(i).getComponentName())));
                codeComponentTable.addCell(new Cell().setFont(boldFont).add(new Paragraph(codeComponents.get(i).getCodeComponent())));
            }
            document.add(codeComponentTable);

            // 空行
            document.add(new Paragraph("\n"));

            // 匹配情况
            document.add(new Paragraph("匹配情况")
                    .setFont(boldFont)
                    .setFontSize(16)
                    .setTextAlignment(TextAlignment.CENTER));

            Table matchTable = new Table(UnitValue.createPercentArray(new float[]{1, 3, 2, 3, 2}))
                    .useAllAvailableWidth();
            matchTable.addCell(new Cell().setFont(boldFont).add(new Paragraph("序号")));
            matchTable.addCell(new Cell().setFont(boldFont).add(new Paragraph("文档中的组件")));
            matchTable.addCell(new Cell().setFont(boldFont).add(new Paragraph("是否匹配到")));
            matchTable.addCell(new Cell().setFont(boldFont).add(new Paragraph("对应代码项")));
            matchTable.addCell(new Cell().setFont(boldFont).add(new Paragraph("匹配结果")));
            for (int i = 0; i < documentComponent.size(); i++) {
                matchTable.addCell(new Cell().setFont(boldFont).add(new Paragraph(String.valueOf(i + 1))));
                matchTable.addCell(new Cell().setFont(boldFont).add(new Paragraph(documentComponent.get(i).getDocComponent())));
                boolean matched = !(documentComponent.get(i).getCodeComponent() == null);
                matchTable.addCell(new Cell().setFont(boldFont).add(new Paragraph(matched ? "是" : "否")));
                matchTable.addCell(new Cell().setFont(boldFont).add(new Paragraph(documentComponent.get(i).getCodeComponent() == null ? " " : documentComponent.get(i).getCodeComponent())));
                matchTable.addCell(new Cell().setFont(boldFont).add(new Paragraph(documentComponent.get(i).getProbability() == null ? " " : documentComponent.get(i).getProbability().toString())));
            }
            document.add(matchTable);

            document.close();
            System.out.println("PDF created: " + pdf);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static TreeMap<Integer, SentencePosition> splitIntoSentences(String text) {
        List<SentencePosition> result = new ArrayList<>();
        Pattern pattern = Pattern.compile("[^.!?\\n]+[.!?]+[\"')\\]]*|[^.!?\\n]+$");
        Matcher matcher = pattern.matcher(text);

        int lastEnd = 0;

        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            String sentence = text.substring(start, end).trim();

            // 检查是否是缩写结尾而非真正的句子终止
            boolean isAbbreviation = false;
            for (String abbr : COMMON_ABBREVIATIONS) {
                if (sentence.endsWith(abbr)) {
                    isAbbreviation = true;
                    break;
                }
            }

            if (isAbbreviation && matcher.find()) {
                // 合并当前句和下一个句
                String nextPart = text.substring(matcher.start(), matcher.end()).trim();
                sentence += " " + nextPart;
                end = matcher.end(); // 更新终止位置
            }

            result.add(new SentencePosition(sentence, start, end));
            lastEnd = end;
        }
        //必须建立treeMap索引, 二分查找太慢了
        //二分是O(nlogn), treeMap大约在O(logn)
        TreeMap<Integer, SentencePosition> treeMap = new TreeMap<>();
        for (SentencePosition sentencePosition : result) {
            treeMap.put(sentencePosition.getStart(), sentencePosition);
        }
        return treeMap;
    }

    public Result getOrderDetail(String orderId) {
        OrderEntity entity = orderMapper.getOrderById(orderId);
        if (Objects.isNull(entity) || !"finished".equals(entity.getOrderStatus())) {
            throw new RuntimeException("order不存在");
        }
        OrderDetailedDto orderDetailedDto = new OrderDetailedDto();
        orderDetailedDto.setOrderId(entity.getOrderId());
        orderDetailedDto.setOrderName(entity.getOrderName());
        orderDetailedDto.setUserId(entity.getUserId());
        orderDetailedDto.setCreateTime(entity.getCreateTime());
        orderDetailedDto.setDocUrl(entity.getDocUrl());
        orderDetailedDto.setCodeUrl(entity.getCodeUrl());
        orderDetailedDto.setResultUrl(entity.getResultUrl());
        orderDetailedDto.setTimeCost(entity.getTimeCost() / 1000.0);
        String documentComponent = entity.getDocumentComponent();
        Object documentComponentObj = JSON.parse(documentComponent);
        List<MatchEntity> matchEntities = new ArrayList<>();
        orderDetailedDto.setDocumentComponent(matchEntities);
        if (documentComponentObj instanceof List<?>) {
            List<String> documentComponentList = (List<String>) documentComponentObj;
            for (String component : documentComponentList) {
                MatchEntity matchEntity = new MatchEntity();
                matchEntity.setDocComponent(component);
                List<OrderDetailDto> detailDtos = orderDetailMapper.getByComponentName(component, orderId);
                OrderDetailDto detailDto = detailDtos.isEmpty() ? new OrderDetailDto() : detailDtos.get(0);
                if (Objects.nonNull(detailDto)) {
                    matchEntity.setProbability(detailDto.getProbability());
                    matchEntity.setCodeComponent(detailDto.getCodeComponent());
                }
//                //聚合一下, 找三个句子塞里面
//                List<String> byOrderIdLim3 = componentDocPhrasesMapper.getByOrderIdLim3(orderId, component);
//                matchEntity.setDocComponentSentences(byOrderIdLim3);
                matchEntities.add(matchEntity);
            }
        }
        return Results.ok(orderDetailedDto);
    }

    public Result getOrderCodeComponents(String orderId) {
        List<OrderDetailDto> byOrderId = orderDetailMapper.getByOrderId(orderId);
        return Results.ok(byOrderId);
    }

//    public static void main(String[] args) {
//        String docUrl = "teammates.doc";
//        String codeUrl = "teammates.zip";
//
//        String projectId = docUrl.substring(0, docUrl.lastIndexOf('.'));
//        //使用ArDoCo管道运行
//        ArDoCoPipeline arDoCoPipeline = new ArDoCoPipeline();
//        arDoCoPipeline.addTask(new CodeToPCM())
//                .addTask(new DocToText())
//                .addTask(new ArDoCoCore())
//                .addTask(new ResultToPDF());
//
//        String cwd = System.getProperty("user.dir");
//        String uploadPath = cwd + "/data/files/upload/";
//        File doc = new File(uploadPath + docUrl);
//        File code = new File(uploadPath + codeUrl);
//        TaskContext context = new TaskContext(doc, code, projectId);
//        context.setMainPackage("teammates");
//
//        arDoCoPipeline.execute(context);
//
//        File finalResult = context.getFinalResult();
//        if (Objects.isNull(finalResult) || !finalResult.exists()) {
//            //失败了
//            System.out.println("failed");
//            return;
//        }
//        String resultUrl = finalResult.getName();
//        System.out.println("success");
//    }
}
