package com.qcadoo.mes.masterOrders;

import com.qcadoo.mes.basic.ShiftsService;
import com.qcadoo.mes.lineChangeoverNorms.ChangeoverNormsService;
import com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields;
import com.qcadoo.mes.lineChangeoverNormsForOrders.LineChangeoverNormsForOrdersService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.exception.EntityRuntimeException;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchRestrictions;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import static com.qcadoo.mes.orders.constants.OrderFields.PRODUCTION_LINE;

@Service
public class OrdersGenerationService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ShiftsService shiftsService;

    @Autowired
    private ChangeoverNormsService changeoverNormsService;

    @Autowired
    private LineChangeoverNormsForOrdersService lineChangeoverNormsForOrdersService;

    /*
     * override by aspect
     */
    public void generateSubOrders(GenerationOrderResult result, Entity order) {

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Date tryGeneratePPS(final Entity order, final Date date) {
        Date startDate = findStartDate(order, date);

        generateEmptyPpsForOrder(order);

        order.setField("generatePPS", true);
        order.setField(OrderFields.START_DATE, startDate);
        order.setField(OrderFields.FINISH_DATE, new DateTime(order.getDateField(OrderFields.START_DATE)).plusDays(1).toDate());

        Entity storedOrder = order.getDataDefinition().save(order);

        if (!storedOrder.isValid()) {
            throw new EntityRuntimeException(storedOrder);
        }

        return order.getDateField(OrderFields.FINISH_DATE);
    }

    public void generateEmptyPpsForOrder(final Entity order) {
        Entity productionPerShift = dataDefinitionService.get("productionPerShift", "productionPerShift").find()
                .add(SearchRestrictions.belongsTo("order", order)).setMaxResults(1).uniqueResult();

        if (Objects.nonNull(productionPerShift)) {
            return;
        }

        boolean shouldBeCorrected = OrderState.of(order).compareTo(OrderState.PENDING) != 0;

        productionPerShift = dataDefinitionService.get("productionPerShift", "productionPerShift").create();
        productionPerShift.setField("order", order);

        if (shouldBeCorrected) {
            productionPerShift.setField("plannedProgressType", "02corrected");
        } else {
            productionPerShift.setField("plannedProgressType", "01planned");
        }

        productionPerShift.getDataDefinition().save(productionPerShift);
    }

    public Date findStartDate(final Entity order) {
        if (Objects.nonNull(order.getDateField(OrderFields.START_DATE))) {
            return order.getDateField(OrderFields.START_DATE);
        }

        Optional<Entity> previousOrder = findPreviousOrder(order);

        if (previousOrder.isPresent()) {
            Integer changeoverDurationInMillis = getChangeoverDurationInMillis(previousOrder.get(), order);

            Optional<DateTime> maybeDate = shiftsService.getNearestWorkingDate(
                    new DateTime(previousOrder.get().getDateField(OrderFields.FINISH_DATE)),
                    order.getBelongsToField(OrderFields.PRODUCTION_LINE));

            if (maybeDate.isPresent()) {
                return calculateOrderStartDate(maybeDate.get().toDate(), changeoverDurationInMillis);
            }
        }

        return DateTime.now().toDate();
    }

    public Date findStartDate(final Entity order, final Date startDate) {
        Optional<Entity> previousOrder = findPreviousOrder(order);

        if (previousOrder.isPresent()) {
            Integer changeoverDurationInMillis = getChangeoverDurationInMillis(previousOrder.get(), order);

            Optional<DateTime> maybeDate = shiftsService.getNearestWorkingDate(new DateTime(startDate),
                    order.getBelongsToField(OrderFields.PRODUCTION_LINE));

            if (maybeDate.isPresent()) {
                return calculateOrderStartDate(maybeDate.get().toDate(), changeoverDurationInMillis);
            }
        }

        return startDate;
    }

    public Date calculateOrderStartDate(final Date finishDate, final Integer changeoverDurationInMillis) {
        DateTime finishDateTime = new DateTime(finishDate);

        finishDateTime = finishDateTime.plusMillis(changeoverDurationInMillis);

        return finishDateTime.toDate();
    }

    public Optional<Entity> findPreviousOrder(final Entity order) {
        Entity productionLine = order.getBelongsToField(OrderFields.PRODUCTION_LINE);

        Entity nextOrder = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).find()
                .add(SearchRestrictions.belongsTo(OrderFields.PRODUCTION_LINE, productionLine))
                .add(SearchRestrictions.isNotNull(OrderFields.START_DATE)).addOrder(SearchOrders.desc(OrderFields.START_DATE))
                .setMaxResults(1).uniqueResult();

        return Optional.ofNullable(nextOrder);
    }

    public Integer getChangeoverDurationInMillis(final Entity previousOrder, final Entity nextOrder) {
        Entity fromTechnology = previousOrder.getBelongsToField(OrderFields.TECHNOLOGY_PROTOTYPE);
        Entity toTechnology = nextOrder.getBelongsToField(OrderFields.TECHNOLOGY_PROTOTYPE);
        Entity productionLine = nextOrder.getBelongsToField(PRODUCTION_LINE);
        Entity changeover = changeoverNormsService.getMatchingChangeoverNorms(fromTechnology, toTechnology, productionLine);

        if (Objects.nonNull(changeover)) {
            Integer duration = changeover.getIntegerField(LineChangeoverNormsFields.DURATION);

            if (Objects.isNull(duration)) {
                return 0;
            }

            return duration * 1000;
        }

        return 0;
    }

}
