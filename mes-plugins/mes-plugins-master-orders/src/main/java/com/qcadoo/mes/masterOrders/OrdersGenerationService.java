package com.qcadoo.mes.masterOrders;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.ShiftsService;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.lineChangeoverNorms.ChangeoverNormsService;
import com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields;
import com.qcadoo.mes.masterOrders.constants.OrderFieldsMO;
import com.qcadoo.mes.orders.OrderService;
import com.qcadoo.mes.orders.TechnologyServiceO;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.constants.ParameterFieldsO;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.mes.orders.states.constants.OrderStateStringValues;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.exception.EntityRuntimeException;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.utils.NumberGeneratorService;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

import static com.qcadoo.mes.orders.constants.OrderFields.PRODUCTION_LINE;

@Service
public class OrdersGenerationService {

    private static final String IGNORE_MISSING_COMPONENTS = "ignoreMissingComponents";

    private static final String L_AUTOMATICALLY_GENERATE_ORDERS_FOR_COMPONENTS = "automaticallyGenerateOrdersForComponents";

    private static final String L_ORDERS_GENERATED_BY_COVERAGE = "ordersGeneratedByCoverage";

    private static final String L_ORDERS_GENERATION_NOT_COMPLETE_DATES = "ordersGenerationNotCompleteDates";

    private static final String L_PPS_IS_AUTOMATIC = "ppsIsAutomatic";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ShiftsService shiftsService;

    @Autowired
    private ChangeoverNormsService changeoverNormsService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private TechnologyServiceO technologyServiceO;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    public void createOrders(final GenerationOrderResult result, final Set<Long> ids, final BigDecimal plannedQuantity,
                             final Date dateFrom,
                             final Date dateTo) {
        Entity parameter = parameterService.getParameter();

        boolean automaticPps = parameter.getBooleanField(L_PPS_IS_AUTOMATIC);

        ids.forEach(productId -> {
            Entity product = getProductDD().get(productId);
            Entity order = createOrder(parameter, null, product, plannedQuantity, null, dateFrom, dateTo);

            if (!order.isValid()) {
                result.addGeneratedOrderNumber(product.getStringField(ProductFields.NUMBER));
            } else {
                if (Objects.isNull(order.getBelongsToField(OrderFields.TECHNOLOGY))) {
                    result.addOrderWithoutGeneratedSubOrders(new SubOrderErrorHolder(order.getStringField(OrderFields.NUMBER),
                            "masterOrders.masterOrder.generationOrder.ordersWithoutGeneratedSubOrders.technologyNotSet"));
                } else if (parameter.getBooleanField(L_AUTOMATICALLY_GENERATE_ORDERS_FOR_COMPONENTS)
                        && parameter.getBooleanField(L_ORDERS_GENERATED_BY_COVERAGE)
                        && Objects.nonNull(order.getDateField(OrderFields.DATE_FROM))
                        && order.getDateField(OrderFields.DATE_FROM).before(new Date())) {
                    result.addOrderWithoutGeneratedSubOrders(new SubOrderErrorHolder(order.getStringField(OrderFields.NUMBER),
                            "masterOrders.masterOrder.generationOrder.ordersWithoutGeneratedSubOrders.orderStartDateEarlierThanToday"));
                } else {
                    generateSubOrders(result, order);
                }

                createPps(result, parameter, automaticPps, order);

                result.addGeneratedOrderNumber(order.getStringField(OrderFields.NUMBER));
            }
        });
    }

    public void createPps(final GenerationOrderResult result, final Entity parameter, boolean automaticPps,
                          final Entity order) {
        if (order.isValid() && automaticPps && !parameter.getBooleanField(L_ORDERS_GENERATION_NOT_COMPLETE_DATES)) {
            List<Entity> orders = getOrderAndSubOrders(order.getId());

            Collections.reverse(orders);

            Integer lastLevel = null;
            Date lastDate = null;

            for (Entity ord : orders) {
                Date calculatedOrderStartDate;

                if (parameterService.getParameter().getBooleanField(ParameterFieldsO.ADVISE_START_DATE_OF_THE_ORDER)) {
                    calculatedOrderStartDate = order.getDateField(OrderFields.START_DATE);
                } else {
                    if (Objects.isNull(ord.getDateField(OrderFields.DATE_FROM))) {
                        Optional<Entity> maybeOrder = orderService.findLastOrder(ord);

                        if (maybeOrder.isPresent()) {
                            calculatedOrderStartDate = ord.getDateField(OrderFields.FINISH_DATE);
                        } else {
                            calculatedOrderStartDate = new DateTime().toDate();
                        }
                    } else {
                        Optional<Entity> maybeOrder = findPreviousOrder(ord);

                        if (maybeOrder.isPresent()) {
                            calculatedOrderStartDate = maybeOrder.get().getDateField(OrderFields.FINISH_DATE);

                        } else {
                            calculatedOrderStartDate = ord.getDateField(OrderFields.FINISH_DATE);
                        }
                    }
                }

                if (Objects.isNull(calculatedOrderStartDate)) {
                    calculatedOrderStartDate = new DateTime().toDate();
                }

                if (Objects.nonNull(lastLevel) && !Objects.equals(lastLevel, ord.getIntegerField("level"))) {
                    if (Objects.nonNull(lastDate) && calculatedOrderStartDate.before(lastDate)) {
                        calculatedOrderStartDate = lastDate;
                    }
                }

                try {
                    Date finishDate = tryGeneratePPS(ord, calculatedOrderStartDate);

                    if (Objects.nonNull(lastDate) && finishDate.after(lastDate)) {
                        lastDate = finishDate;
                    } else if (Objects.isNull(lastDate)) {
                        lastDate = finishDate;
                    }
                } catch (Exception ex) {
                    result.addOrderWithoutPps(ord.getStringField(OrderFields.NUMBER));

                    break;
                }

                lastLevel = ord.getIntegerField("level");
            }
        }
    }

