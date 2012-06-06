package com.qcadoo.mes.states;

import static com.google.common.base.Preconditions.checkState;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.qcadoo.model.api.DataDefinition;

/**
 * This abstract class provides some default values to make concrete Describers more compact.
 * 
 * @since 1.1.7
 */
public abstract class AbstractStateChangeDescriber implements StateChangeEntityDescriber {

    @Override
    public String getSourceStateFieldName() {
        return "sourceState";
    }

    @Override
    public String getTargetStateFieldName() {
        return "targetState";
    }

    @Override
    public String getFinishedFieldName() {
        return "finished";
    }

    @Override
    public String getMessagesFieldName() {
        return "messages";
    }

    @Override
    public String getOwnerFieldName() {
        return "owner";
    }

    @Override
    public String getPhaseFieldName() {
        return "phase";
    }

    /**
     * Check if any field using in this describer is missing.
     * 
     * @throws IllegalStateException
     *             if at least one field is missing.
     */
    public void checkFields() throws IllegalStateException {
        DataDefinition dataDefinition = getDataDefinition();
        List<String> fieldNames = Lists.newArrayList(getOwnerFieldName(), getSourceStateFieldName(), getTargetStateFieldName(),
                getFinishedFieldName(), getMessagesFieldName(), getPhaseFieldName());
        Set<String> uniqueFieldNames = Sets.newHashSet(fieldNames);
        checkState(fieldNames.size() == uniqueFieldNames.size(), "Describer methods should return unique field names.");

        Set<String> existingFieldNames = dataDefinition.getFields().keySet();
        checkState(existingFieldNames.containsAll(uniqueFieldNames), "DataDefinition for " + dataDefinition.getPluginIdentifier()
                + '.' + dataDefinition.getName() + " does not have all fields with name specified by this desciber.");
    }

}
