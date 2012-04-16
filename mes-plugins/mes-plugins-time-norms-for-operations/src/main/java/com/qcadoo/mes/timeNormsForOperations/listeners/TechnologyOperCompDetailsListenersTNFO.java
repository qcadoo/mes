package com.qcadoo.mes.timeNormsForOperations.listeners;

import static com.google.common.base.Preconditions.checkArgument;
import static com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields.OPERATION;
import static com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperCompTNFOFields.COUNT_MACHINE;
import static com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperCompTNFOFields.COUNT_REALIZED;
import static com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperCompTNFOFields.PRODUCTION_IN_ONE_CYCLE;
import static com.qcadoo.mes.timeNormsForOperations.constants.TimeNormsConstants.FIELDS_OPERATION;
import static com.qcadoo.view.api.ComponentState.MessageType.INFO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperCompTNFOFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class TechnologyOperCompDetailsListenersTNFO {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void copyTimeNormsFromOperation(final ViewDefinitionState view, final ComponentState operationLookupState,
            final String[] args) {

        ComponentState operationLookup = view.getComponentByReference(OPERATION);
        if (operationLookup.getFieldValue() == null) {
            if (!OPERATION.equals(operationLookupState.getName())) {
                view.getComponentByReference("form").addMessage("productionTimeNorms.messages.info.missingOperationReference",
                        INFO);
            }
            return;
        }

        Entity operation = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_OPERATION).get((Long) operationLookup.getFieldValue());

        applyTimeNormsFromGivenSource(view, operation, FIELDS_OPERATION);
    }

    void applyTimeNormsFromGivenSource(final ViewDefinitionState view, final Entity source, final Iterable<String> fields) {
        checkArgument(source != null, "source entity is null");
        FieldComponent component = null;

        for (String fieldName : fields) {
            component = (FieldComponent) view.getComponentByReference(fieldName);
            component.setFieldValue(source.getField(fieldName));
        }

        if (source.getField(COUNT_REALIZED) == null) {
            view.getComponentByReference(COUNT_REALIZED).setFieldValue("01all");
        }

        if (source.getField(PRODUCTION_IN_ONE_CYCLE) == null) {
            view.getComponentByReference(PRODUCTION_IN_ONE_CYCLE).setFieldValue("1");
        }

        if (source.getField(COUNT_MACHINE) == null) {
            view.getComponentByReference(COUNT_MACHINE).setFieldValue("0");
        }

    }

    public void inheritOperationNormValues(final ViewDefinitionState viewDefinitionState, final ComponentState componentState,
            final String[] args) {
        copyTimeNormsFromOperation(viewDefinitionState, componentState, args);
    }

    public void changeCountRealizedNorm(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        FieldComponent countRealized = (FieldComponent) viewDefinitionState.getComponentByReference(COUNT_REALIZED);
        FieldComponent countMachine = (FieldComponent) viewDefinitionState.getComponentByReference(COUNT_MACHINE);
        FieldComponent countMachineUNIT = (FieldComponent) viewDefinitionState
                .getComponentByReference(TechnologyOperCompTNFOFields.COUNT_MACHINE_UNIT);

        Boolean visibilityValue = "02specified".equals(countRealized.getFieldValue());
        countMachine.setVisible(visibilityValue);
        countMachine.setEnabled(visibilityValue);
        countMachineUNIT.setVisible(visibilityValue);

    }

    public void changeCountRealizedNormOperation(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        FieldComponent countRealized = (FieldComponent) viewDefinitionState.getComponentByReference(COUNT_REALIZED);
        FieldComponent countMachine = (FieldComponent) viewDefinitionState.getComponentByReference(COUNT_MACHINE);
        FieldComponent countMachineUNIT = (FieldComponent) viewDefinitionState
                .getComponentByReference(TechnologyOperCompTNFOFields.COUNT_MACHINE_UNIT);

        Boolean visibilityValue = "02specified".equals(countRealized.getFieldValue());
        countMachine.setVisible(visibilityValue);
        countMachine.setEnabled(visibilityValue);
        countMachineUNIT.setVisible(visibilityValue);

    }
}
