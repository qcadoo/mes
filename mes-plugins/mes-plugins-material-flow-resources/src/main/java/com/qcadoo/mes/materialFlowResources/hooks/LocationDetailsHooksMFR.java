package com.qcadoo.mes.materialFlowResources.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialFlowResources.constants.LocationFieldsMFR;
import com.qcadoo.mes.materialFlowResources.constants.WarehouseAlgorithm;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class LocationDetailsHooksMFR {

    public void setEnabledForBatchCheckbox(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        FieldComponent algorithmField = (FieldComponent) view.getComponentByReference(LocationFieldsMFR.ALGORITHM);
        CheckBoxComponent batchCheckBox = (CheckBoxComponent) view.getComponentByReference(LocationFieldsMFR.REQUIRE_BATCH);
        String algorithm = (String) algorithmField.getFieldValue();
        if (WarehouseAlgorithm.MANUAL.getStringValue().equals(algorithm)) {
            batchCheckBox.setChecked(true);
            batchCheckBox.setEnabled(false);
        } else {
            batchCheckBox.setEnabled(form.isEnabled());
        }
        batchCheckBox.requestComponentUpdateState();
    }

}
