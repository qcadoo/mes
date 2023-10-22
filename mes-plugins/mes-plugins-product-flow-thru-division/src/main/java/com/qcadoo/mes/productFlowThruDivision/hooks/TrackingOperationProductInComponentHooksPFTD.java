package com.qcadoo.mes.productFlowThruDivision.hooks;

import com.qcadoo.mes.productFlowThruDivision.constants.OrderProductResourceReservationFields;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductInComponentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
public class TrackingOperationProductInComponentHooksPFTD {

    @Autowired
    private NumberService numberService;

    public void onSave(final DataDefinition trackingOperationProductOutComponentDD,
                       final Entity trackingOperationProductInComponent) {

        List<Entity> resourceReservations = trackingOperationProductInComponent.getHasManyField("resourceReservations");

        if (!resourceReservations.isEmpty()) {
            BigDecimal usedQuantity = trackingOperationProductInComponent.getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY);
            if (Objects.nonNull(usedQuantity)) {
                for (Entity resourceReservation : resourceReservations) {
                    Entity orderProductResourceReservation = resourceReservation.getBelongsToField("orderProductResourceReservation");
                    BigDecimal resourcePlanedQuantity = orderProductResourceReservation.getDecimalField(OrderProductResourceReservationFields.PLANED_QUANTITY);

                    if (usedQuantity.compareTo(resourcePlanedQuantity) > 0) {
                        resourceReservation.setField("usedQuantity", resourcePlanedQuantity);
                        usedQuantity = usedQuantity.subtract(resourcePlanedQuantity, numberService.getMathContext());
                    } else if (usedQuantity.compareTo(BigDecimal.ZERO) > 0) {
                        resourceReservation.setField("usedQuantity", usedQuantity);
                        usedQuantity = BigDecimal.ZERO;
                    } else {
                        resourceReservation.setField("usedQuantity", null);
                    }
                    resourceReservation.getDataDefinition().save(resourceReservation);
                }
            }
        }

    }

}
