package cn.scut.aicodesmell.core.ardoco.task;

import cn.scut.aicodesmell.core.ardoco.HoldBackRunResultsProducer;
import cn.scut.aicodesmell.exception.CoreTaskException;
import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.data.PipelineStepData;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelInstance;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.ConnectionStatesImpl;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.RecommendationStatesImpl;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @author wanghy
 */
public class ArDoCoCore implements ITaskHandler {
    private static final String OUTPUT = System.getProperty("user.dir") + "/data/files/download/";

    /**
     * 这是整个流水线的最核心部分, 我们在这里读取pcm和txt, 然后交给ArDoCo处理, 最后获得两个txt的输出结果
     *
     * @param context TaskContext
     */
    @Override
    public void handle(TaskContext context) {
        File pcm = context.getPcm();
        File txt = context.getTxt();
        String projectId = context.getProjectId();

        HoldBackRunResultsProducer holdBackRunResultsProducer = new HoldBackRunResultsProducer(txt, pcm);
        Map<ModelInstance, ArDoCoResult> runs = holdBackRunResultsProducer.produceHoldBackRunResults(false);
        //需要的是这个null关联的内容
        ArDoCoResult baseArDoCoResult = runs.get(null);
        DataRepository dataRepository = baseArDoCoResult.dataRepository();

        //通过反射强行获取, 跳过它设定的获取方法
        HashMap<String, PipelineStepData> data;
        try {
            Field dataField = dataRepository.getClass().getDeclaredField("data");
            dataField.setAccessible(true);
            data = (HashMap<String, PipelineStepData>) dataField.get(dataRepository);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new CoreTaskException(projectId);
        }
        RecommendationStatesImpl recommendationStates = (RecommendationStatesImpl) data.get("RecommendationStates");
        ModelStates modelStatesData = (ModelStates) data.get("ModelStatesData");
        ConnectionStatesImpl connectionStates = (ConnectionStatesImpl) data.get("ConnectionStates");

        context.setRecommendationStates(recommendationStates);
        context.setModelStatesData(modelStatesData);
        context.setConnectionStates(connectionStates);
    }

}
