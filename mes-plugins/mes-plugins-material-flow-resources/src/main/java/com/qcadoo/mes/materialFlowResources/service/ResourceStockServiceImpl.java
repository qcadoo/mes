package com.qcadoo.mes.materialFlowResources.service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialFlowResources.constants.DocumentFields;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.PositionFields;
import com.qcadoo.mes.materialFlowResources.constants.ResourceFields;
import com.qcadoo.mes.materialFlowResources.constants.ResourceStockFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class ResourceStockServiceImpl implements ResourceStockService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public void addResourceStock(final Entity resource) {

        Entity product = resource.getBelongsToField(ResourceFields.PRODUCT);
        Entity location = resource.getBelongsToField(ResourceFields.LOCATION);
        BigDecimal quantity = resource.getDecimalField(ResourceFields.QUANTITY);
        addResourceStock(product, location, quantity);

    }

    @Override
    public void addResourceStock(Entity product, Entity location, BigDecimal quantity) {
        Entity resourceStock;
        Optional<Entity> maybeStock = getResourceStockForProductAndLocation(product, location);
        if (maybeStock.isPresent()) {
            resourceStock = maybeStock.get();
            BigDecimal oldQuantity = resourceStock.getDecimalField(ResourceStockFields.QUANTITY);
            BigDecimal oldAvailableQuantity = resourceStock.getDecimalField(ResourceStockFields.AVAILABLE_QUANTITY);
            resourceStock.setField(ResourceStockFields.QUANTITY, oldQuantity.add(quantity));
            resourceStock.setField(ResourceStockFields.AVAILABLE_QUANTITY, oldAvailableQuantity.add(quantity));
        } else {
            resourceStock = createResourceStock(product, location, quantity);
        }
        getResourceStockDataDefinition().save(resourceStock);
    }

    @Override
    public void removeResourceStock(Entity resource) {
        Entity product = resource.getBelongsToField(ResourceFields.PRODUCT);
        Entity location = resource.getBelongsToField(ResourceFields.LOCATION);
        BigDecimal quantity = resource.getDecimalField(ResourceFields.QUANTITY);
        removeResourceStock(product, location, quantity);
    }

    @Override
    public void removeResourceStock(Entity product, Entity location, BigDecimal quantity) {
        Optional<Entity> maybeStock = getResourceStockForProductAndLocation(product, location);
        if (maybeStock.isPresent()) {
            Entity resourceStock = maybeStock.get();
            BigDecimal oldQuantity = resourceStock.getDecimalField(ResourceStockFields.QUANTITY);
            BigDecimal oldAvailableQuantity = resourceStock.getDecimalField(ResourceStockFields.AVAILABLE_QUANTITY);
            BigDecimal newQuantity = oldQuantity.subtract(quantity);
            BigDecimal newAvailableQuantity = oldAvailableQuantity.subtract(quantity);
            if (newQuantity.compareTo(BigDecimal.ZERO) > 0) {
                resourceStock.setField(ResourceStockFields.QUANTITY, newQuantity);
                resourceStock.setField(ResourceStockFields.AVAILABLE_QUANTITY, newAvailableQuantity);
                getResourceStockDataDefinition().save(resourceStock);
            } else {
                getResourceStockDataDefinition().delete(resourceStock.getId());
            }
        }
    }

    @Override
    public Optional<Entity> getResourceStockForProductAndLocation(Entity product, Entity location) {
        Entity existingResourceStock = getResourceStockDataDefinition().find()
                .add(SearchRestrictions.belongsTo(ResourceStockFields.LOCATION, location))
                .add(SearchRestrictions.belongsTo(ResourceStockFields.PRODUCT, product)).setMaxResults(1).uniqueResult();
        return Optional.ofNullable(existingResourceStock);
    }

    private Entity createResourceStock(final Entity product, final Entity location, final BigDecimal quantity) {
        Entity stock = getResourceStockDataDefinition().create();
        stock.setField(ResourceStockFields.LOCATION, location);
        stock.setField(ResourceStockFields.PRODUCT, product);
        stock.setField(ResourceStockFields.QUANTITY, quantity);
        stock.setField(ResourceStockFields.AVAILABLE_QUANTITY, quantity);
        stock.setField(ResourceStockFields.RESERVED_QUANTITY, BigDecimal.ZERO);
        return stock;
    }

    private DataDefinition getResourceStockDataDefinition() {
        return dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_RESOURCE_STOCK);
    }

    public void updateResourceStock(Map<String, Object> params, BigDecimal quantityToAdd) {
        params.put("quantity_to_add", quantityToAdd);
        String query = "UPDATE materialflowresources_resourcestock SET reservedquantity = reservedquantity + :quantity_to_add, "
                + "availablequantity = availablequantity - :quantity_to_add WHERE product_id = :product_id AND "
                + "location_id = (SELECT locationfrom_id FROM materialflowresources_document WHERE id=:document_id)";
        jdbcTemplate.update(query, params);
    }

    public void updateResourceStock(Entity position, BigDecimal quantityToAdd) {
        updateResourceStock(position.getBelongsToField(PositionFields.PRODUCT),
                position.getBelongsToField(PositionFields.DOCUMENT).getBelongsToField(DocumentFields.LOCATION_FROM),
                quantityToAdd);
    }

    public void updateResourceStock(Entity product, Entity location, BigDecimal quantityToAdd) {
        Optional<Entity> maybeResourceStock = getResourceStockForProductAndLocation(product, location);
        if (maybeResourceStock.isPresent()) {
            Entity resourceStock = maybeResourceStock.get();
            BigDecimal reservedQuantity = resourceStock.getDecimalField(ResourceStockFields.RESERVED_QUANTITY);
            BigDecimal availableQuantity = resourceStock.getDecimalField(ResourceStockFields.AVAILABLE_QUANTITY);
            resourceStock.setField(ResourceStockFields.AVAILABLE_QUANTITY, availableQuantity.subtract(quantityToAdd));
            resourceStock.setField(ResourceStockFields.RESERVED_QUANTITY, reservedQuantity.add(quantityToAdd));
            resourceStock.getDataDefinition().save(resourceStock);
        }

    }
}
