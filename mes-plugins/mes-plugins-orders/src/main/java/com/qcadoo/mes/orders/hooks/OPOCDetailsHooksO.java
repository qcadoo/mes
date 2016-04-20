package com.qcadoo.mes.orders.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.constants.TechnologyFieldsO;
import com.qcadoo.mes.technologies.constants.OperationProductOutComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class OPOCDetailsHooksO {

    public void toggleSetField(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity opoc = form.getPersistedEntityWithIncludedFormValues();
        Entity operationComponent = opoc.getBelongsToField(OperationProductOutComponentFields.OPERATION_COMPONENT);
        EntityList ordersFromTechnology = operationComponent.getBelongsToField(TechnologyOperationComponentFields.TECHNOLOGY)
                .getHasManyField(TechnologyFieldsO.ORDERS);

        if (!ordersFromTechnology.isEmpty()) {
            hideSetCheckbox(view, true);
        }

    }

    private void hideSetCheckbox(final ViewDefinitionState view, final boolean enabled) {
        FieldComponent checkbox = (FieldComponent) view.getComponentByReference(OperationProductOutComponentFields.SET);
        checkbox.setVisible(false);
    }
}
