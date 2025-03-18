package cn.scut.aicodesmell.core.ardoco;

import cn.scut.aicodesmell.core.Processor;
import cn.scut.aicodesmell.core.ardoco.task.*;
import cn.scut.aicodesmell.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.io.File;
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

    @Override
    public void generateResult(String docUrl, String codeUrl) {
        Runnable task = () -> {
            String projectId = docUrl.substring(0, docUrl.lastIndexOf('.'));
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

            arDoCoPipeline.execute(context);

            File finalResult = context.getFinalResult();
            if (Objects.isNull(finalResult) || !finalResult.exists()) {
                //失败了
                orderMapper.setStatusByProjectId(projectId, "failed");
                return;
            }
            String resultUrl = finalResult.getName();
            orderMapper.setResultUrl(projectId, resultUrl);
        };

        //提交到线程池
        processOrderThreadPool.submit(task);
    }

//    public static void main(String[] args) {
//        String docUrl = "jabref.doc";
//        String codeUrl = "jabref.repository";
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
