package com.qcadoo.mes.materialFlowResources.hooks;

import com.qcadoo.mes.advancedGenealogy.criteriaModifier.BatchCriteriaModifier;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.materialFlowResources.MaterialFlowResourcesService;
import com.qcadoo.mes.materialFlowResources.constants.StocktakingFields;
import com.qcadoo.mes.materialFlowResources.constants.StocktakingPositionFields;
import com.qcadoo.mes.materialFlowResources.constants.StorageLocationMode;
import com.qcadoo.mes.materialFlowResources.criteriaModifiers.StorageLocationCriteriaModifiers;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.qcadoo.mes.basic.constants.ProductFields.UNIT;

@Service
public class StocktakingPositionDetailsHooks {

    @Autowired
    private MaterialFlowResourcesService materialFlowResourcesService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private BatchCriteriaModifier batchCriteriaModifier;

    public void onBeforeRender(final ViewDefinitionState view) {
        FormComponent stocktakingPositionForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity stocktakingPosition = stocktakingPositionForm.getPersistedEntityWithIncludedFormValues();

        materialFlowResourcesService.fillUnitFieldValues(view);
        fillUnitField(view, stocktakingPositionForm);

        setStorageLocationLookupFilterValue(view, stocktakingPosition);
        setProductLookupCategoryFilterValue(view, stocktakingPosition);
        setBatchLookupProductFilterValue(view, stocktakingPosition);
    }

    private void setStorageLocationLookupFilterValue(ViewDefinitionState view, Entity stocktakingPosition) {
        LookupComponent storageLocationLookup = (LookupComponent) view.getComponentByReference(StocktakingPositionFields.STORAGE_LOCATION);

        FilterValueHolder filter = storageLocationLookup.getFilterValue();

        Entity stocktaking = stocktakingPosition.getBelongsToField(StocktakingPositionFields.STOCKTAKING);

        Entity warehouse = stocktaking.getBelongsToField(StocktakingFields.LOCATION);

        filter.put(StocktakingFields.LOCATION, warehouse.getId());

        if (StorageLocationMode.SELECTED.getStringValue().equals(
                stocktaking.getStringField(StocktakingFields.STORAGE_LOCATION_MODE))) {
            List<Entity> storageLocations = stocktaking.getHasManyField(StocktakingFields.STORAGE_LOCATIONS);
            if (storageLocations.size() == 1) {
                storageLocationLookup.setFieldValue(storageLocations.get(0).getId());
                storageLocationLookup.setEnabled(false);
                storageLocationLookup.requestComponentUpdateState();
            }
            if (!storageLocations.isEmpty()) {
                filter.put(StorageLocationCriteriaModifiers.IDS, storageLocations.stream().map(Entity::getId).collect(Collectors.toList()));
            }
        }

        storageLocationLookup.setFilterValue(filter);
    }

    private void setProductLookupCategoryFilterValue(ViewDefinitionState view, Entity stocktakingPosition) {
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(StocktakingPositionFields.PRODUCT);

        FilterValueHolder filter = productLookup.getFilterValue();

        Entity stocktaking = stocktakingPosition.getBelongsToField(StocktakingPositionFields.STOCKTAKING);

        String category = stocktaking.getStringField(StocktakingFields.CATEGORY);

        if (Objects.nonNull(category)) {
            filter.put(StocktakingFields.CATEGORY, category);
            productLookup.setFilterValue(filter);
        }
    }

    private void setBatchLookupProductFilterValue(ViewDefinitionState view, Entity stocktakingPosition) {
        LookupComponent batchLookup = (LookupComponent) view.getComponentByReference(StocktakingPositionFields.BATCH);

        Entity product = stocktakingPosition.getBelongsToField(StocktakingPositionFields.PRODUCT);

        if (Objects.nonNull(product)) {
            batchCriteriaModifier.putProductFilterValue(batchLookup, product);
        }
    }

    private void fillUnitField(final ViewDefinitionState view, FormComponent stocktakingForm) {
        FieldComponent stockUnitField = (FieldComponent) view.getComponentByReference("stockUNIT");
        FieldComponent additionalUnitField = (FieldComponent) view.getComponentByReference(ProductFields.ADDITIONAL_UNIT);

        Long productId = (Long) view.getComponentByReference(StocktakingPositionFields.PRODUCT).getFieldValue();

        if (Objects.isNull(productId)) {
            FieldComponent unitField = (FieldComponent) view.getComponentByReference("quantityUNIT");
            unitField.setFieldValue(null);
            unitField.requestComponentUpdateState();
            additionalUnitField.setFieldValue(null);
            additionalUnitField.requestComponentUpdateState();
            stockUnitField.setFieldValue(null);
            stockUnitField.requestComponentUpdateState();
            return;
        }

        Entity product = getProductDD().get(productId);
        String unit = product.getStringField(UNIT);
        String additionalUnit = product.getStringField(ProductFields.ADDITIONAL_UNIT);
        FieldComponent conversion = (FieldComponent) view.getComponentByReference(StocktakingPositionFields.CONVERSION);
        if (StringUtils.isEmpty(additionalUnit)) {
            conversion.setFieldValue(BigDecimal.ONE);
        }
        if (StringUtils.isEmpty(additionalUnit) || stocktakingForm.getEntityId() != null) {
            conversion.setEnabled(false);
            conversion.requestComponentUpdateState();
        }
        if (Objects.isNull(additionalUnit)) {
            additionalUnit = product.getStringField(ProductFields.UNIT);
        }
        additionalUnitField.setFieldValue(additionalUnit);
        additionalUnitField.requestComponentUpdateState();
        stockUnitField.setFieldValue(unit);
        stockUnitField.requestComponentUpdateState();
    }

    private DataDefinition getProductDD() {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT);
    }
}
