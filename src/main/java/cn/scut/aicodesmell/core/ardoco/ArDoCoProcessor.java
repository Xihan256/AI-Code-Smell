package cn.scut.aicodesmell.core.ardoco;

import cn.scut.aicodesmell.common.MatchComponentEntity;
import cn.scut.aicodesmell.config.CacheConfig;
import cn.scut.aicodesmell.core.Processor;
import cn.scut.aicodesmell.core.ardoco.task.*;
import cn.scut.aicodesmell.mapper.OrderDetailMapper;
import cn.scut.aicodesmell.mapper.OrderMapper;
import com.alibaba.fastjson2.JSON;
import edu.kit.kastel.mcse.ardoco.core.api.data.connectiongenerator.InstanceLink;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.ConnectionStateImpl;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.RecommendationStateImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author wanghy
 */
@Component("ArDoCo")
@Slf4j
public class ArDoCoProcessor implements Processor {

    @Autowired
    @Qualifier("processOrderThreadPool")
    private ThreadPoolTaskExecutor processOrderThreadPool;

    @Value("${file-save.upload}")
    private String uploadFilePath;

    @Value("${file-save.download}")
    private String downloadFilePath;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private CacheConfig cacheConfig;

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
            List<String> componentsInDocument = new ArrayList<>();
            for (RecommendedInstance recommendedInstance : rsArchitecture.getRecommendedInstances()) {
                componentsInDocument.add(recommendedInstance.getName());
            }

            List<MatchComponentEntity> matchComponents = new ArrayList<>();
            ConnectionStateImpl cSArchitecture = context.getConnectionStates().getConnectionState(Metamodel.ARCHITECTURE);
            for (InstanceLink instanceLink : cSArchitecture.getInstanceLinks()) {
                //code组件的名称
                String name = instanceLink.getModelInstance().getFullName();
                double probability = instanceLink.getProbability();
                String codeComponent = context.getCodeComponent2CodePackageMap().get(name);
                matchComponents.add(new MatchComponentEntity(name, probability, codeComponent));
            }

            //存上
            String jsonComponentsInDocument = JSON.toJSONString(componentsInDocument);
            orderMapper.setResult(projectId, resultUrl, jsonComponentsInDocument, timeCost);
            orderDetailMapper.batchAdd(projectId, matchComponents);
        };

        //提交到线程池
        processOrderThreadPool.submit(task);
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
