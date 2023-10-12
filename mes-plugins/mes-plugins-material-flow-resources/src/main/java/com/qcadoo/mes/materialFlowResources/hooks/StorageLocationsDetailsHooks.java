package com.qcadoo.mes.materialFlowResources.hooks;

import com.qcadoo.mes.materialFlowResources.constants.StorageLocationFields;
import com.qcadoo.mes.materialFlowResources.criteriaModifiers.ProductsCriteriaModifiers;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.*;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
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
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(StorageLocationFields.PRODUCT);
        FieldComponent maximumNumberField = (FieldComponent) view.getComponentByReference(StorageLocationFields.MAXIMUM_NUMBER_OF_PALLETS);
        CheckBoxComponent isPlaceCheckBox = (CheckBoxComponent) view.getComponentByReference(StorageLocationFields.PLACE_STORAGE_LOCATION);
        GridComponent productsGrid = (GridComponent) view.getComponentByReference(StorageLocationFields.PRODUCTS);

        Entity product = productLookup.getEntity();
        List<Entity> products = productsGrid.getEntities();

        boolean isSaved = Objects.nonNull(storageLocationForm.getEntityId());

        if (Objects.nonNull(product) || StringUtils.isNotEmpty(productLookup.getCurrentCode())) {
            productLookup.setEnabled(true);
            productsGrid.setEnabled(false);
        } else {
            productLookup.setEnabled(products.isEmpty());
            productsGrid.setEnabled(isSaved);
        }

        if (isPlaceCheckBox.isChecked()) {
            maximumNumberField.setEnabled(true);
        } else {
            maximumNumberField.setEnabled(false);
            maximumNumberField.setFieldValue(null);
        }

        productLookup.requestComponentUpdateState();
        maximumNumberField.requestComponentUpdateState();
    }

    private void setFilterValueHolders(final ViewDefinitionState view) {
        LookupComponent locationLookup = (LookupComponent) view.getComponentByReference(StorageLocationFields.LOCATION);
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(StorageLocationFields.PRODUCT);
        LookupComponent productsLookup = (LookupComponent) view.getComponentByReference(L_PRODUCTS_LOOKUP);

        Entity location = locationLookup.getEntity();

        FilterValueHolder filterValueHolder = productLookup.getFilterValue();

        if (Objects.isNull(location)) {
            filterValueHolder.remove(ProductsCriteriaModifiers.L_LOCATION_ID);
        } else {
            Long locationId = location.getId();

            filterValueHolder.put(ProductsCriteriaModifiers.L_LOCATION_ID, locationId);
        }

        productLookup.setFilterValue(filterValueHolder);
        productsLookup.setFilterValue(filterValueHolder);
    }

}
