package com.qcadoo.mes.productionCounting;

import static com.qcadoo.model.api.search.SearchProjections.alias;
import static com.qcadoo.model.api.search.SearchProjections.field;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basicProductionCounting.constants.*;
import com.qcadoo.mes.productionCounting.constants.*;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.plugin.api.PluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;
import com.qcadoo.mes.basicProductionCounting.ProductionTrackingUpdateService;
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

    @Autowired
    private PluginManager pluginManager;

    @Override
    public void updateProductionTracking(final Long productionTrackingId) {
        Entity productionTracking = dataDefinitionService
                .get(ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_PRODUCTION_TRACKING)
                .get(productionTrackingId);

        if(productionTracking == null) {
            return;
        }

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
        Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);

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

        List<Entity> newProductionTrackingOutputs = productionTracking.getDataDefinition().get(productionTracking.getId())
                .getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_OUT_COMPONENTS);

        Entity toc = productionTracking.getBelongsToField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT);
        for (Entity productionTrackingOutput : newProductionTrackingOutputs) {

            SearchCriteriaBuilder scb = dataDefinitionService.get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER, BasicProductionCountingConstants.MODEL_PRODUCTION_COUNTING_QUANTITY)
                    .find()
                    .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.ORDER, order))
                    .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.PRODUCT, productionTrackingOutput.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT)))
                    .add(SearchRestrictions.eq(ProductionCountingQuantityFields.ROLE, ProductionCountingQuantityRole.PRODUCED.getStringValue()));

            if (Objects.nonNull(toc)) {
                scb.add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT, toc));
            }

            List<Entity> pcqs = scb.list().getEntities();

            List<Entity> attrs = Lists.newArrayList(productionTrackingOutput.getHasManyField(TrackingOperationProductOutComponentFields.PROD_OUT_RESOURCE_ATTR_VALS));

            for (Entity productionCountingQuantity : pcqs) {

                List<Entity> productionCountingAttributeValues = productionCountingQuantity
                        .getHasManyField(ProductionCountingQuantityFields.PRODUCTION_COUNTING_ATTRIBUTE_VALUES);

                for (Entity productionCountingAttributeValue : productionCountingAttributeValues) {
                    if(attributeExist(productionCountingAttributeValue, attrs)) {
                        continue;
                    }
                    Entity prodOutResourceAttrVal = dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                            ProductionCountingConstants.MODEL_PROD_OUT_RESOURCE_ATTR_VAL).create();
                    prodOutResourceAttrVal.setField(ProdOutResourceAttrValFields.ATTRIBUTE,
                            productionCountingAttributeValue.getField(ProductionCountingAttributeValueFields.ATTRIBUTE));
                    prodOutResourceAttrVal.setField(ProdOutResourceAttrValFields.ATTRIBUTE_VALUE,
                            productionCountingAttributeValue.getField(ProductionCountingAttributeValueFields.ATTRIBUTE_VALUE));
                    prodOutResourceAttrVal.setField(ProdOutResourceAttrValFields.VALUE,
                            productionCountingAttributeValue.getField(ProductionCountingAttributeValueFields.VALUE));

                    prodOutResourceAttrVal.setField(ProdOutResourceAttrValFields.TRACKING_OPERATION_PRODUCT_OUT_COMPONENT,
                            productionTrackingOutput);
                    prodOutResourceAttrVal.getDataDefinition().fastSave(prodOutResourceAttrVal);
                }


            }

        }

    }

    private boolean attributeExist(Entity productionCountingAttributeValue, List<Entity> attrs) {
        return attrs.stream().anyMatch(currentAttr ->
                currentAttr.getBelongsToField(ProdOutResourceAttrValFields.ATTRIBUTE).getId().equals(productionCountingAttributeValue.getBelongsToField(ProductionCountingAttributeValueFields.ATTRIBUTE).getId())
                        && currentAttr.getStringField(ProdOutResourceAttrValFields.VALUE).equals(productionCountingAttributeValue.getStringField(ProductionCountingAttributeValueFields.VALUE)));
    }

    private void updateInProducts(final Entity productionTracking,
                                  final OperationProductsExtractor.TrackingOperationProducts operationProducts) {
        Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);
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

        if (pluginManager.isPluginEnabled("productFlowThruDivision")) {
            List<Entity> newProductionTrackingInputs = productionTracking.getDataDefinition().get(productionTracking.getId())
                    .getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_IN_COMPONENTS);

            Entity toc = productionTracking.getBelongsToField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT);

            for (Entity productionTrackingInput : newProductionTrackingInputs) {

                SearchCriteriaBuilder scb = dataDefinitionService.get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER, BasicProductionCountingConstants.MODEL_PRODUCTION_COUNTING_QUANTITY)
                        .find()
                        .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.ORDER, order))
                        .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.PRODUCT, productionTrackingInput.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT)))
                        .add(SearchRestrictions.eq(ProductionCountingQuantityFields.ROLE, ProductionCountingQuantityRole.USED.getStringValue()))
                        .add(SearchRestrictions.eq(ProductionCountingQuantityFields.TYPE_OF_MATERIAL, ProductionCountingQuantityTypeOfMaterial.COMPONENT.getStringValue()));

                if (Objects.nonNull(toc)) {
                    scb.add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT, toc));
                }

                List<Entity> pcqs = scb.list().getEntities();

                List<Entity> resourceReservations = Lists.newArrayList(productionTrackingInput.getHasManyField("resourceReservations"));
                List<Long> orderProductResourceReservations = resourceReservations.stream().map(x -> x.getBelongsToField("orderProductResourceReservation").getId()).collect(Collectors.toList());

                for (Entity productionCountingQuantity : pcqs) {
                    List<Entity> orderProductResourceReservationsPCQ = productionCountingQuantity.getHasManyField("orderProductResourceReservations");
                    for (Entity orderProductResourceReservation : orderProductResourceReservationsPCQ) {
                        if (orderProductResourceReservations.contains(orderProductResourceReservation.getId())) {
                            continue;
                        }
                        Entity reservation = dataDefinitionService.get("productFlowThruDivision", "trackingProductResourceReservation").create();
                        reservation.setField("trackingOperationProductInComponent", productionTrackingInput);
                        reservation.setField("orderProductResourceReservation", orderProductResourceReservation);
                        reservation.setField("priority", orderProductResourceReservation.getIntegerField("priority"));
                        reservation.getDataDefinition().fastSave(reservation);
                    }
                }
            }
        }
    }
}
