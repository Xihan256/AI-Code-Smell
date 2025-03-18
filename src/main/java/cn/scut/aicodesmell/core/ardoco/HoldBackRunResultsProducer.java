package cn.scut.aicodesmell.core.ardoco;

import cn.scut.aicodesmell.core.ardoco.baseline.InconsistencyBaseline;
import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.api.data.PreprocessingData;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelInstance;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.model.ModelProvider;
import edu.kit.kastel.mcse.ardoco.core.model.PcmXMLModelConnector;
import edu.kit.kastel.mcse.ardoco.core.pipeline.ArDoCo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author wanghy
 */
public class HoldBackRunResultsProducer {
    private File inputText;
    private File inputModel;
    private PcmXMLModelConnector pcmModel;

    public HoldBackRunResultsProducer(File inputText, File inputModel) {
        this.inputText = inputText;
        this.inputModel = inputModel;
    }

    public Map<ModelInstance, ArDoCoResult> produceHoldBackRunResults(boolean useBaselineApproach) {
        Map<ModelInstance, ArDoCoResult> runs = new HashMap<ModelInstance, ArDoCoResult>();

        var holdElementsBackModelConnector = constructHoldElementsBackModelConnector();

        ArDoCo arDoCoBaseRun;
        try {
            arDoCoBaseRun = definePipelineBase(inputText, holdElementsBackModelConnector, useBaselineApproach);
        } catch (IOException e) {
            //todo 异常处理 获取失败的订单并设状态fail
            return runs;
        }
        arDoCoBaseRun.run();
        var baseRunData = new ArDoCoResult(arDoCoBaseRun.getDataRepository());
        runs.put(null, baseRunData);

        for (int i = 0; i < holdElementsBackModelConnector.numberOfActualInstances(); i++) {
            holdElementsBackModelConnector.setCurrentHoldBackIndex(i);
            var currentHoldBack = holdElementsBackModelConnector.getCurrentHoldBack();
            var currentRun = defineArDoCoWithPreComputedData(baseRunData, holdElementsBackModelConnector, useBaselineApproach);
            currentRun.run();
            runs.put(currentHoldBack, new ArDoCoResult(currentRun.getDataRepository()));
        }
        return runs;
    }

    private HoldElementsBackModelConnector constructHoldElementsBackModelConnector() {
        try {
            pcmModel = new PcmXMLModelConnector(inputModel);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new HoldElementsBackModelConnector(pcmModel);
    }

    private static ArDoCo definePipelineBase(File inputText, HoldElementsBackModelConnector holdElementsBackModelConnector,
                                             boolean useInconsistencyBaseline) throws FileNotFoundException {
        ArDoCo arDoCo = new ArDoCo(inputText.getName().substring(0, inputText.getName().lastIndexOf('.')).toLowerCase());
        DataRepository dataRepository = arDoCo.getDataRepository();
        //感觉应该用不上这个additionalConfigs
        //var additionalConfigs = project.getAdditionalConfigurations();
        Map<String, String> additionalConfigs = new HashMap<>();

        arDoCo.addPipelineStep(ArDoCo.getTextProvider(inputText, additionalConfigs, dataRepository));

        addMiddleSteps(holdElementsBackModelConnector, arDoCo, dataRepository, additionalConfigs);

        if (useInconsistencyBaseline) {
            arDoCo.addPipelineStep(new InconsistencyBaseline(dataRepository));
        } else {
            arDoCo.addPipelineStep(ArDoCo.getInconsistencyChecker(additionalConfigs, dataRepository));
        }

        return arDoCo;
    }

    private static void addMiddleSteps(HoldElementsBackModelConnector holdElementsBackModelConnector, ArDoCo arDoCo, DataRepository dataRepository,
                                       Map<String, String> additionalConfigs) {
        arDoCo.addPipelineStep(new ModelProvider(dataRepository, holdElementsBackModelConnector));
        arDoCo.addPipelineStep(ArDoCo.getTextExtraction(additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(ArDoCo.getRecommendationGenerator(additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(ArDoCo.getConnectionGenerator(additionalConfigs, dataRepository));
    }

    private static ArDoCo defineArDoCoWithPreComputedData(ArDoCoResult precomputedResults, HoldElementsBackModelConnector holdElementsBackModelConnector,
                                                          boolean useInconsistencyBaseline) {
        var projectName = precomputedResults.getProjectName();
        ArDoCo arDoCo = new ArDoCo(projectName);
        var dataRepository = arDoCo.getDataRepository();

        var additionalConfigs = ArDoCo.loadAdditionalConfigs(null);
        //这个情况貌似并不会出现
//        var optionalProject = Project.getFromName(projectName);
//        if (optionalProject.isPresent()) {
//            additionalConfigs = optionalProject.get().getAdditionalConfigurations();
//        }

        var preprocessingData = new PreprocessingData(precomputedResults.getText());
        dataRepository.addData(PreprocessingData.ID, preprocessingData);

        addMiddleSteps(holdElementsBackModelConnector, arDoCo, dataRepository, additionalConfigs);

        if (useInconsistencyBaseline) {
            arDoCo.addPipelineStep(new InconsistencyBaseline(dataRepository));
        } else {
            arDoCo.addPipelineStep(ArDoCo.getInconsistencyChecker(additionalConfigs, dataRepository));
        }
        return arDoCo;
    }
}
