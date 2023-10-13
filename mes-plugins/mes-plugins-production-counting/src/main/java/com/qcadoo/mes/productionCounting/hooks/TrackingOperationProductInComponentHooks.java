package com.qcadoo.mes.productionCounting.hooks;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingConstants;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityRole;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityTypeOfMaterial;
import com.qcadoo.mes.productionCounting.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.constants.ProductionTrackingFields;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductInComponentFields;
import com.qcadoo.mes.productionCounting.constants.UsedBatchFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
public class TrackingOperationProductInComponentHooks {

    private static final String PRODUCT = "product";
    public static final String L_AUTOMATICALLY_USE_THE_BATCH_PLANNED_IN_ORDER = "automaticallyUseTheBatchPlannedInOrder";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ParameterService parameterService;

    public void onSave(final DataDefinition trackingOperationProductOutComponentDD,
                       final Entity trackingOperationProductInComponent) {

        if (Objects.isNull(trackingOperationProductInComponent.getId())) {
            createProductionCountingIfReplacement(trackingOperationProductInComponent);
        }

        if (Objects.nonNull(trackingOperationProductInComponent.getId()) && parameterService.getParameter().getBooleanField(L_AUTOMATICALLY_USE_THE_BATCH_PLANNED_IN_ORDER)) {
            Entity trackingOperationProductInComponentDb = trackingOperationProductInComponent.getDataDefinition().get(trackingOperationProductInComponent.getId());

            if (Objects.nonNull(trackingOperationProductInComponent.getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY))
                    && Objects.isNull(trackingOperationProductInComponentDb.getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY))
                    && trackingOperationProductInComponent.getHasManyField(TrackingOperationProductInComponentFields.USED_BATCHES).isEmpty()) {

                Entity productionTracking = trackingOperationProductInComponent.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCTION_TRACKING);
                Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);
                Entity toc = productionTracking.getBelongsToField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT);

                SearchCriteriaBuilder scb = getProductionCountingQuantityDD()
                        .find()
                        .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.ORDER, order))
                        .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.PRODUCT, trackingOperationProductInComponent.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT)))
                        .add(SearchRestrictions.eq(ProductionCountingQuantityFields.ROLE,
                                ProductionCountingQuantityRole.USED.getStringValue()))
                        .add(SearchRestrictions.eq(ProductionCountingQuantityFields.TYPE_OF_MATERIAL, "01component"));

                if (Objects.nonNull(toc)) {
                    scb.add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT, toc));
                }

                Entity pcq = scb.setMaxResults(1).uniqueResult();

                if(Objects.nonNull(pcq) && Objects.nonNull(pcq.getHasManyField(ProductionCountingQuantityFields.BATCHES))) {
                    List<Entity> batches = pcq.getHasManyField(ProductionCountingQuantityFields.BATCHES);

                    if (batches.size() == 1) {
                        Entity usedBatch = dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_USED_BATCH).create();
                        usedBatch.setField(UsedBatchFields.QUANTITY, trackingOperationProductInComponent.getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY));
                        usedBatch.setField(UsedBatchFields.BATCH, batches.get(0).getId());
                        usedBatch.setField(UsedBatchFields.TRACKING_OPERATION_PRODUCT_IN_COMPONENT,
                                trackingOperationProductInComponent.getId());
                        usedBatch = usedBatch.getDataDefinition().fastSave(usedBatch);
                    }
                }
            }
        }


    }

    private void createProductionCountingIfReplacement(final Entity trackingOperationProductInComponent) {
        if (Objects.nonNull(trackingOperationProductInComponent
                .getBelongsToField(TrackingOperationProductInComponentFields.REPLACEMENT_TO))) {

            Entity productionTracking = trackingOperationProductInComponent
                    .getBelongsToField(TrackingOperationProductInComponentFields.PRODUCTION_TRACKING);
            Entity baseProduct = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT).get(
                    trackingOperationProductInComponent.getBelongsToField(
                            TrackingOperationProductInComponentFields.REPLACEMENT_TO).getId());

            SearchCriteriaBuilder scb = dataDefinitionService
                    .get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                            BasicProductionCountingConstants.MODEL_PRODUCTION_COUNTING_QUANTITY)
                    .find()
                    .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.ORDER,
                            productionTracking.getBelongsToField(ProductionTrackingFields.ORDER)))
                    .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.PRODUCT,
                            trackingOperationProductInComponent.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT)));
            if (Objects.nonNull(productionTracking.getBelongsToField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT))) {
                scb = scb.add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT,
                        productionTracking.getBelongsToField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT)));
            }

            Entity pcq = scb.setMaxResults(1).uniqueResult();
            if (Objects.isNull(pcq)) {
                createProductionCountingQuantity(
                        trackingOperationProductInComponent.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT),
                        baseProduct, productionTracking);
            }
        }
    }

    private void createProductionCountingQuantity(Entity product, Entity baseProduct, Entity productionTracking) {
        Entity productionCountingQuantity = dataDefinitionService.get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                BasicProductionCountingConstants.MODEL_PRODUCTION_COUNTING_QUANTITY).create();
        productionCountingQuantity.setField(ProductionCountingQuantityFields.ORDER,
                productionTracking.getBelongsToField(ProductionTrackingFields.ORDER).getId());
        if (Objects.nonNull(productionTracking.getBelongsToField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT))) {
            productionCountingQuantity.setField(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT,
                    productionTracking.getBelongsToField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT).getId());
        }
        productionCountingQuantity.setField(ProductionCountingQuantityFields.PLANNED_QUANTITY, BigDecimal.ZERO);
        productionCountingQuantity.setField(ProductionCountingQuantityFields.PRODUCT, product.getId());
        productionCountingQuantity.setField(ProductionCountingQuantityFields.REPLACEMENT_TO, baseProduct.getId());
        productionCountingQuantity.setField("role", ProductionCountingQuantityRole.USED.getStringValue());
        productionCountingQuantity
                .setField("typeOfMaterial", ProductionCountingQuantityTypeOfMaterial.COMPONENT.getStringValue());
        productionCountingQuantity.setField(ProductionCountingQuantityFields.FLOW_FILLED, Boolean.TRUE);
        Entity pCQBaseProduct = dataDefinitionService
                .get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                        BasicProductionCountingConstants.MODEL_PRODUCTION_COUNTING_QUANTITY)
                .find()
                .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.ORDER,
                        productionTracking.getBelongsToField(ProductionTrackingFields.ORDER)))
                .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.PRODUCT, baseProduct))
                .add(SearchRestrictions.eq(ProductionCountingQuantityFields.ROLE,
                        ProductionCountingQuantityRole.USED.getStringValue())).setMaxResults(1).uniqueResult();
        if (Objects.nonNull(pCQBaseProduct)) {
            String type = pCQBaseProduct.getStringField(ProductionCountingQuantityFields.TYPE_OF_MATERIAL);
            if (ProductionCountingQuantityTypeOfMaterial.COMPONENT.getStringValue().equals(type)) {
                productionCountingQuantity.setField(ProductionCountingQuantityFields.COMPONENTS_LOCATION,
                        pCQBaseProduct.getField(ProductionCountingQuantityFields.COMPONENTS_LOCATION));
                productionCountingQuantity.setField(ProductionCountingQuantityFields.COMPONENTS_OUTPUT_LOCATION,
                        pCQBaseProduct.getField(ProductionCountingQuantityFields.COMPONENTS_OUTPUT_LOCATION));
            } else if (ProductionCountingQuantityTypeOfMaterial.INTERMEDIATE.getStringValue().equals(type)) {
                productionCountingQuantity.setField(ProductionCountingQuantityFields.PRODUCTION_FLOW,
                        pCQBaseProduct.getField(ProductionCountingQuantityFields.PRODUCTION_FLOW));
                productionCountingQuantity.setField(ProductionCountingQuantityFields.PRODUCTS_FLOW_LOCATION,
                        pCQBaseProduct.getField(ProductionCountingQuantityFields.PRODUCTS_FLOW_LOCATION));
            }
        }
        productionCountingQuantity = productionCountingQuantity.getDataDefinition().save(productionCountingQuantity);
    }


    private DataDefinition getProductionCountingQuantityDD() {
        return dataDefinitionService.get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                BasicProductionCountingConstants.MODEL_PRODUCTION_COUNTING_QUANTITY);
    }

}
