package cn.scut.aicodesmell.core.ardoco.task;

/**
 * @author wanghy
 */
public interface ITaskHandler {
    /**
     * 处理任务的接口
     *
     * @param context TaskContext
     */
    void handle(TaskContext context);
}
