package cn.scut.aicodesmell.core.ardoco;

import edu.kit.kastel.mcse.ardoco.core.api.data.model.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelConnector;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelInstance;

/**
 * @author wanghy
 */
public class HoldElementsBackModelConnector implements ModelConnector {

    private final ModelConnector actualModelConnector;
    private int currentHoldBackIndex = -1;

    /**
     * Constructor that uses the provided {@link ModelConnector} as underlying connector.
     *
     * @param actualModelConnector the connector that is used for actually retrieving elements
     */
    public HoldElementsBackModelConnector(ModelConnector actualModelConnector) {
        this.actualModelConnector = actualModelConnector;
    }

    @Override
    public String getModelId() {
        return actualModelConnector.getModelId();
    }

    @Override
    public Metamodel getMetamodel() {
        return actualModelConnector.getMetamodel();
    }

    @Override
    public org.eclipse.collections.api.list.ImmutableList<ModelInstance> getInstances() {
        var actualInstances = actualModelConnector.getInstances();
        if (currentHoldBackIndex < 0) {
            return actualInstances;
        }
        return actualInstances.newWithout(actualInstances.get(currentHoldBackIndex));
    }

    /**
     * Set the index of the element that should be hold back. Set the index to <0 if nothing should be held back.
     *
     * @param currentHoldBackIndex the index of the element to be hold back. If negative, nothing is held back
     */
    public void setCurrentHoldBackIndex(int currentHoldBackIndex) {
        this.currentHoldBackIndex = currentHoldBackIndex;
    }

    /**
     * @return the ModelInstance that is held back. If nothing is held back, returns null
     */
    public ModelInstance getCurrentHoldBack() {
        if (currentHoldBackIndex < 0) {
            return null;
        }
        return actualModelConnector.getInstances().get(currentHoldBackIndex);
    }

    /**
     * @return the number of actual instances (including all held back elements)
     */
    public int numberOfActualInstances() {
        return actualModelConnector.getInstances().size();
    }
}
