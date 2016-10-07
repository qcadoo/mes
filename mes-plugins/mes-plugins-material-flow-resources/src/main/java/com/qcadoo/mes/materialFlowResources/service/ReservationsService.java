package com.qcadoo.mes.materialFlowResources.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.mes.materialFlowResources.constants.DocumentFields;
import com.qcadoo.mes.materialFlowResources.constants.DocumentState;
import com.qcadoo.mes.materialFlowResources.constants.DocumentType;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.ParameterFieldsMFR;
import com.qcadoo.mes.materialFlowResources.constants.PositionFields;
import com.qcadoo.mes.materialFlowResources.constants.ReservationFields;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.tenant.api.MultiTenantCallback;
import com.qcadoo.tenant.api.MultiTenantService;

@Service
public class ReservationsService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private ResourceStockService resourceStockService;

    @Autowired
    private MultiTenantService multiTenantService;

    private final static String L_QUANTITY = "quantity";

    public void cleanReservationsTrigger() {
        multiTenantService.doInMultiTenantContext(new MultiTenantCallback() {

            @Override
            public void invoke() {
                cleanReservations();
            }

        });
    }

    public void cleanReservations() {
        String sql = "DELETE FROM materialflowresources_reservation WHERE quantity = 0";
        jdbcTemplate.update(sql, Maps.newHashMap());
    }

    public boolean reservationsEnabledForDocumentPositions() {
        Entity documentPositionParameters = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_DOCUMENT_POSITION_PARAMETERS).find().setMaxResults(1).uniqueResult();
        if (documentPositionParameters != null) {
            return documentPositionParameters.getBooleanField(ParameterFieldsMFR.DRAFT_MAKES_RESERVATION);
        }
        return false;
    }

    public boolean reservationsEnabledForDocumentPositions(final Entity document) {
        if (document == null) {
            return ReservationsService.this.reservationsEnabledForDocumentPositions();
        }
        String type = document.getStringField(DocumentFields.TYPE);
        return ReservationsService.this.reservationsEnabledForDocumentPositions() && DocumentType.isOutbound(type);
    }

    /**
     * Creates new reservation for position with given id, using specified parameters, and updates resource stock. Uses
     * jdbcTemplate.
     *
     * Warning! If logic in this method is changed, it should also be applied to corresponding framework method.
     *
     * @param params
     *            map containing keys: id (position id), quantity, product_id, document_id
     * @see ReservationsService#createReservationFromDocumentPosition(Entity)
     */
    public void createReservationFromDocumentPosition(Map<String, Object> params) {
        if (!ReservationsService.this.reservationsEnabledForDocumentPositions(params)) {
            return;
        }
        String query = "INSERT INTO materialflowresources_reservation (location_id, product_id, quantity, position_id) "
                + "VALUES ((SELECT locationfrom_id FROM materialflowresources_document WHERE id=:document_id), :product_id, :quantity, :id)";

        jdbcTemplate.update(query, params);
        resourceStockService.updateResourceStock(params, BigDecimalUtils.convertNullToZero(params.get(L_QUANTITY)));
    }

    /**
     * Creates new reservation for position and updates resource stock. Uses framework.
     *
     * Warning! If logic in this method is changed, it should also be applied to corresponding jdbc method.
     *
     * @param position
     * @see ReservationsService#createReservationFromDocumentPosition(Map)
     */
    public void createReservationFromDocumentPosition(final Entity position) {
        if (!reservationsEnabledForDocumentPositions(position.getBelongsToField(PositionFields.DOCUMENT))) {
            return;
        }
        Entity document = position.getBelongsToField(PositionFields.DOCUMENT);
        if (DocumentState.of(document).equals(DocumentState.ACCEPTED)) {
            return;
        }
        Entity reservation = dataDefinitionService
                .get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_RESERVATION).create();

        reservation.setField(ReservationFields.LOCATION, document.getBelongsToField(DocumentFields.LOCATION_FROM));
        reservation.setField(ReservationFields.POSITION, position);
        reservation.setField(ReservationFields.PRODUCT, position.getBelongsToField(PositionFields.PRODUCT));
        reservation.setField(ReservationFields.QUANTITY, position.getDecimalField(PositionFields.QUANTITY));

        reservation = reservation.getDataDefinition().save(reservation);
        // updateResourceStock(position, position.getDecimalField(PositionFields.QUANTITY));
        position.setField(PositionFields.RESERVATIONS, Lists.newArrayList(reservation));
    }

    /**
     * Updates existing reservation for position with given id, using specified parameters, and updates resource stock. Uses
     * jdbcTemplate.
     *
     * Warning! If logic in this method is changed, it should also be applied to corresponding framework method.
     *
     * @param params
     *            map containing keys: id (position id), quantity, product_id, document_id
     * @see ReservationsService#updateReservationFromDocumentPosition(Entity)
     */
    public void updateReservationFromDocumentPosition(Map<String, Object> params) {
        if (!ReservationsService.this.reservationsEnabledForDocumentPositions(params)) {
            return;
        }

        if (params.get("id") != null) {
            String queryForOld = "SELECT product_id, quantity FROM materialflowresources_position WHERE id = :id";
            Map<String, Object> oldPosition = jdbcTemplate.query(queryForOld, params,
                    new ResultSetExtractor<Map<String, Object>>() {

                        @Override
                        public Map<String, Object> extractData(ResultSet rs) throws SQLException, DataAccessException {
                            Map<String, Object> result = Maps.newHashMap();
                            if (rs.next()) {
                                result.put("product_id", rs.getLong("product_id"));
                                result.put("quantity", rs.getBigDecimal("quantity"));
                            }
                            return result;
                        }
                    });
            Long oldProductId = (Long) oldPosition.get("product_id");
            BigDecimal oldPositionQuantity = (BigDecimal) oldPosition.get("quantity");

            BigDecimal newQuantity = BigDecimalUtils.convertNullToZero(params.get(L_QUANTITY));
            BigDecimal quantityToAdd = newQuantity.subtract(oldPositionQuantity);
            String query = "UPDATE materialflowresources_reservation SET "
                    + "location_id = (SELECT locationfrom_id FROM materialflowresources_document WHERE id=:document_id), "
                    + "product_id = :product_id, quantity = :quantity WHERE position_id = :id";

            jdbcTemplate.update(query, params);

            if (oldProductId.compareTo((Long) params.get("product_id")) != 0) {
                resourceStockService.updateResourceStock(params, newQuantity);
                Map<String, Object> paramsForOld = Maps.newHashMap(params);
                paramsForOld.put("product_id", oldProductId);
                resourceStockService.updateResourceStock(paramsForOld, oldPositionQuantity.negate());
            } else {
                resourceStockService.updateResourceStock(params, quantityToAdd);
            }
        }

    }

    /**
     * Updates reservation for position and updates resource stock. Uses framework.
     *
     * Warning! If logic in this method is changed, it should also be applied to corresponding jdbc method.
     *
     * @param position
     * @see ReservationsService#updateReservationFromDocumentPosition(Map)
     */
    public void updateReservationFromDocumentPosition(final Entity position) {
        if (!reservationsEnabledForDocumentPositions(position.getBelongsToField(PositionFields.DOCUMENT))) {
            return;
        }
        Entity product = position.getBelongsToField(PositionFields.PRODUCT);
        Entity location = position.getBelongsToField(PositionFields.DOCUMENT).getBelongsToField(DocumentFields.LOCATION_FROM);
        BigDecimal newQuantity = position.getDecimalField(PositionFields.QUANTITY);

        Entity existingReservation = getReservationForPosition(position);
        if (existingReservation != null) {
            existingReservation.setField(ReservationFields.QUANTITY, newQuantity);
            existingReservation.setField(ReservationFields.PRODUCT, product);
            existingReservation.setField(ReservationFields.LOCATION, location);
            existingReservation.getDataDefinition().save(existingReservation);
        }
    }

    /**
     * Deletes reservation for position with given id and updates resource stock. Uses jdbcTemplate.
     *
     * Warning! If logic in this method is changed, it should also be applied to corresponding framework method.
     *
     * @param params
     *            map containing keys: id (position id), quantity, product_id, document_id
     * @see ReservationsService#deleteReservationFromDocumentPosition(Entity)
     */
    public void deleteReservationFromDocumentPosition(Map<String, Object> params) {
        if (!reservationsEnabledForDocumentPositions(params)) {
            return;
        }
        String query = "DELETE FROM materialflowresources_reservation WHERE position_id = :id";
        jdbcTemplate.update(query, params);
        resourceStockService.updateResourceStock(params, (BigDecimalUtils.convertNullToZero(params.get(L_QUANTITY))).negate());
    }

    /**
     * Deletes reservation for position and updates resource stock. Uses framework.
     *
     * Warning! If logic in this method is changed, it should also be applied to corresponding jdbc method.
     *
     * @param position
     * @see ReservationsService#deleteReservationFromDocumentPosition(Map)
     */
    public void deleteReservationFromDocumentPosition(final Entity position) {
        if (!reservationsEnabledForDocumentPositions(position.getBelongsToField(PositionFields.DOCUMENT))) {
            return;
        }

        Entity reservation = getReservationForPosition(position);
        if (reservation != null) {
            reservation.getDataDefinition().delete(reservation.getId());
        }
    }

    public Boolean reservationsEnabledForDocumentPositions(Map<String, Object> params) {
        String query = "SELECT draftmakesreservation FROM materialflowresources_documentpositionparameters LIMIT 1";
        Boolean enabled = jdbcTemplate.queryForObject(query, new HashMap<String, Object>() {
        }, Boolean.class);
        String queryForDocumentType = "SELECT type FROM materialflowresources_document WHERE id = :document_id";
        String type = jdbcTemplate.queryForObject(queryForDocumentType, params, String.class);
        return enabled && DocumentType.isOutbound(type);
    }

    public Entity getReservationForPosition(final Entity position) {
        if (position.getId() == null) {
            return null;
        }
        return dataDefinitionService
                .get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_RESERVATION).find()
                .add(SearchRestrictions.belongsTo(ReservationFields.POSITION, position)).setMaxResults(1).uniqueResult();
    }
}
