package com.qcadoo.mes.materialFlowResources.hooks;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialFlowResources.constants.ReservationFields;
import com.qcadoo.mes.materialFlowResources.service.ResourceReservationsService;
import com.qcadoo.mes.materialFlowResources.service.ResourceStockService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class ReservationHooks {

    @Autowired
    private ResourceStockService resourceStockService;

    @Autowired
    private ResourceReservationsService resourceReservationsService;

    public boolean onDelete(DataDefinition reservationDD, Entity reservation) {
        resourceStockService.updateResourceStock(reservation.getBelongsToField(ReservationFields.PRODUCT),
                reservation.getBelongsToField(ReservationFields.LOCATION),
                reservation.getDecimalField(ReservationFields.QUANTITY).negate());
        return true;
    }

    public void onSave(DataDefinition reservationDD, Entity reservation) {
        Entity product = reservation.getBelongsToField(ReservationFields.PRODUCT);
        Entity location = reservation.getBelongsToField(ReservationFields.LOCATION);
        Entity newResource = reservation.getBelongsToField(ReservationFields.RESOURCE);
        Entity oldResource = null;
        Entity oldReservation = null;
        BigDecimal newQuantity = reservation.getDecimalField(ReservationFields.QUANTITY);
        BigDecimal oldQuantity = BigDecimal.ZERO;
        if (reservation.getId() != null) {
            oldReservation = reservationDD.get(reservation.getId());
            oldQuantity = oldReservation.getDecimalField(ReservationFields.QUANTITY);
            oldResource = oldReservation.getBelongsToField(ReservationFields.RESOURCE);
        }
        BigDecimal quantityToAdd = newQuantity.subtract(oldQuantity);
        resourceStockService.updateResourceStock(product, location, quantityToAdd);

        if (oldResource != null && newResource != null) {
            if (oldResource.getId().compareTo(newResource.getId()) != 0) {
                resourceReservationsService.updateResourceQuantites(reservation, newQuantity);
                resourceReservationsService.updateResourceQuantites(oldReservation, oldQuantity.negate());
            } else {
                resourceReservationsService.updateResourceQuantites(reservation, quantityToAdd);
            }
        } else if (oldResource == null && newResource != null) {
            resourceReservationsService.updateResourceQuantites(reservation, newQuantity);
        } else if (oldResource != null) {
            resourceReservationsService.updateResourceQuantites(oldReservation, oldQuantity.negate());
        }
    }

    public void onCreate(DataDefinition reservationDD, Entity reservation) {
    }

    public void onCopy(DataDefinition reservationDD, Entity reservation) {
        reservation.setField(ReservationFields.RESOURCE, null);
    }
}
