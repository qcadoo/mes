package com.qcadoo.mes.materialFlowResources;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.materialFlowResources.constants.DocumentType;
import com.qcadoo.mes.materialFlowResources.constants.ParameterFieldsMFR;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinitionService;

@Service
public class ReservationsService {

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    private final static String L_QUANTITY = "quantity";

    public boolean reservationsEnabled() {
        return parameterService.getParameter().getBelongsToField(ParameterFieldsMFR.DOCUMENT_POSITION_PARAMETERS)
                .getBooleanField(ParameterFieldsMFR.DRAFT_MAKES_RESERVATION);
    }

    public void createReservation(Map<String, Object> params) {
        if (reservationsEnabled(params)) {
            String query = "INSERT INTO materialflowresources_reservation (location_id, product_id, quantity, position_id) "
                    + "VALUES ((SELECT locationfrom_id FROM materialflowresources_document WHERE id=:document_id), :product_id, :quantity, :id)";

            jdbcTemplate.update(query, params);
            updateResourceStock(params, BigDecimalUtils.convertNullToZero(params.get(L_QUANTITY)));
        }
    }

    public void updateReservation(Map<String, Object> params) {
        if (reservationsEnabled(params)) {
            String queryForOldQuantity = "SELECT reservedQuantity FROM materialflowresources_resourcestock WHERE product_id = :product_id AND "
                    + "location_id = (SELECT locationfrom_id FROM materialflowresources_document WHERE id=:document_id)";
            BigDecimal oldQuantity = jdbcTemplate.queryForObject(queryForOldQuantity, params, BigDecimal.class);
            BigDecimal newQuantity = BigDecimalUtils.convertNullToZero(params.get(L_QUANTITY));
            BigDecimal quantityToAdd = newQuantity.subtract(oldQuantity);
            String query = "UPDATE materialflowresources_reservation SET "
                    + "location_id = (SELECT locationfrom_id FROM materialflowresources_document WHERE id=:document_id), "
                    + "product_id = :product_id, quantity = :quantity WHERE position_id = :id";

            jdbcTemplate.update(query, params);
            updateResourceStock(params, quantityToAdd);
        }
    }

    public void deleteReservation(Map<String, Object> params) {
        if (reservationsEnabled(params)) {
            String query = "DELETE FROM materialflowresources_reservation WHERE position_id = :id";
            jdbcTemplate.update(query, params);
            updateResourceStock(params, (BigDecimalUtils.convertNullToZero(params.get(L_QUANTITY))).negate());
        }
    }

    public Boolean reservationsEnabled(Map<String, Object> params) {
        String query = "SELECT draftmakesreservation FROM materialflowresources_documentpositionparameters LIMIT 1";
        Boolean enabled = jdbcTemplate.queryForObject(query, new HashMap<String, Object>() {
        }, Boolean.class);
        String queryForDocumentType = "SELECT type FROM materialflowresources_document WHERE id = :document_id";
        String type = jdbcTemplate.queryForObject(queryForDocumentType, params, String.class);
        return enabled && DocumentType.isOutbound(type);
    }

    public void updateResourceStock(Map<String, Object> params, BigDecimal quantityToAdd) {
        params.put("quantity_to_add", quantityToAdd);
        String query = "UPDATE materialflowresources_resourcestock SET reservedquantity = reservedquantity + :quantity_to_add, "
                + "availablequantity = availablequantity - :quantity_to_add WHERE product_id = :product_id AND "
                + "location_id = (SELECT locationfrom_id FROM materialflowresources_document WHERE id=:document_id)";
        jdbcTemplate.update(query, params);

    }
}
