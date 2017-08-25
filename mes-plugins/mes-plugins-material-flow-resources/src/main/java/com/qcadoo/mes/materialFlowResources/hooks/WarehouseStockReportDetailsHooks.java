package com.qcadoo.mes.materialFlowResources.hooks;

import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.materialFlowResources.constants.StorageLocationMode;
import com.qcadoo.mes.materialFlowResources.constants.WarehouseStockReportFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;

@Service
public class WarehouseStockReportDetailsHooks {

    public void onBeforeRender(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity warehouseStockReport = form.getPersistedEntityWithIncludedFormValues();
        if (Objects.isNull(warehouseStockReport.getDateField(WarehouseStockReportFields.WAREHOUSE_STOCK_DATE))) {
            FieldComponent warehouseStockDateField = (FieldComponent) view
                    .getComponentByReference(WarehouseStockReportFields.WAREHOUSE_STOCK_DATE);
            warehouseStockDateField.setFieldValue(DateUtils.toDateString(new Date()));
            warehouseStockDateField.requestComponentUpdateState();
        }
        setCriteriaModifierParameters(view, warehouseStockReport);
        disableForm(view, form, warehouseStockReport);
    }

    private void disableForm(final ViewDefinitionState view, final FormComponent form, final Entity stocktaking) {
        if(stocktaking.getBooleanField(WarehouseStockReportFields.GENERATED)) {
            form.setFormEnabled(false);
        } else {
            form.setFormEnabled(true);
            changeStorageLocationsGridEnabled(view);
        }
    }

    private void changeStorageLocationsGridEnabled(final ViewDefinitionState view) {
        GridComponent storageLocations = (GridComponent) view
                .getComponentByReference(WarehouseStockReportFields.STORAGE_LOCATIONS);
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity stocktaking = form.getEntity();
        boolean enabled = StorageLocationMode.SELECTED.getStringValue().equals(
                stocktaking.getStringField(WarehouseStockReportFields.STORAGE_LOCATION_MODE));
        storageLocations.setEnabled(enabled);

    }

    public void changeStorageLocationsGridEnabled(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        changeStorageLocationsGridEnabled(view);
    }

    private void setCriteriaModifierParameters(final ViewDefinitionState view, final Entity warehouseStockReport) {
        LookupComponent storageLocationLookup = (LookupComponent) view.getComponentByReference("storageLocationLookup");
        Entity location = warehouseStockReport.getBelongsToField(WarehouseStockReportFields.LOCATION);
        FilterValueHolder filterValueHolder = storageLocationLookup.getFilterValue();
        if (location != null) {
            filterValueHolder.put(WarehouseStockReportFields.LOCATION, location.getId());
        } else {
            filterValueHolder.put(WarehouseStockReportFields.LOCATION, 0L);
        }
        storageLocationLookup.setFilterValue(filterValueHolder);
    }
}
