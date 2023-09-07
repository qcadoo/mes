package com.qcadoo.mes.productFlowThruDivision.hooks;

import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.materialFlowResources.constants.ResourceFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class OrderProductResourceReservationHooks {

    public void onSave(final DataDefinition orderProductResourceReservationDD, final Entity orderProductResourceReservation) {
        orderProductResourceReservation.setField("resourceNumber", orderProductResourceReservation.getBelongsToField("resource").getStringField(ResourceFields.NUMBER));
    }

    public boolean validate(final DataDefinition dataDefinition, final Entity orderProductResourceReservation) {

        Entity resource = orderProductResourceReservation.getBelongsToField("resource");
        BigDecimal resourceQuantity = resource.getDecimalField(ResourceFields.AVAILABLE_QUANTITY);
        BigDecimal planedQuantity = orderProductResourceReservation.getDecimalField("planedQuantity");
        if (planedQuantity.compareTo(resourceQuantity) > 0) {
            orderProductResourceReservation.addError(dataDefinition.getField("planedQuantity"), "productFlowThruDivision.orderProductResourceReservation.error.planedQuantityGreaterThanResource");
            return false;
        }

        Entity pcq = orderProductResourceReservation.getBelongsToField("productionCountingQuantity");

        List<Entity> orderProductResourceReservations = pcq.getHasManyField("orderProductResourceReservations");

        if (Objects.nonNull(orderProductResourceReservation.getId())) {
            orderProductResourceReservations = orderProductResourceReservations
                    .stream()
                    .filter(oprr -> !oprr.getId().equals(orderProductResourceReservation.getId()))
                    .collect(Collectors.toList());
        }
        orderProductResourceReservations.add(orderProductResourceReservation);

        BigDecimal productPlannedQuantity = pcq.getDecimalField(ProductionCountingQuantityFields.PLANNED_QUANTITY);

        BigDecimal plannedQuantityFromResources = orderProductResourceReservations
                .stream()
                .map(rr -> rr.getDecimalField("planedQuantity"))
                .filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);

        if (plannedQuantityFromResources.compareTo(productPlannedQuantity) > 0) {
            orderProductResourceReservation.addError(dataDefinition.getField("planedQuantity"), "productFlowThruDivision.orderProductResourceReservation.error.allPlanedQuantityGreaterThanProductQuantity");
            return false;
        }


        return true;
    }
}