    @Transactional
    public Entity createOrder(final Entity parameter, Entity technology, final Entity product,
                              final BigDecimal plannedQuantity,
                              final Entity salesPlan, final Date dateFrom, final Date dateTo) {
        Entity order = getOrderDD().create();

        if (Objects.isNull(technology)) {
            technology = technologyServiceO.getDefaultTechnology(product);
        }

        order.setField(OrderFields.NUMBER,
                numberGeneratorService.generateNumber(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER));
        order.setField(OrderFields.NAME, orderService.makeDefaultName(product, technology, LocaleContextHolder.getLocale()));
        order.setField(OrderFields.PRODUCT, product);
        order.setField(OrderFields.TECHNOLOGY, technology);
        order.setField(OrderFields.PRODUCTION_LINE, orderService.getProductionLine(technology));
        order.setField(OrderFields.DIVISION, orderService.getDivision(technology));
        order.setField(OrderFields.DATE_FROM, dateFrom);
        order.setField(OrderFields.DATE_TO, dateTo);
        order.setField(OrderFields.EXTERNAL_SYNCHRONIZED, true);
        order.setField(OrderFields.STATE, OrderStateStringValues.PENDING);
        order.setField(OrderFields.PLANNED_QUANTITY, plannedQuantity);

        order.setField(IGNORE_MISSING_COMPONENTS, parameter.getBooleanField(IGNORE_MISSING_COMPONENTS));
        order.setField(OrderFields.DESCRIPTION, buildDescription(parameter, technology, product));
        order.setField(OrderFieldsMO.SALES_PLAN, salesPlan);

        return order.getDataDefinition().save(order);
    }

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

    private void generateEmptyPpsForOrder(final Entity order) {
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

    private Date findStartDate(final Entity order, final Date startDate) {
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

    private Date calculateOrderStartDate(final Date finishDate, final Integer changeoverDurationInMillis) {
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

    private Integer getChangeoverDurationInMillis(final Entity previousOrder, final Entity nextOrder) {
        Entity fromTechnology = previousOrder.getBelongsToField(OrderFields.TECHNOLOGY);
        Entity toTechnology = nextOrder.getBelongsToField(OrderFields.TECHNOLOGY);
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

    private String buildDescription(final Entity parameter, final Entity technology, final Entity product) {
        boolean fillOrderDescriptionBasedOnTechnology = parameter
                .getBooleanField(ParameterFieldsO.FILL_ORDER_DESCRIPTION_BASED_ON_TECHNOLOGY_DESCRIPTION);

        boolean fillOrderDescriptionBasedOnProductDescription = parameter
                .getBooleanField(ParameterFieldsO.FILL_ORDER_DESCRIPTION_BASED_ON_PRODUCT_DESCRIPTION);

        StringBuilder descriptionBuilder = new StringBuilder();

        if (fillOrderDescriptionBasedOnTechnology && Objects.nonNull(technology)
                && StringUtils.isNoneBlank(technology.getStringField(TechnologyFields.DESCRIPTION))) {
            if (StringUtils.isNoneBlank(descriptionBuilder.toString())) {
                descriptionBuilder.append("\n");
            }

            descriptionBuilder.append(technology.getStringField(TechnologyFields.DESCRIPTION));
        }

        if (fillOrderDescriptionBasedOnProductDescription && Objects.nonNull(product)) {
            String productDescription = product.getStringField(ProductFields.DESCRIPTION);

            if (StringUtils.isNoneBlank(productDescription)) {
                if (StringUtils.isNoneBlank(descriptionBuilder.toString())) {
                    descriptionBuilder.append("\n");
                }

                descriptionBuilder.append(productDescription);
            }
        }

        return descriptionBuilder.toString();
    }

    private List<Entity> getOrderAndSubOrders(final Long orderID) {
        String sql = "SELECT o FROM #orders_order AS o WHERE o.root = :orderID OR o.id = :orderID";

        return getOrderDD().find(sql).setLong("orderID", orderID).list().getEntities();
    }

    private DataDefinition getOrderDD() {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER);
    }

    private DataDefinition getProductDD() {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT);
    }

}
