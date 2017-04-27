package com.qcadoo.mes.productionCounting;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.qcadoo.mes.basicProductionCounting.ProductionTrackingUpdateService;
import com.qcadoo.mes.productionCounting.constants.*;
import com.qcadoo.mes.productionCounting.hooks.helpers.OperationProductsExtractor;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProductionTrackingUpdateServiceImpl implements ProductionTrackingUpdateService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private OperationProductsExtractor operationProductsExtractor;

    @Override
    public void updateProductionTracking(final Long productionTrackingId) {
        Entity productionTracking = dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                ProductionCountingConstants.MODEL_PRODUCTION_TRACKING).get(productionTrackingId);

        Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);

        final boolean registerQuantityInProduct = order.getBooleanField(OrderFieldsPC.REGISTER_QUANTITY_IN_PRODUCT);
        final boolean registerQuantityOutProduct = order.getBooleanField(OrderFieldsPC.REGISTER_QUANTITY_OUT_PRODUCT);

        OperationProductsExtractor.TrackingOperationProducts operationProducts = operationProductsExtractor
                .getProductsByModelName(productionTracking);

        if (registerQuantityInProduct) {
            updateInProducts(productionTracking, operationProducts);
        }

        if (registerQuantityOutProduct) {
            updateOutProducts(productionTracking, operationProducts);
        }
        productionTracking = productionTracking.getDataDefinition().save(productionTracking);
    }

    private void updateOutProducts(final Entity productionTracking,
            final OperationProductsExtractor.TrackingOperationProducts operationProducts) {
        List<Entity> outputs = Collections.emptyList();
        List<Entity> productionTrackingOutputs = Collections.emptyList();
        outputs = operationProducts.getOutputComponents();
        productionTrackingOutputs = productionTracking
                .getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_OUT_COMPONENTS);

        Map<Long, Entity> outputsMap = outputs.stream().collect(
                Collectors.toMap(x -> x.getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCT).getId(), x -> x));
        Map<Long, Entity> productionTrackingOutputsMap = productionTrackingOutputs.stream().collect(
                Collectors.toMap(x -> x.getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCT).getId(), x -> x));

        Set<Long> newEntries = Sets.difference(outputsMap.keySet(), productionTrackingOutputsMap.keySet());
        Set<Long> removedEntries = Sets.difference(productionTrackingOutputsMap.keySet(), outputsMap.keySet());

        Set<Long> toUpdateEntries = Sets.intersection(outputsMap.keySet(), productionTrackingOutputsMap.keySet());

        List<Entity> updatedProductionTrackingOutputs = Lists.newArrayList();
        for (Long newEntry : newEntries) {
            updatedProductionTrackingOutputs.add(outputsMap.get(newEntry));
        }

        if (!newEntries.isEmpty() || !removedEntries.isEmpty()) {
            for (Long e : toUpdateEntries) {
                Entity toUpdateEntry = productionTrackingOutputsMap.get(e);
                updatedProductionTrackingOutputs.add(toUpdateEntry);
            }

            productionTracking.setField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_OUT_COMPONENTS,
                    updatedProductionTrackingOutputs);
        }
    }

    private void updateInProducts(final Entity productionTracking,
            final OperationProductsExtractor.TrackingOperationProducts operationProducts) {
        List<Entity> inputs = Collections.emptyList();
        List<Entity> productionTrackingInputs = Collections.emptyList();
        inputs = operationProducts.getInputComponents();
        productionTrackingInputs = productionTracking
                .getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_IN_COMPONENTS);

        Map<Long, Entity> inputsMap = inputs.stream().collect(
                Collectors.toMap(x -> x.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT).getId(), x -> x));
        Map<Long, Entity> productionTrackingInputsMap = productionTrackingInputs.stream().collect(
                Collectors.toMap(x -> x.getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCT).getId(), x -> x));

        Set<Long> newEntries = Sets.difference(inputsMap.keySet(), productionTrackingInputsMap.keySet());
        Set<Long> toUpdateEntries = Sets.intersection(inputsMap.keySet(), productionTrackingInputsMap.keySet());
        Set<Long> removedEntries = Sets.difference(productionTrackingInputsMap.keySet(), inputsMap.keySet());

        List<Entity> updatedProductionTrackingInputs = Lists.newArrayList();

        for (Long newEntry : newEntries) {
            updatedProductionTrackingInputs.add(inputsMap.get(newEntry));
        }
        if (!newEntries.isEmpty() || !removedEntries.isEmpty()) {
            for (Long e : toUpdateEntries) {
                Entity toUpdateEntry = productionTrackingInputsMap.get(e);
                updatedProductionTrackingInputs.add(toUpdateEntry);
            }
            productionTracking.setField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_IN_COMPONENTS,
                    updatedProductionTrackingInputs);
        }
    }
}
