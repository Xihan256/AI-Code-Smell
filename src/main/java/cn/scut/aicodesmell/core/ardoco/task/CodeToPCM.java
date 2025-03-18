package cn.scut.aicodesmell.core.ardoco.task;

import java.io.File;

/**
 * @author wanghy
 */
public class CodeToPCM implements ITaskHandler {

    private String uploadPath = System.getProperty("user.dir") + "/" + "data/files/upload/";

    @Override
    public void handle(TaskContext context) {
        //todo 这是mock, 后面会把这个填上
        File code = context.getCode();
        File pcm = new File(uploadPath + context.getProjectId() + ".repository");
        context.setPcm(pcm);
    }
}
