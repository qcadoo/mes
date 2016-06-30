package com.qcadoo.mes.materialFlowResources.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.ParameterService;
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

@Service
public class ReservationsServiceImpl implements ReservationsService {

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private ResourceStockService resourceStockService;

    private final static String L_QUANTITY = "quantity";

    @Override
    public boolean reservationsEnabled() {
        return parameterService.getParameter().getBelongsToField(ParameterFieldsMFR.DOCUMENT_POSITION_PARAMETERS)
                .getBooleanField(ParameterFieldsMFR.DRAFT_MAKES_RESERVATION);
    }

    @Override
    public boolean reservationsEnabled(final Entity document) {
        if (document == null) {
            return reservationsEnabled();
        }
        String type = document.getStringField(DocumentFields.TYPE);
        return reservationsEnabled() && DocumentType.isOutbound(type);
    }

    /**
     * Creates new reservation for position with given id, using specified parameters, and updates resource stock. Uses
     * jdbcTemplate.
     *
     * Warning! If logic in this method is changed, it should also be applied to corresponding framework method.
     *
     * @param params
     *            map containing keys: id (position id), quantity, product_id, document_id
     * @see ReservationsServiceImpl#createReservation(Entity)
     */
    @Override
    public void createReservation(Map<String, Object> params) {
        if (!reservationsEnabled(params)) {
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
     * @see ReservationsServiceImpl#createReservation(Map)
     */
    @Override
    public void createReservation(final Entity position) {
        if (!reservationsEnabled(position.getBelongsToField(PositionFields.DOCUMENT))) {
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
     * @see ReservationsServiceImpl#updateReservation(Entity)
     */
    @Override
    public void updateReservation(Map<String, Object> params) {
        if (!reservationsEnabled(params)) {
            return;
        }
        String queryForOldQuantity = "SELECT reservedQuantity FROM materialflowresources_resourcestock WHERE product_id = :product_id AND "
                + "location_id = (SELECT locationfrom_id FROM materialflowresources_document WHERE id=:document_id)";
        BigDecimal oldQuantity = jdbcTemplate.queryForObject(queryForOldQuantity, params, BigDecimal.class);
        BigDecimal newQuantity = BigDecimalUtils.convertNullToZero(params.get(L_QUANTITY));
        BigDecimal quantityToAdd = newQuantity.subtract(oldQuantity);
        String query = "UPDATE materialflowresources_reservation SET "
                + "location_id = (SELECT locationfrom_id FROM materialflowresources_document WHERE id=:document_id), "
                + "product_id = :product_id, quantity = :quantity WHERE position_id = :id";

        jdbcTemplate.update(query, params);
        resourceStockService.updateResourceStock(params, quantityToAdd);

    }

    /**
     * Updates reservation for position and updates resource stock. Uses framework.
     *
     * Warning! If logic in this method is changed, it should also be applied to corresponding jdbc method.
     *
     * @param position
     * @see ReservationsServiceImpl#updateReservation(Map)
     */
    @Override
    public void updateReservation(final Entity position) {
        if (!reservationsEnabled(position.getBelongsToField(PositionFields.DOCUMENT))) {
            return;
        }
        Entity product = position.getBelongsToField(PositionFields.PRODUCT);
        Entity location = position.getBelongsToField(PositionFields.DOCUMENT).getBelongsToField(DocumentFields.LOCATION_FROM);
        BigDecimal newQuantity = position.getDecimalField(PositionFields.QUANTITY);
        // Optional<Entity> maybeResourceStock = resourceStockService.getResourceStockForProductAndLocation(product, location);
        // if (maybeResourceStock.isPresent()) {
        // Entity resourceStock = maybeResourceStock.get();
        // BigDecimal oldQuantity = resourceStock.getDecimalField(ResourceStockFields.RESERVED_QUANTITY);
        // BigDecimal quantityToAdd = newQuantity.subtract(oldQuantity);

        Entity existingReservation = getReservationForPosition(position);
        if (existingReservation != null) {
            existingReservation.setField(ReservationFields.QUANTITY, newQuantity);
            existingReservation.setField(ReservationFields.PRODUCT, product);
            existingReservation.setField(ReservationFields.LOCATION, location);
            existingReservation.getDataDefinition().save(existingReservation);
            // updateResourceStock(position, quantityToAdd);
        }
        // }

    }

    /**
     * Deletes reservation for position with given id and updates resource stock. Uses jdbcTemplate.
     *
     * Warning! If logic in this method is changed, it should also be applied to corresponding framework method.
     *
     * @param params
     *            map containing keys: id (position id), quantity, product_id, document_id
     * @see ReservationsServiceImpl#deleteReservation(Entity)
     */
    @Override
    public void deleteReservation(Map<String, Object> params) {
        if (!reservationsEnabled(params)) {
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
     * @see ReservationsServiceImpl#deleteReservation(Map)
     */
    @Override
    public void deleteReservation(final Entity position) {
        if (!reservationsEnabled(position.getBelongsToField(PositionFields.DOCUMENT))) {
            return;
        }

        Entity reservation = getReservationForPosition(position);
        if (reservation != null) {
            // BigDecimal quantityToAdd = position.getDecimalField(PositionFields.QUANTITY).negate();
            reservation.getDataDefinition().delete(reservation.getId());
            // updateResourceStock(position, quantityToAdd);
        }

    }

    @Override
    public Boolean reservationsEnabled(Map<String, Object> params) {
        String query = "SELECT draftmakesreservation FROM materialflowresources_documentpositionparameters LIMIT 1";
        Boolean enabled = jdbcTemplate.queryForObject(query, new HashMap<String, Object>() {
        }, Boolean.class);
        String queryForDocumentType = "SELECT type FROM materialflowresources_document WHERE id = :document_id";
        String type = jdbcTemplate.queryForObject(queryForDocumentType, params, String.class);
        return enabled && DocumentType.isOutbound(type);
    }

    @Override
    public Entity getReservationForPosition(final Entity position) {
        if (position.getId() == null) {
            return null;
        }
        return dataDefinitionService
                .get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_RESERVATION).find()
                .add(SearchRestrictions.belongsTo(ReservationFields.POSITION, position)).setMaxResults(1).uniqueResult();
    }
}
