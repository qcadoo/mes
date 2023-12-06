package com.qcadoo.mes.productFlowThruDivision.hooks;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.materialFlowResources.constants.ReservationFields;
import com.qcadoo.mes.materialFlowResources.constants.ResourceFields;
import com.qcadoo.mes.materialFlowResources.service.ResourceReservationsService;
import com.qcadoo.mes.productFlowThruDivision.constants.OrderProductResourceReservationFields;
import com.qcadoo.mes.productFlowThruDivision.reservation.OrderReservationsService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class OrderProductResourceReservationHooks {

    public static final String L_ORDER_PRODUCT_RESOURCE_RESERVATIONS = "orderProductResourceReservations";

    @Autowired
    private OrderReservationsService orderReservationsService;

    @Autowired
    private ResourceReservationsService resourceReservationsService;

    public boolean onDelete(final DataDefinition orderProductResourceReservationDD, final Entity orderProductResourceReservation) {
        BigDecimal usedQuantity = orderProductResourceReservation.getDecimalField(OrderProductResourceReservationFields.USED_QUANTITY);

        if (Objects.nonNull(usedQuantity) && usedQuantity.compareTo(BigDecimal.ZERO) > 0) {
            orderProductResourceReservation.addGlobalError("productFlowThruDivision.orderProductResourceReservation.error.cannotDeleteTheItem");
            return false;
        }

        List<Entity> reservations = orderProductResourceReservation.getHasManyField(OrderProductResourceReservationFields.RESERVATIONS);
        for (Entity reservation : reservations) {
            resourceReservationsService.updateResourceQuantitiesOnRemoveReservation(reservation.getBelongsToField(ReservationFields.RESOURCE),
                    reservation.getDecimalField(ReservationFields.QUANTITY));
        }
        return true;
    }

    public void onSave(final DataDefinition orderProductResourceReservationDD, final Entity orderProductResourceReservation) {
        if (Objects.isNull(orderProductResourceReservation.getId())) {
            orderProductResourceReservation.setField(OrderProductResourceReservationFields.CREATION_DATE, new Date());
        }
        Entity resource = orderProductResourceReservation.getBelongsToField(OrderProductResourceReservationFields.RESOURCE);
        if (Objects.nonNull(resource)) {
            orderProductResourceReservation.setField(OrderProductResourceReservationFields.RESOURCE_NUMBER,
                    resource.getStringField(ResourceFields.NUMBER));
            orderProductResourceReservation.setField(OrderProductResourceReservationFields.RESOURCE_UNIT,
                    resource.getBelongsToField(ResourceFields.PRODUCT).getStringField(ProductFields.UNIT));
        }
        orderReservationsService.createOrUpdateReservation(orderProductResourceReservation);
    }

    public boolean validate(final DataDefinition dataDefinition, final Entity orderProductResourceReservation) {

        Entity resource = orderProductResourceReservation.getBelongsToField(OrderProductResourceReservationFields.RESOURCE);

        if (Objects.isNull(resource)
                && Strings.isNullOrEmpty(orderProductResourceReservation.getStringField(OrderProductResourceReservationFields.RESOURCE_NUMBER))) {
            orderProductResourceReservation.addError(dataDefinition.getField(OrderProductResourceReservationFields.RESOURCE),
                    "qcadooView.validate.field.error.missing");
            return false;
        }

        if(Objects.isNull(resource)) {
            return true;
        }

        BigDecimal resourceQuantity = resource.getDecimalField(ResourceFields.AVAILABLE_QUANTITY);
        BigDecimal planedQuantity = orderProductResourceReservation.getDecimalField(OrderProductResourceReservationFields.PLANED_QUANTITY);

        if(Objects.nonNull(orderProductResourceReservation.getId())) {
            Entity orderProductResourceReservationDb = orderProductResourceReservation.getDataDefinition().get(orderProductResourceReservation.getId());
            resourceQuantity = resourceQuantity.add(orderProductResourceReservationDb.getDecimalField(OrderProductResourceReservationFields.PLANED_QUANTITY));
        }

        if (planedQuantity.compareTo(resourceQuantity) > 0) {
            orderProductResourceReservation.addError(dataDefinition.getField(OrderProductResourceReservationFields.PLANED_QUANTITY),
                    "productFlowThruDivision.orderProductResourceReservation.error.planedQuantityGreaterThanResource");
            return false;
        }

        Entity pcq = orderProductResourceReservation.getBelongsToField(OrderProductResourceReservationFields.PRODUCTION_COUNTING_QUANTITY);

        List<Entity> orderProductResourceReservations = Lists.newArrayList(pcq.getHasManyField(L_ORDER_PRODUCT_RESOURCE_RESERVATIONS));

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
                .map(rr -> rr.getDecimalField(OrderProductResourceReservationFields.PLANED_QUANTITY))
                .filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);

        if (plannedQuantityFromResources.compareTo(productPlannedQuantity) > 0) {
            orderProductResourceReservation.addError(dataDefinition.getField(OrderProductResourceReservationFields.PLANED_QUANTITY),
                    "productFlowThruDivision.orderProductResourceReservation.error.allPlanedQuantityGreaterThanProductQuantity");
            return false;
        }


        return true;
    }
}