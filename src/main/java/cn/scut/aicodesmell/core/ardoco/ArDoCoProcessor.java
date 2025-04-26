package cn.scut.aicodesmell.core.ardoco;

import cn.scut.aicodesmell.common.MatchComponentEntity;
import cn.scut.aicodesmell.common.SentencePosition;
import cn.scut.aicodesmell.config.CacheConfig;
import cn.scut.aicodesmell.core.Processor;
import cn.scut.aicodesmell.core.ardoco.task.*;
import cn.scut.aicodesmell.mapper.ComponentDocPhrasesMapper;
import cn.scut.aicodesmell.mapper.OrderDetailMapper;
import cn.scut.aicodesmell.mapper.OrderMapper;
import com.alibaba.fastjson2.JSON;
import edu.kit.kastel.mcse.ardoco.core.api.data.connectiongenerator.InstanceLink;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.RecommendedInstance;
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
                //得到割好的句子, 和文档得到的组件构建关系
                for (RecommendedInstance recommendedInstance : rsArchitecture.getRecommendedInstances()) {
                    ImmutableList<NounMapping> typeMappings = recommendedInstance.getTypeMappings();
                    for (NounMapping nounMapping : typeMappings) {
                        double confidence = nounMapping.getDistribution().get(MappingKind.TYPE).getConfidence();
                        //设定可信度标准, 避免垃圾数据太多
                        if (confidence >= 0.7) {
                            componentsInDocument.add(recommendedInstance.getName());
                            List<String> phrasesMatching = new ArrayList<>();
                            nounMapping.getWords().forEach(word -> {
                                int position = word.getPosition();
                                // treeMap快速查找
                                Map.Entry<Integer, SentencePosition> entry = positions.floorEntry(position);
                                if (entry != null && position < entry.getValue().getEnd()) {
                                    System.out.println("Found: " + entry.getValue());
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
                            });
                            //添加到数据库
                            componentDocPhrasesMapper.batchAdd(context.getProjectId(), recommendedInstance.getName(), phrasesMatching);
                        }
                    }
                }
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
        };

        //提交到线程池
        processOrderThreadPool.submit(task);
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
