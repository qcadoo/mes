package com.qcadoo.mes.materialFlowResources.hooks;

import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.StocktakingFields;
import com.qcadoo.mes.materialFlowResources.constants.StorageLocationMode;
import com.qcadoo.mes.materialFlowResources.states.constants.StocktakingStateStringValues;
import com.qcadoo.model.api.Entity;
import com.qcadoo.plugin.api.PluginManager;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.*;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.api.utils.NumberGeneratorService;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;

import static com.qcadoo.mes.materialFlowResources.listeners.DocumentsListListeners.MOBILE_WMS;
import static com.qcadoo.mes.materialFlowResources.listeners.DocumentsListListeners.REALIZED;

@Service
public class StocktakingDetailsHooks {

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private PluginManager pluginManager;

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
        if (pluginManager.isPluginEnabled(MOBILE_WMS)) {
            if (stocktaking.getBooleanField(StocktakingFields.WMS)
                    && !REALIZED.equals(stocktaking.getStringField(StocktakingFields.STATE_IN_WMS))) {
                WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
                RibbonGroup stateGroup = window.getRibbon().getGroupByName("state");
                RibbonActionItem rejectRibbonActionItem = stateGroup.getItemByName("reject");
                RibbonGroup generateGroup = window.getRibbon().getGroupByName("generate");
                RibbonActionItem copyFromStockRibbonActionItem = generateGroup.getItemByName("copyFromStock");
                RibbonActionItem settleRibbonActionItem = generateGroup.getItemByName("settle");
                copyFromStockRibbonActionItem.setEnabled(false);
                copyFromStockRibbonActionItem.requestUpdate(true);
                settleRibbonActionItem.setEnabled(false);
                settleRibbonActionItem.setMessage("materialFlowResources.stocktakingDetails.ribbon.message.settle");
                settleRibbonActionItem.requestUpdate(true);
                rejectRibbonActionItem.setEnabled(false);
                rejectRibbonActionItem.requestUpdate(true);
            }
        }
    }

    private void disableForm(final ViewDefinitionState view, final FormComponent stocktakingForm, final Entity stocktaking) {
        GridComponent storageLocationsGrid = (GridComponent) view.getComponentByReference(StocktakingFields.STORAGE_LOCATIONS);
        GridComponent positionsGrid = (GridComponent) view.getComponentByReference(StocktakingFields.POSITIONS);

        if (stocktaking.getId() != null) {
            String state = stocktaking.getStringField(StocktakingFields.STATE);

            if (StocktakingStateStringValues.DRAFT.equals(state)) {
                stocktakingForm.setFormEnabled(true);
                changeStorageLocationsGridEnabled(view);
            } else {
                stocktakingForm.setFormEnabled(false);
                storageLocationsGrid.setEnabled(false);
            }

            positionsGrid.setEnabled(StocktakingStateStringValues.IN_PROGRESS.equals(state) && (!stocktaking.getBooleanField(StocktakingFields.WMS)
                    || REALIZED.equals(stocktaking.getStringField(StocktakingFields.STATE_IN_WMS))) || StocktakingStateStringValues.FINALIZED.equals(state));
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
