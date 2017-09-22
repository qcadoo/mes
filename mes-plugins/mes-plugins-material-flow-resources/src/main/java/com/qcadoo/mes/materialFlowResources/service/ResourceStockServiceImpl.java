package com.qcadoo.mes.materialFlowResources.service;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.ResourceFields;
import com.qcadoo.mes.materialFlowResources.constants.ResourceStockFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

@Service
public class ResourceStockServiceImpl implements ResourceStockService {

    private static final String RESOURCE_STOCK_QUERY = "SELECT stock FROM #materialFlowResources_resourceStockDto stock WHERE location_id = :locationId AND product_id = :productId";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    public void createResourceStock(final Entity resource) {
        Entity product = resource.getBelongsToField(ResourceFields.PRODUCT);
        Entity location = resource.getBelongsToField(ResourceFields.LOCATION);
        Optional<Entity> maybeStock = getResourceStockForProductAndLocation(product, location);
        if (!maybeStock.isPresent()) {
            DataDefinition resourceStockDD = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                    MaterialFlowResourcesConstants.MODEL_RESOURCE_STOCK);
            Entity stock = resourceStockDD.create();
            stock.setField(ResourceStockFields.LOCATION, location);
            stock.setField(ResourceStockFields.PRODUCT, product);
            resourceStockDD.save(stock);
        }
    }

    @Override
    public BigDecimal getResourceStockAvailableQuantity(final Entity product, final Entity location) {
        BigDecimal availableQuantity = BigDecimal.ZERO;
        Optional<Entity> resourceStock = getResourceStockForProductAndLocation(product, location);
        if (resourceStock.isPresent()) {
            availableQuantity = resourceStock.get().getDecimalField(ResourceStockFields.AVAILABLE_QUANTITY);
        }
        return availableQuantity;
    }

    @Override
    public BigDecimal getResourceStockQuantity(final Entity product, final Entity location) {
        BigDecimal quantity = BigDecimal.ZERO;
        Optional<Entity> resourceStock = getResourceStockForProductAndLocation(product, location);
        if (resourceStock.isPresent()) {
            quantity = resourceStock.get().getDecimalField(ResourceStockFields.QUANTITY);
        }
        return quantity;
    }

    private Optional<Entity> getResourceStockForProductAndLocation(Entity product, Entity location) {
        DataDefinition resourceStockDtoDD = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_RESOURCE_STOCK_DTO);
        Entity existingResourceStock = resourceStockDtoDD.find(RESOURCE_STOCK_QUERY)
                .setParameter("locationId", location.getId().intValue()).setParameter("productId", product.getId().intValue())
                .setMaxResults(1).uniqueResult();
        return Optional.ofNullable(existingResourceStock);
    }
}
