package com.qcadoo.mes.materialFlowResources.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.materialFlow.constants.LocationFields;
import com.qcadoo.mes.materialFlowResources.constants.DocumentFields;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.PositionFields;
import com.qcadoo.mes.materialFlowResources.constants.ResourceFields;
import com.qcadoo.mes.materialFlowResources.constants.ResourceStockFields;
import com.qcadoo.mes.materialFlowResources.dto.ResourceStockDto;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

@Service
public class ResourceStockServiceImpl implements ResourceStockService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public void createResourceStock(final Entity resource) {
        Entity product = resource.getBelongsToField(ResourceFields.PRODUCT);
        Entity location = resource.getBelongsToField(ResourceFields.LOCATION);
        Optional<ResourceStockDto> maybeStock = getResourceStockForProductAndLocation(product, location);
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
        Optional<ResourceStockDto> resourceStock = getResourceStockForProductAndLocation(product, location);
        if (resourceStock.isPresent()) {
            availableQuantity = resourceStock.get().getAvailableQuantity();
        }
        return availableQuantity;
    }

    @Override
    public BigDecimal getResourceStockQuantity(final Entity product, final Entity location) {
        BigDecimal quantity = BigDecimal.ZERO;
        Optional<ResourceStockDto> resourceStock = getResourceStockForProductAndLocation(product, location);
        if (resourceStock.isPresent()) {
            quantity = resourceStock.get().getQuantity();
        }
        return quantity;
    }

    private Optional<ResourceStockDto> getResourceStockForProductAndLocation(Entity product, Entity location) {
        StringBuilder query = new StringBuilder();
        query.append("SELECT rs.* ");
        query.append("FROM materialflowresources_resourcestockdto rs ");
        query.append("WHERE rs.location_id = :locationId AND rs.product_id = :productId ");
        query.append("LIMIT 1");

        Map<String, Object> params = Maps.newHashMap();
        params.put("locationId", location.getId().intValue());
        params.put("productId", product.getId().intValue());
        List<ResourceStockDto> resourceStock = jdbcTemplate.query(query.toString(), params,
                BeanPropertyRowMapper.newInstance(ResourceStockDto.class));
        if (resourceStock.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(resourceStock.get(0));
        }
    }

    @Override
    public void checkResourcesStock(Entity document) {
        List<String> invalidProducts = new ArrayList<>();
        List<Entity> mergedPositions = mergePositionsBeforeValidation(document.getHasManyField(DocumentFields.POSITIONS));
        for (Entity position : mergedPositions) {
            if ((position.getDecimalField(PositionFields.QUANTITY)
                    .compareTo(getResourceStockAvailableQuantity(position.getBelongsToField(PositionFields.PRODUCT),
                            document.getBelongsToField(DocumentFields.LOCATION_FROM))) > 0)) {
                Entity product = position.getBelongsToField(PositionFields.PRODUCT);
                invalidProducts.add(product.getStringField(ProductFields.NUMBER));
            }
        }
        if (!invalidProducts.isEmpty()) {
            addDocumentErrors(document, invalidProducts);
        }
    }

    private List<Entity> mergePositionsBeforeValidation(final List<Entity> positions) {
        List<Entity> mergedPositions = Lists.newArrayList();
        for (Entity position : positions) {
            Optional<Entity> mergedPosition = mergedPositions.stream()
                    .filter(merged -> merged.getBelongsToField(PositionFields.PRODUCT).getId()
                            .equals(position.getBelongsToField(PositionFields.PRODUCT).getId()))
                    .findAny();
            if (mergedPosition.isPresent()) {
                mergedPosition.get().setField(PositionFields.QUANTITY, mergedPosition.get()
                        .getDecimalField(PositionFields.QUANTITY).add(position.getDecimalField(PositionFields.QUANTITY)));
            } else {
                mergedPositions.add(position.copy());
            }
        }
        return mergedPositions;
    }

    private void addDocumentErrors(Entity document, List<String> invalidProducts) {
        String productNumbers = invalidProducts.stream().collect(Collectors.joining(", ")) + " ";
        Entity locationFrom = document.getBelongsToField(DocumentFields.LOCATION_FROM);
        String locationNumber = locationFrom == null ? "" : locationFrom.getStringField(LocationFields.NUMBER);
        document.addGlobalError("materialFlow.error.position.quantity.notEnoughResources", false, productNumbers, locationNumber);
    }
}
