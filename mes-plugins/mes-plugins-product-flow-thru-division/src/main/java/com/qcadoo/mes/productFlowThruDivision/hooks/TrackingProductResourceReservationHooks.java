package com.qcadoo.mes.productFlowThruDivision.hooks;

import com.qcadoo.mes.productFlowThruDivision.constants.TrackingProductResourceReservationFields;
import com.qcadoo.mes.productionCounting.ProductionTrackingService;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductInComponentFields;
import com.qcadoo.model.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TrackingProductResourceReservationHooks {

    public static final String L_FROM_TERMINAL = "fromTerminal";
    public static final String L_RESOURCE_RESERVATIONS = "resourceReservations";

    @Autowired
    private ProductionTrackingService productionTrackingService;

    public void onSave(final DataDefinition trackingProductResourceReservationDD,
                       final Entity trackingProductResourceReservation) {

        if (Objects.isNull(trackingProductResourceReservation.getId())) {
            return;
        }

        Entity trackingOperationProductInComponent = trackingProductResourceReservation.getBelongsToField(TrackingProductResourceReservationFields.TRACKING_OPERATION_PRODUCT_IN_COMPONENT);
        Entity productionTracking = trackingOperationProductInComponent.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCTION_TRACKING);

        if (productionTracking.getBooleanField(L_FROM_TERMINAL)) {
            return;
        }

        List<Entity> resourceReservations = trackingOperationProductInComponent.getHasManyField(L_RESOURCE_RESERVATIONS);

        List<Entity> otherResourceReservations = resourceReservations
                .stream()
                .filter(rr -> !rr.getId().equals(trackingProductResourceReservation.getId()))
                .collect(Collectors.toList());

        BigDecimal sumUsedQuantity = otherResourceReservations.
                stream()
                .map(rr -> rr.getDecimalField(TrackingProductResourceReservationFields.USED_QUANTITY))
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        sumUsedQuantity = sumUsedQuantity.add(BigDecimalUtils.convertNullToZero(trackingProductResourceReservation.getDecimalField(TrackingProductResourceReservationFields.USED_QUANTITY)));

        trackingOperationProductInComponent.setField(TrackingOperationProductInComponentFields.USED_QUANTITY,
                sumUsedQuantity);

        Optional<BigDecimal> givenQuantity = productionTrackingService.calculateGivenQuantity(
                trackingOperationProductInComponent, sumUsedQuantity);
        givenQuantity.ifPresent(gq -> trackingOperationProductInComponent.setField(
                TrackingOperationProductInComponentFields.GIVEN_QUANTITY, gq));

        trackingOperationProductInComponent.getDataDefinition().fastSave(trackingOperationProductInComponent);
    }
}
