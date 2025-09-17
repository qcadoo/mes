package com.qcadoo.mes.materialFlowResources.hooks;

import com.qcadoo.mes.advancedGenealogy.criteriaModifier.BatchCriteriaModifier;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.materialFlowResources.MaterialFlowResourcesService;
import com.qcadoo.mes.materialFlowResources.constants.StocktakingFields;
import com.qcadoo.mes.materialFlowResources.constants.StocktakingPositionFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

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
        FormComponent stocktakingForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity stocktakingPosition = stocktakingForm.getPersistedEntityWithIncludedFormValues();

        materialFlowResourcesService.fillUnitFieldValues(view);
        fillUnitField(view);

        setStorageLocationLookupFilterValue(view, stocktakingPosition);
        setBatchLookupProductFilterValue(view, stocktakingPosition);
    }

    private void setStorageLocationLookupFilterValue(ViewDefinitionState view, Entity stocktakingPosition) {
        LookupComponent storageLocationLookup = (LookupComponent) view.getComponentByReference(StocktakingPositionFields.STORAGE_LOCATION);

        FilterValueHolder filter = storageLocationLookup.getFilterValue();

        Entity stocktaking = stocktakingPosition.getBelongsToField(StocktakingPositionFields.STOCKTAKING);

        Entity warehouse = stocktaking.getBelongsToField(StocktakingFields.LOCATION);

        filter.put(StocktakingFields.LOCATION, warehouse.getId());

        storageLocationLookup.setFilterValue(filter);
    }

    private void setBatchLookupProductFilterValue(ViewDefinitionState view, Entity stocktakingPosition) {
        LookupComponent batchLookup = (LookupComponent) view.getComponentByReference(StocktakingPositionFields.BATCH);

        Entity product = stocktakingPosition.getBelongsToField(StocktakingPositionFields.PRODUCT);

        if (Objects.nonNull(product)) {
            batchCriteriaModifier.putProductFilterValue(batchLookup, product);
        }
    }

    private void fillUnitField(final ViewDefinitionState view) {
        FieldComponent stockUnitField = (FieldComponent) view.getComponentByReference("stockUNIT");

        Long productId = (Long) view.getComponentByReference(StocktakingPositionFields.PRODUCT).getFieldValue();

        if (Objects.isNull(productId)) {
            return;
        }

        Entity product = getProductDD().get(productId);
        String unit = product.getStringField(UNIT);

        stockUnitField.setFieldValue(unit);
        stockUnitField.requestComponentUpdateState();
    }

    private DataDefinition getProductDD() {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT);
    }
}
