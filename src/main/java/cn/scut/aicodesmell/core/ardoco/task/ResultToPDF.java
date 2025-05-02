package cn.scut.aicodesmell.core.ardoco.task;

import cn.scut.aicodesmell.exception.CoreTaskException;
import edu.kit.kastel.mcse.ardoco.core.api.data.connectiongenerator.InstanceLink;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelExtractionState;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.ConnectionStateImpl;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.ConnectionStatesImpl;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.RecommendationStateImpl;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.RecommendationStatesImpl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

/**
 * @author wanghy
 */
public class ResultToPDF implements ITaskHandler {

    private String downloadPath = System.getProperty("user.dir") + File.separator + "data/files/download/";

    @Override
    public void handle(TaskContext context) {
        RecommendationStatesImpl recommendationStates = context.getRecommendationStates();
        ModelStates modelStatesData = context.getModelStatesData();
        ConnectionStatesImpl connectionStates = context.getConnectionStates();

        String path = downloadPath + context.getProjectId() + ".pdf";
        File finalResult = new File(path);
        try {
            finalResult.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        context.setFinalResult(finalResult);
    }
}
