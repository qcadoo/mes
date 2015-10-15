package com.qcadoo.mes.materialFlowResources.hooks;

import com.qcadoo.mes.materialFlowResources.constants.StorageLocationFields;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FieldComponent;
import org.springframework.stereotype.Service;

@Service
public class StorageLocationsDetailsHooks {

    public void onBeforeRender(final ViewDefinitionState view) {
        FieldComponent maximumNumber = (FieldComponent) view.getComponentByReference(StorageLocationFields.MAXIMUM_NUMBER_OF_PALLETS);
        CheckBoxComponent isPlace = (CheckBoxComponent) view.getComponentByReference(StorageLocationFields.PLACE_STORAGE_LOCATION);
        if (isPlace.isChecked()) {
            maximumNumber.setEnabled(true);
        } else {
            maximumNumber.setEnabled(false);
            maximumNumber.setFieldValue(null);
            maximumNumber.requestComponentUpdateState();
        }
    }
}
