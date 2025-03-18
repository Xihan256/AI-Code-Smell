package cn.scut.aicodesmell.core.ardoco.baseline;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.api.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.api.agent.PipelineAgent;

import java.util.List;

/**
 * @author wanghy
 */
public class InconsistencyBaselineAgent extends PipelineAgent {

    protected InconsistencyBaselineAgent(DataRepository dataRepository) {
        super(InconsistencyBaselineAgent.class.getSimpleName(), dataRepository);
    }

    @Override
    protected List<Informant> getEnabledPipelineSteps() {
        return List.of(new InconsistencyBaselineInformant(getDataRepository()));
    }
}