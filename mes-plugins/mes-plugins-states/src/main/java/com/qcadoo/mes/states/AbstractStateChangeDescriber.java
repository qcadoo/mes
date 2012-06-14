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
    public String getStatusFieldName() {
        return "status";
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

    @Override
    public String getDateTimeFieldName() {
        return "dateAndTime";
    }

    @Override
    public String getShiftFieldName() {
        return "shift";
    }

    @Override
    public String getWorkerFieldName() {
        return "worker";
    }

    @Override
    public void checkFields() {
        DataDefinition dataDefinition = getDataDefinition();
        List<String> fieldNames = Lists.newArrayList(getOwnerFieldName(), getSourceStateFieldName(), getTargetStateFieldName(),
                getStatusFieldName(), getMessagesFieldName(), getPhaseFieldName(), getDateTimeFieldName(), getShiftFieldName(),
                getWorkerFieldName());
        Set<String> uniqueFieldNames = Sets.newHashSet(fieldNames);
        checkState(fieldNames.size() == uniqueFieldNames.size(), "Describer methods should return unique field names.");

        Set<String> existingFieldNames = dataDefinition.getFields().keySet();
        checkState(existingFieldNames.containsAll(uniqueFieldNames), "DataDefinition for " + dataDefinition.getPluginIdentifier()
                + '.' + dataDefinition.getName() + " does not have all fields with name specified by this desciber.");
    }

}
