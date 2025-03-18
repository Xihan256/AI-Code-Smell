package cn.scut.aicodesmell.core.ardoco.task;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.File;

/**
 * @author wanghy
 */
@Getter
@Setter
@NoArgsConstructor
public class TaskContext {
    private String projectId;

    /*
    步骤1 原始的输入
     */
    private File doc;
    private File code;

    /*
    步骤2 code转成pcm doc转成text的结果
     */
    private File pcm;
    private File txt;

    /*
    步骤3 使用ArDoCo算法跑的结果
     */
    private File ardocoResult;

    /*
    步骤4 把ArDoCo算法的输出结果转成pdf之类的形式的最终File
     */
    private File finalResult;

    public TaskContext(File doc, File code, String projectId) {
        this.doc = doc;
        this.code = code;
        this.projectId = projectId;
    }
}
