package com.qcadoo.mes.materialFlowResources.hooks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialFlowResources.ReservationsService;
import com.qcadoo.mes.materialFlowResources.constants.ReservationFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class ReservationHooks {

    @Autowired
    private ReservationsService reservationsService;

    public boolean onDelete(DataDefinition reservationDD, Entity reservation) {
        reservationsService.updateResourceStock(reservation.getBelongsToField(ReservationFields.PRODUCT),
                reservation.getBelongsToField(ReservationFields.LOCATION),
                reservation.getDecimalField(ReservationFields.QUANTITY).negate());
        return true;
    }
}
