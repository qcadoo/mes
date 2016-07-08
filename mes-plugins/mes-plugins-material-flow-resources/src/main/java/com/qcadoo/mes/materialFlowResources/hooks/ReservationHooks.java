package com.qcadoo.mes.materialFlowResources.hooks;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialFlowResources.constants.ReservationFields;
import com.qcadoo.mes.materialFlowResources.service.ResourceStockService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class ReservationHooks {

    @Autowired
    private ResourceStockService resourceStockService;

    public boolean onDelete(DataDefinition reservationDD, Entity reservation) {
        resourceStockService.updateResourceStock(reservation.getBelongsToField(ReservationFields.PRODUCT),
                reservation.getBelongsToField(ReservationFields.LOCATION),
                reservation.getDecimalField(ReservationFields.QUANTITY).negate());
        return true;
    }

    public void onSave(DataDefinition reservationDD, Entity reservation) {
        Entity product = reservation.getBelongsToField(ReservationFields.PRODUCT);
        Entity location = reservation.getBelongsToField(ReservationFields.LOCATION);
        BigDecimal newQuantity = reservation.getDecimalField(ReservationFields.QUANTITY);
        BigDecimal oldQuantity = BigDecimal.ZERO;
        if (reservation.getId() != null) {
            oldQuantity = reservationDD.get(reservation.getId()).getDecimalField(ReservationFields.QUANTITY);
        }
        BigDecimal quantityToAdd = newQuantity.subtract(oldQuantity);
        resourceStockService.updateResourceStock(product, location, quantityToAdd);

    }

    public void onCreate(DataDefinition reservationDD, Entity reservation) {
    }

    public void onCopy(DataDefinition reservationDD, Entity reservation) {
        resourceStockService.updateResourceStock(reservation.getBelongsToField(ReservationFields.PRODUCT),
                reservation.getBelongsToField(ReservationFields.LOCATION),
                reservation.getDecimalField(ReservationFields.QUANTITY));
    }
}
