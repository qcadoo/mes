package com.qcadoo.mes.productionCounting;

import static com.qcadoo.model.api.search.SearchProjections.alias;
import static com.qcadoo.model.api.search.SearchProjections.field;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;
import com.qcadoo.mes.basicProductionCounting.ProductionTrackingUpdateService;
import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.constants.ProductionTrackingFields;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductInComponentFields;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductOutComponentFields;
import com.qcadoo.mes.productionCounting.hooks.helpers.OperationProductsExtractor;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

@Service
public class ProductionTrackingUpdateServiceImpl implements ProductionTrackingUpdateService {

    private static final String PRODUCT_ID_FIELD_NAME = "productId";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private OperationProductsExtractor operationProductsExtractor;

    @Override
    public void updateProductionTracking(final Long productionTrackingId) {
        Entity productionTracking = dataDefinitionService
                .get(ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_PRODUCTION_TRACKING)
                .get(productionTrackingId);

        Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);

        OperationProductsExtractor.TrackingOperationProducts operationProducts = operationProductsExtractor
                .getProductsByModelName(productionTracking);

        if (order.getBooleanField(OrderFieldsPC.REGISTER_QUANTITY_IN_PRODUCT)) {
            updateInProducts(productionTracking, operationProducts);
        }

        if (order.getBooleanField(OrderFieldsPC.REGISTER_QUANTITY_OUT_PRODUCT)) {
            updateOutProducts(productionTracking, operationProducts);
        }
    }

    private void updateOutProducts(final Entity productionTracking,
            final OperationProductsExtractor.TrackingOperationProducts operationProducts) {
        List<Entity> outputs = operationProducts.getOutputComponents();
        List<Entity> productionTrackingOutputs = productionTracking
                .getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_OUT_COMPONENTS).find()
                .setProjection(alias(field(TrackingOperationProductOutComponentFields.PRODUCT + ".id"), PRODUCT_ID_FIELD_NAME))
                .list().getEntities();

        Map<Long, Entity> outputsMap = outputs.stream().collect(
                Collectors.toMap(x -> x.getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCT).getId(), x -> x));
        Map<Long, Entity> productionTrackingOutputsMap = productionTrackingOutputs.stream()
                .collect(Collectors.toMap(x -> x.getLongField(PRODUCT_ID_FIELD_NAME), x -> x));

        Set<Long> newEntries = Sets.difference(outputsMap.keySet(), productionTrackingOutputsMap.keySet());
        Set<Long> removedEntries = Sets.difference(productionTrackingOutputsMap.keySet(), outputsMap.keySet());

        for (Long newEntry : newEntries) {
            Entity trackingOperationProductOutComponent = outputsMap.get(newEntry);
            trackingOperationProductOutComponent.setField(TrackingOperationProductOutComponentFields.PRODUCTION_TRACKING,
                    productionTracking);
            trackingOperationProductOutComponent.getDataDefinition().save(trackingOperationProductOutComponent);
        }

        for (Long removedEntry : removedEntries) {
            dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                    ProductionCountingConstants.MODEL_TRACKING_OPERATION_PRODUCT_OUT_COMPONENT).delete(removedEntry);
        }
    }

    private void updateInProducts(final Entity productionTracking,
            final OperationProductsExtractor.TrackingOperationProducts operationProducts) {
        List<Entity> inputs = operationProducts.getInputComponents();
        List<Entity> productionTrackingInputs = productionTracking
                .getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_IN_COMPONENTS).find()
                .setProjection(alias(field(TrackingOperationProductInComponentFields.PRODUCT + ".id"), PRODUCT_ID_FIELD_NAME))
                .list().getEntities();

        Map<Long, Entity> inputsMap = inputs.stream().collect(
                Collectors.toMap(x -> x.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT).getId(), x -> x));
        Map<Long, Entity> productionTrackingInputsMap = productionTrackingInputs.stream()
                .collect(Collectors.toMap(x -> x.getLongField(PRODUCT_ID_FIELD_NAME), x -> x));

        Set<Long> newEntries = Sets.difference(inputsMap.keySet(), productionTrackingInputsMap.keySet());
        Set<Long> removedEntries = Sets.difference(productionTrackingInputsMap.keySet(), inputsMap.keySet());

        for (Long newEntry : newEntries) {
            Entity trackingOperationProductInComponent = inputsMap.get(newEntry);
            trackingOperationProductInComponent.setField(TrackingOperationProductInComponentFields.PRODUCTION_TRACKING,
                    productionTracking);
            trackingOperationProductInComponent.getDataDefinition().save(trackingOperationProductInComponent);
        }

        for (Long removedEntry : removedEntries) {
            dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                    ProductionCountingConstants.MODEL_TRACKING_OPERATION_PRODUCT_IN_COMPONENT).delete(removedEntry);
        }
    }
}
