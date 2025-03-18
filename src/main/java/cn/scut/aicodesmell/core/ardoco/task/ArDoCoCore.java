package cn.scut.aicodesmell.core.ardoco.task;

import cn.scut.aicodesmell.core.ardoco.HoldBackRunResultsProducer;
import cn.scut.aicodesmell.exception.CoreTaskException;
import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.data.PipelineStepData;
import edu.kit.kastel.mcse.ardoco.core.api.data.connectiongenerator.InstanceLink;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelExtractionState;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelInstance;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.common.util.FilePrinter;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.ConnectionStateImpl;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.ConnectionStatesImpl;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.RecommendationStateImpl;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.RecommendationStatesImpl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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

        File result = saveOutput(projectId, recommendationStates, modelStatesData, connectionStates);
        context.setArdocoResult(result);
    }

    private File saveOutput(String projectId, RecommendationStatesImpl recommendationStates,
                            ModelStates modelStatesData, ConnectionStatesImpl connectionStates) {
        Path outputDir = Path.of(OUTPUT);
        String fileName = projectId + ".txt";
        File outputFile = outputDir.resolve(fileName).toFile();

        try (BufferedWriter writer = Files.newBufferedWriter(outputDir.resolve(fileName), StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
           // writer.write("Files.newBufferedWriter Example\n");
            if(Objects.nonNull(recommendationStates)){
                writer.write("recommendation states\n");
                RecommendationStateImpl code = recommendationStates.getRecommendationState(Metamodel.CODE);
                RecommendationStateImpl architecture = recommendationStates.getRecommendationState(Metamodel.ARCHITECTURE);
                writer.write("Code:\n");
                writer.write("recommended instances:\n");
                for (RecommendedInstance recommendedInstance : code.getRecommendedInstances()) {
                    writer.write(recommendedInstance.toString());
                }
                if(!code.getLastAppliedConfiguration().isEmpty()){
                    writer.write("last applied configuration:\n");
                    writer.write(code.getLastAppliedConfiguration().toString());
                }

                writer.write("Architecture:\n");
                writer.write("recommended instances:\n");
                for (RecommendedInstance recommendedInstance : architecture.getRecommendedInstances()) {
                    writer.write(recommendedInstance.toString());
                }
                if(!architecture.getLastAppliedConfiguration().isEmpty()){
                    writer.write("last applied configuration:\n");
                    writer.write(code.getLastAppliedConfiguration().toString());
                }
            }
            if(Objects.nonNull(modelStatesData)){
                writer.write("\nmodel states\n");
                for (String modelId : modelStatesData.modelIds()) {
                    ModelExtractionState modelState = modelStatesData.getModelState(modelId);
                    writer.write("model state {" + modelId + "}\n");
                    writer.write(modelState.toString() + "\n");
                }
            }
            if(Objects.nonNull(connectionStates)){
                writer.write("\nconnection states\n");
                ConnectionStateImpl code = connectionStates.getConnectionState(Metamodel.CODE);
                ConnectionStateImpl architecture = connectionStates.getConnectionState(Metamodel.ARCHITECTURE);

                writer.write("Code:\n");
                writer.write("instance links:\n");
                for (InstanceLink instanceLink : code.getInstanceLinks()) {
                    writer.write(instanceLink.toString() + "\n");
                }
                if(!code.getLastAppliedConfiguration().isEmpty()){
                    writer.write("last applied configuration:\n");
                    writer.write(code.getLastAppliedConfiguration().toString());
                }

                writer.write("Architecture:\n");
                writer.write("instance links:\n");
                for (InstanceLink instanceLink : architecture.getInstanceLinks()) {
                    writer.write(instanceLink.toString() + "\n");
                }
                if(!architecture.getLastAppliedConfiguration().isEmpty()){
                    writer.write("last applied configuration:\n");
                    writer.write(code.getLastAppliedConfiguration().toString());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new CoreTaskException(projectId);
        }
        return outputFile;
    }

    private static void saveOutput(String projectId, ArDoCoResult arDoCoResult) {
        Objects.requireNonNull(projectId);
        Objects.requireNonNull(arDoCoResult);

        String projectName = projectId.toLowerCase();
        var outputDir = Path.of(OUTPUT);
        var filename = projectName + ".txt";

        var outputFileTLR = outputDir.resolve("traceLinks_" + filename).toFile();
        FilePrinter.writeTraceabilityLinkRecoveryOutput(outputFileTLR, arDoCoResult);
        var outputFileID = outputDir.resolve("inconsistencyDetection_" + filename).toFile();
        FilePrinter.writeInconsistencyOutput(outputFileID, arDoCoResult);
    }

}
