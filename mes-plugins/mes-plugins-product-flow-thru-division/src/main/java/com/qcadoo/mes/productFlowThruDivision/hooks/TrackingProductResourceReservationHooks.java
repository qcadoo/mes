package com.qcadoo.mes.productFlowThruDivision.hooks;

import com.qcadoo.mes.productionCounting.ProductionTrackingService;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductInComponentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.NumberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TrackingProductResourceReservationHooks {

    @Autowired
    private ProductionTrackingService productionTrackingService;

    @Autowired
    private NumberService numberService;

    public void onSave(final DataDefinition trackingProductResourceReservationDD,
                       final Entity trackingProductResourceReservation) {

        Entity trackingOperationProductInComponent = trackingProductResourceReservation.getBelongsToField("trackingOperationProductInComponent");

        List<Entity> resourceReservations = trackingOperationProductInComponent.getHasManyField("resourceReservations");

        List<Entity> otherResourceReservations = resourceReservations
                .stream()
                .filter(rr -> !rr.getId().equals(trackingProductResourceReservation.getId()))
                .collect(Collectors.toList());

        BigDecimal sumUsedQuantity = otherResourceReservations.
                stream()
                .map(rr -> rr.getDecimalField("usedQuantity"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        sumUsedQuantity = sumUsedQuantity.add(trackingProductResourceReservation.getDecimalField("usedQuantity"));

        trackingOperationProductInComponent.setField(TrackingOperationProductInComponentFields.USED_QUANTITY,
                sumUsedQuantity);

        Optional<BigDecimal> givenQuantity = productionTrackingService.calculateGivenQuantity(
                trackingOperationProductInComponent, sumUsedQuantity);
        givenQuantity.ifPresent(gq -> trackingOperationProductInComponent.setField(
                TrackingOperationProductInComponentFields.GIVEN_QUANTITY, gq));

        trackingOperationProductInComponent.getDataDefinition().fastSave(trackingOperationProductInComponent);
    }
}
