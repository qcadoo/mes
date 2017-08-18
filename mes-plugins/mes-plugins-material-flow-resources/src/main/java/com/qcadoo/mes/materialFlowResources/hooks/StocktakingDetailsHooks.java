package com.qcadoo.mes.materialFlowResources.hooks;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.StocktakingFields;
import com.qcadoo.mes.materialFlowResources.constants.StorageLocationMode;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class StocktakingDetailsHooks {

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    public void onBeforeRender(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity stocktaking = form.getPersistedEntityWithIncludedFormValues();
        if (stocktaking.getId() == null) {
            numberGeneratorService.generateAndInsertNumber(view, MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                    MaterialFlowResourcesConstants.MODEL_STOCKTAKING, "form", StocktakingFields.NUMBER);
        }
        if (stocktaking.getDateField(StocktakingFields.STOCKTAKING_DATE) == null) {
            FieldComponent stocktakingDateField = (FieldComponent) view
                    .getComponentByReference(StocktakingFields.STOCKTAKING_DATE);
            stocktakingDateField.setFieldValue(new Date());
            stocktakingDateField.requestComponentUpdateState();
        }

        setCriteriaModifierParameters(view, stocktaking);
        changeStorageLocationsGridEnabled(view);
    }

    private void changeStorageLocationsGridEnabled(final ViewDefinitionState view) {
        GridComponent storageLocations = (GridComponent) view.getComponentByReference(StocktakingFields.STORAGE_LOCATIONS);
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity stocktaking = form.getEntity();
        boolean enabled = StorageLocationMode.SELECTED.getStringValue().equals(
                stocktaking.getStringField(StocktakingFields.STORAGE_LOCATION_MODE));
        storageLocations.setEnabled(enabled);

    }

    public void changeStorageLocationsGridEnabled(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        changeStorageLocationsGridEnabled(view);
    }

    private void setCriteriaModifierParameters(final ViewDefinitionState view, final Entity stocktaking) {
        LookupComponent storageLocationLookup = (LookupComponent) view.getComponentByReference("storageLocationLookup");
        Entity location = stocktaking.getBelongsToField(StocktakingFields.LOCATION);
        FilterValueHolder filterValueHolder = storageLocationLookup.getFilterValue();
        if (location != null) {
            filterValueHolder.put(StocktakingFields.LOCATION, location.getId());
        } else {
            filterValueHolder.put(StocktakingFields.LOCATION, 0L);
        }
        storageLocationLookup.setFilterValue(filterValueHolder);
    }
}
