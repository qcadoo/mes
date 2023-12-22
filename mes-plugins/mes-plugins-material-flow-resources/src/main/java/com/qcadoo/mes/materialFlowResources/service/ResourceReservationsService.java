package com.qcadoo.mes.materialFlowResources.service;

import java.math.BigDecimal;
import java.util.Map;

import com.qcadoo.model.api.NumberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.PositionFields;
import com.qcadoo.mes.materialFlowResources.constants.ResourceFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

@Service
public class ResourceReservationsService {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    void updateResourceQuantites(Map<String, Object> params, BigDecimal quantityToAdd) {
        if (params.get("resource_id") != null) {
            params.put("quantity_to_add", quantityToAdd);
            String query = "UPDATE materialflowresources_resource SET reservedquantity = reservedquantity + :quantity_to_add, "
                    + "availablequantity = availablequantity - :quantity_to_add WHERE id = :resource_id";
            jdbcTemplate.update(query, params);
        }
    }

    public void updateResourceQuantites(Entity position, BigDecimal quantityToAdd) {
        Entity resource = position.getBelongsToField(PositionFields.RESOURCE);
        if (resource != null) {
            resource = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                    MaterialFlowResourcesConstants.MODEL_RESOURCE).get(resource.getId());
            if (resource != null) {
                BigDecimal reservedQuantity = resource.getDecimalField(ResourceFields.RESERVED_QUANTITY).add(quantityToAdd);
                BigDecimal quantity = resource.getDecimalField(ResourceFields.QUANTITY);
                resource.setField(ResourceFields.AVAILABLE_QUANTITY, quantity.subtract(reservedQuantity));
                resource.setField(ResourceFields.RESERVED_QUANTITY, reservedQuantity);
                resource.getDataDefinition().save(resource);
            }
        }
    }

    public void updateResourceQuantitiesOnRemoveReservation(Entity resource, BigDecimal reservationQuantity) {
        if(resource == null) {
            return;
        }
        BigDecimal reservedQuantity = resource.getDecimalField(ResourceFields.RESERVED_QUANTITY).subtract(reservationQuantity, numberService.getMathContext());
        BigDecimal quantity = resource.getDecimalField(ResourceFields.QUANTITY);
        resource.setField(ResourceFields.AVAILABLE_QUANTITY, quantity.add(reservedQuantity));
        resource.setField(ResourceFields.RESERVED_QUANTITY, reservedQuantity);
        resource.getDataDefinition().save(resource);
    }

}
