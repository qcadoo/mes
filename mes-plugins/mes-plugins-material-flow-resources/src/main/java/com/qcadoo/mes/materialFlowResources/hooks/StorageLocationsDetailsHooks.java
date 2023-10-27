package com.qcadoo.mes.materialFlowResources.hooks;

import com.qcadoo.mes.materialFlowResources.constants.StorageLocationFields;
import com.qcadoo.mes.materialFlowResources.criteriaModifiers.ProductsCriteriaModifiers;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.*;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class StorageLocationsDetailsHooks {

    public static final String L_PRODUCTS_LOOKUP = "productsLookup";

    public void onBeforeRender(final ViewDefinitionState view) {
        setFieldsEnabled(view);
        setFilterValueHolders(view);
    }

    private void setFieldsEnabled(final ViewDefinitionState view) {
        FormComponent storageLocationForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        FieldComponent maximumNumberField = (FieldComponent) view.getComponentByReference(StorageLocationFields.MAXIMUM_NUMBER_OF_PALLETS);
        CheckBoxComponent isPlaceCheckBox = (CheckBoxComponent) view.getComponentByReference(StorageLocationFields.PLACE_STORAGE_LOCATION);
        GridComponent productsGrid = (GridComponent) view.getComponentByReference(StorageLocationFields.PRODUCTS);

        boolean isSaved = Objects.nonNull(storageLocationForm.getEntityId());

        if (isPlaceCheckBox.isChecked()) {
            maximumNumberField.setEnabled(true);
        } else {
            maximumNumberField.setEnabled(false);
            maximumNumberField.setFieldValue(null);
        }

        maximumNumberField.requestComponentUpdateState();
        productsGrid.setEnabled(isSaved);
    }

    private void setFilterValueHolders(final ViewDefinitionState view) {
        LookupComponent locationLookup = (LookupComponent) view.getComponentByReference(StorageLocationFields.LOCATION);
        LookupComponent productsLookup = (LookupComponent) view.getComponentByReference(L_PRODUCTS_LOOKUP);

        Entity location = locationLookup.getEntity();

        FilterValueHolder filterValueHolder = productsLookup.getFilterValue();

        if (Objects.isNull(location)) {
            filterValueHolder.remove(ProductsCriteriaModifiers.L_LOCATION_ID);
        } else {
            Long locationId = location.getId();

            filterValueHolder.put(ProductsCriteriaModifiers.L_LOCATION_ID, locationId);
        }

        productsLookup.setFilterValue(filterValueHolder);
    }

}
