package com.qcadoo.mes.technologies.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.technologies.constants.OperationProductOutComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class OPOCDetailsHooks {

    public void onBeforeRender(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity opoc = form.getPersistedEntityWithIncludedFormValues();
        Entity product = opoc.getBelongsToField(OperationProductOutComponentFields.PRODUCT);
        if (product == null) {
            setEnabled(view, false);
            return;
        }
        Entity operationComponent = opoc.getBelongsToField(OperationProductOutComponentFields.OPERATION_COMPONENT);
        Entity productFromTechnology = operationComponent.getBelongsToField(TechnologyOperationComponentFields.TECHNOLOGY)
                .getBelongsToField(TechnologyFields.PRODUCT);

        if (operationComponent.getBelongsToField(TechnologyOperationComponentFields.PARENT) == null
                && product.getId().equals(productFromTechnology.getId())) {
            setEnabled(view, true);
        } else {
            setEnabled(view, false);
        }

    }

    private void setEnabled(final ViewDefinitionState view, final boolean enabled) {
        FieldComponent checkbox = (FieldComponent) view.getComponentByReference(OperationProductOutComponentFields.SET);
        checkbox.setEnabled(enabled);
    }
}
