package com.qcadoo.mes.operationalTasksForOrders.hooks;

import java.util.List;

import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.operationalTasks.constants.OperationalTasksFields;
import com.qcadoo.mes.operationalTasksForOrders.constants.OperationalTasksOTFRFields;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class OperationalTasksDetailsHooksOTFO {

    public void disabledFieldWhenOrderTypeIsSelected(final ViewDefinitionState view) {

        FieldComponent type = (FieldComponent) view.getComponentByReference(OperationalTasksFields.TYPE_TASK);

        List<String> referenceBasicFields = Lists.newArrayList(OperationalTasksFields.NAME,
                OperationalTasksFields.PRODUCTION_LINE, OperationalTasksFields.DESCRIPTION);
        List<String> extendFields = Lists.newArrayList(OperationalTasksOTFRFields.ORDER,
                OperationalTasksOTFRFields.TECHNOLOGY_INSTANCE_OPERATION_COMPONENT);
        if (type.getFieldValue().equals("01otherCase")) {
            changedStateField(view, referenceBasicFields, true);
            changedStateField(view, extendFields, false);
            clearFieldValue(view, extendFields);
        } else {
            changedStateField(view, referenceBasicFields, false);
            changedStateField(view, extendFields, true);
        }
    }

    private void clearFieldValue(final ViewDefinitionState view, final List<String> references) {
        for (String reference : references) {
            FieldComponent field = (FieldComponent) view.getComponentByReference(reference);
            field.setFieldValue(null);
            field.requestComponentUpdateState();
        }
    }

    private void changedStateField(final ViewDefinitionState view, final List<String> references, final boolean enabled) {
        for (String reference : references) {
            FieldComponent field = (FieldComponent) view.getComponentByReference(reference);
            field.setEnabled(enabled);
            field.requestComponentUpdateState();
        }
    }
}
