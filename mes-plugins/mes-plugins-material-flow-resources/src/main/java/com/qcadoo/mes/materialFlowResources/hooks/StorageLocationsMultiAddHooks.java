package com.qcadoo.mes.materialFlowResources.hooks;

import com.qcadoo.mes.materialFlowResources.constants.StorageLocationFields;
import com.qcadoo.mes.materialFlowResources.constants.StorageLocationHelperFields;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FieldComponent;
import org.springframework.stereotype.Service;

@Service public class StorageLocationsMultiAddHooks {

    public void onBeforeRender(final ViewDefinitionState view) {
        FieldComponent number = (FieldComponent) view.getComponentByReference(StorageLocationHelperFields.NUMBER);
        number.setRequired(true);
        number.requestComponentUpdateState();

        FieldComponent prefix = (FieldComponent) view.getComponentByReference(StorageLocationHelperFields.PREFIX);
        prefix.setRequired(true);
        prefix.requestComponentUpdateState();

        FieldComponent location = (FieldComponent) view.getComponentByReference(StorageLocationHelperFields.LOCATION);
        location.setRequired(true);
        location.requestComponentUpdateState();

        FieldComponent numberOf = (FieldComponent) view
                .getComponentByReference(StorageLocationHelperFields.NUMBER_OF_STORAGE_LOCATIONS);
        numberOf.setRequired(true);
        numberOf.requestComponentUpdateState();

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
