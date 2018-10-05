package com.qcadoo.mes.productFlowThruDivision.validators;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.qcadoo.mes.materialFlow.constants.MaterialFlowConstants;
import com.qcadoo.mes.materialFlowResources.MaterialFlowResourcesService;
import com.qcadoo.mes.materialFlowResources.helpers.NotEnoughResourcesErrorMessageCopyToEntityHelper;
import com.qcadoo.mes.materialFlowResources.helpers.NotEnoughResourcesErrorMessageHolder;
import com.qcadoo.mes.materialFlowResources.helpers.NotEnoughResourcesErrorMessageHolderFactory;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductInComponentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ProductionTrackingValidatorsPFTD {

    @Autowired
    private MaterialFlowResourcesService materialFlowResourcesService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NotEnoughResourcesErrorMessageHolderFactory notEnoughResourcesErrorMessageHolderFactory;

    public boolean checkResources(Entity productionTracking, Multimap<Long, Entity> groupedRecordInProducts,
            List<Entity> recordOutProducts) {

        boolean enoughResources = true;

        NotEnoughResourcesErrorMessageHolder errorMessageHolder = notEnoughResourcesErrorMessageHolderFactory.create();

        Map<Entity, Map<Entity, BigDecimal>> productsNotInStock = findProductsNotInStock(groupedRecordInProducts);

        for (Entity warehouseFrom : productsNotInStock.keySet()) {
            for (Entity productNotInStock : productsNotInStock.get(warehouseFrom).keySet()) {
                boolean productInTrackingOperationProductOut = false;

                for (Entity recordOutProduct : recordOutProducts) {
                    if (productNotInStock.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT).getId().equals(
                            recordOutProduct.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT).getId())) {
                        if (Objects.isNull(
                                recordOutProduct.getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY))) {
                            BigDecimal quantity = productNotInStock
                                    .getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY)
                                    .subtract(productsNotInStock.get(warehouseFrom).get(productNotInStock));

                            errorMessageHolder.addErrorEntry(
                                    productNotInStock.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT),
                                    quantity);

                            enoughResources = false;
                        } else if (productNotInStock.getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY)
                                .compareTo(
                                        recordOutProduct.getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY)
                                                .add(productsNotInStock.get(warehouseFrom).get(productNotInStock))) > 0) {

                            BigDecimal quantity = productNotInStock
                                    .getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY)
                                    .subtract(productsNotInStock.get(warehouseFrom).get(productNotInStock))
                                    .subtract(recordOutProduct
                                            .getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY));

                            errorMessageHolder.addErrorEntry(
                                    productNotInStock.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT),
                                    quantity);

                            enoughResources = false;
                        }

                        productInTrackingOperationProductOut = true;

                        break;
                    }
                }

                if (!productInTrackingOperationProductOut) {
                    BigDecimal quantity = productNotInStock
                            .getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY)
                            .subtract(productsNotInStock.get(warehouseFrom).get(productNotInStock));

                    errorMessageHolder.addErrorEntry(
                            productNotInStock.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT), quantity);

                    enoughResources = false;
                }
            }

            if (!enoughResources) {
                NotEnoughResourcesErrorMessageCopyToEntityHelper.addError(productionTracking, warehouseFrom, errorMessageHolder);

                return false;
            }
        }

        return true;
    }

    private Map<Entity, Map<Entity, BigDecimal>> findProductsNotInStock(Multimap<Long, Entity> groupedRecordInProducts) {
        DataDefinition locationDD = dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER,
                MaterialFlowConstants.MODEL_LOCATION);

        Map<Entity, Map<Entity, BigDecimal>> productsNotInStock = Maps.newHashMap();

        for (Long warehouseId : groupedRecordInProducts.keySet()) {
            Map<Entity, BigDecimal> productsNotInStockQuantities = Maps.newHashMap();

            Entity warehouseFrom = locationDD.get(warehouseId);

            Map<Long, BigDecimal> stockMap = getStock(groupedRecordInProducts, warehouseId, warehouseFrom);

            for (Entity recordInProduct : groupedRecordInProducts.get(warehouseId)) {
                BigDecimal productStock = stockMap
                        .get(recordInProduct.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT).getId());

                if (Objects.isNull(productStock)) {
                    productsNotInStockQuantities.put(recordInProduct, BigDecimal.ZERO);
                } else if (productStock.compareTo(
                        recordInProduct.getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY)) < 0) {
                    productsNotInStockQuantities.put(recordInProduct, productStock);
                }
            }

            if (!productsNotInStockQuantities.isEmpty()) {
                productsNotInStock.put(warehouseFrom, productsNotInStockQuantities);
            }
        }

        return productsNotInStock;
    }

    private Map<Long, BigDecimal> getStock(Multimap<Long, Entity> groupedRecordInProducts, Long warehouseId,
            Entity warehouseFrom) {
        return materialFlowResourcesService.getQuantitiesForProductsAndLocation(groupedRecordInProducts.get(warehouseId).stream()
                .map(p -> p.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT)).collect(Collectors.toList()),
                warehouseFrom);
    }

}
