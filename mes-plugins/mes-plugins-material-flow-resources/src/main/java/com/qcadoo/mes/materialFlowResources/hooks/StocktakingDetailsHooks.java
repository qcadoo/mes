package com.qcadoo.mes.materialFlowResources.hooks;

import com.qcadoo.localization.api.utils.DateUtils;
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
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;

@Service
public class StocktakingDetailsHooks {

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    public void onBeforeRender(final ViewDefinitionState view) {
        FormComponent stocktakingForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity stocktaking = stocktakingForm.getPersistedEntityWithIncludedFormValues();

        if (Objects.isNull(stocktaking.getId())) {
            numberGeneratorService.generateAndInsertNumber(view, MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                    MaterialFlowResourcesConstants.MODEL_STOCKTAKING, QcadooViewConstants.L_FORM, StocktakingFields.NUMBER);
        }

        if (Objects.isNull(stocktaking.getDateField(StocktakingFields.STOCKTAKING_DATE))) {
            FieldComponent stocktakingDateField = (FieldComponent) view
                    .getComponentByReference(StocktakingFields.STOCKTAKING_DATE);

            stocktakingDateField.setFieldValue(DateUtils.toDateString(new Date()));
            stocktakingDateField.requestComponentUpdateState();
        }

        disableForm(view, stocktakingForm, stocktaking);

        setCriteriaModifierParameters(view, stocktaking);
    }

    private void disableForm(final ViewDefinitionState view, final FormComponent stocktakingForm, final Entity stocktaking) {
        GridComponent storageLocationsGrid = (GridComponent) view.getComponentByReference(StocktakingFields.STORAGE_LOCATIONS);

        if (stocktaking.getBooleanField(StocktakingFields.GENERATED)) {
            stocktakingForm.setFormEnabled(false);
            storageLocationsGrid.setEnabled(false);
        } else {
            stocktakingForm.setFormEnabled(true);
            changeStorageLocationsGridEnabled(view);
        }
    }

    private void changeStorageLocationsGridEnabled(final ViewDefinitionState view) {
        FormComponent stocktakingForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        GridComponent storageLocationsGrid = (GridComponent) view.getComponentByReference(StocktakingFields.STORAGE_LOCATIONS);

        Entity stocktaking = stocktakingForm.getEntity();

        boolean enabled = StorageLocationMode.SELECTED.getStringValue().equals(
                stocktaking.getStringField(StocktakingFields.STORAGE_LOCATION_MODE));

        storageLocationsGrid.setEnabled(enabled);
    }

    public void changeStorageLocationsGridEnabled(final ViewDefinitionState view, final ComponentState componentState,
                                                  final String[] args) {
        changeStorageLocationsGridEnabled(view);
    }

    private void setCriteriaModifierParameters(final ViewDefinitionState view, final Entity stocktaking) {
        LookupComponent storageLocationLookup = (LookupComponent) view.getComponentByReference("storageLocationLookup");

        Entity location = stocktaking.getBelongsToField(StocktakingFields.LOCATION);

        FilterValueHolder filterValueHolder = storageLocationLookup.getFilterValue();

        if (Objects.nonNull(location)) {
            filterValueHolder.put(StocktakingFields.LOCATION, location.getId());
        } else {
            filterValueHolder.put(StocktakingFields.LOCATION, 0L);
        }

        storageLocationLookup.setFilterValue(filterValueHolder);
    }

}
