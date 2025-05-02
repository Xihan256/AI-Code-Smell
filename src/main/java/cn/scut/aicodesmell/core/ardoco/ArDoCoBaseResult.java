package cn.scut.aicodesmell.core.ardoco;

import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.ConnectionStatesImpl;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.RecommendationStatesImpl;
import lombok.*;

/**
 * @author wanghy
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ArDoCoBaseResult {
    private RecommendationStatesImpl recommendationStates;
    private ModelStates ModelStatesData;
    private ConnectionStatesImpl ConnectionStates;
}
