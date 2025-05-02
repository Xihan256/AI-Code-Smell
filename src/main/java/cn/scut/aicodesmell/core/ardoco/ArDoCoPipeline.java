package cn.scut.aicodesmell.core.ardoco;

import cn.scut.aicodesmell.core.ardoco.task.ITaskHandler;
import cn.scut.aicodesmell.core.ardoco.task.TaskContext;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wanghy
 * 使用ArDoCo的工作流程:
 * 1 获取输入code和doc(这一步实际上不用干, 当输入参数了)
 * 2 code转成pcm doc转成text(pcm是默认的, 还有一个uml, 但我没用过)
 * 3 使用ArDoCo算法跑, 并获取输出结果
 * 4 把ArDoCo算法的输出结果做成pdf之类的形式
 */
public class ArDoCoPipeline {
    private List<ITaskHandler> tasks;

    protected ArDoCoPipeline() {
        this.tasks = new ArrayList<>();
    }

    public ArDoCoPipeline addTask(ITaskHandler handler) {
        tasks.add(handler);
        return this;
    }

    public void execute(TaskContext taskContext) {
        tasks.forEach(t -> t.handle(taskContext));
    }
}
