package com.qcadoo.mes.masterOrders.listeners;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.masterOrders.GenerationOrderResult;
import com.qcadoo.mes.masterOrders.OrdersGenerationService;
import com.qcadoo.mes.masterOrders.SubOrderErrorHolder;
import com.qcadoo.mes.orders.OrderService;
import com.qcadoo.mes.orders.TechnologyServiceO;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.constants.ParameterFieldsO;
import com.qcadoo.mes.orders.states.constants.OrderStateStringValues;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.utils.NumberGeneratorService;
import com.qcadoo.view.constants.QcadooViewConstants;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrdersGenerationFromProductsListeners {

    private static final String DATE_FROM = "dateFrom";

    private static final String DATE_TO = "dateTo";

    private static final String IS_SUBCONTRACTED = "isSubcontracted";

    private static final String IGNORE_MISSING_COMPONENTS = "ignoreMissingComponents";

    private static final String L_AUTOMATICALLY_GENERATE_ORDERS_FOR_COMPONENTS = "automaticallyGenerateOrdersForComponents";

    private static final String L_ORDERS_GENERATED_BY_COVERAGE = "ordersGeneratedByCoverage";

    private static final String L_ORDERS_GENERATION_NOT_COMPLETE_DATES = "ordersGenerationNotCompleteDates";

    private static final String L_PPS_IS_AUTOMATIC = "ppsIsAutomatic";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TechnologyServiceO technologyServiceO;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrdersGenerationService ordersGenerationService;

    public void generateOrders(final ViewDefinitionState view, final ComponentState state, final String[] args)
            throws JSONException {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        CheckBoxComponent generated = (CheckBoxComponent) view.getComponentByReference("generated");
        Entity entity = form.getPersistedEntityWithIncludedFormValues();

        entity = entity.getDataDefinition().validate(entity);
        if (!entity.isValid()) {
            form.setEntity(entity);
            return;
        }
        JSONObject context = view.getJsonContext();
        Set<Long> ids = Arrays
                .stream(context.getString("window.mainTab.form.gridLayout.selectedEntities").replaceAll("[\\[\\]]", "")
                        .split(",")).map(Long::valueOf).collect(Collectors.toSet());
        GenerationOrderResult result = new GenerationOrderResult(translationService, parameterService);
        try {
            generateOrders(result, view, ids, entity);
            result.showMessage(view);
        } catch (Exception exc) {
            view.addMessage("orders.ordersGenerationFromProducts.error.ordersNotGenerated ", ComponentState.MessageType.FAILURE);

        }
        generated.setChecked(true);
    }

    private void generateOrders(GenerationOrderResult result, ViewDefinitionState view, final Set<Long> ids,
            final Entity ordersGenerationHelper) {
        Entity parameters = parameterService.getParameter();
        boolean automaticPps = parameters.getBooleanField(L_PPS_IS_AUTOMATIC);

        BigDecimal plannedQuantity = ordersGenerationHelper.getDecimalField("plannedQuantity");
        Date dateFrom = ordersGenerationHelper.getDateField(DATE_FROM);
        Date dateTo = ordersGenerationHelper.getDateField(DATE_TO);

        ids.forEach(productId -> {
            Entity product = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT).get(
                    productId);
            Entity order = createOrder(parameters, product, plannedQuantity, dateFrom, dateTo);
            if (!order.isValid()) {
                result.addGeneratedOrderNumber(product.getStringField(ProductFields.NUMBER));

            } else {
                if (Objects.isNull(order.getBelongsToField(OrderFields.TECHNOLOGY))) {
                    result.addOrderWithoutGeneratedSubOrders(new SubOrderErrorHolder(order.getStringField(OrderFields.NUMBER),
                            "masterOrders.masterOrder.generationOrder.ordersWithoutGeneratedSubOrders.technologyNotSet"));
                } else if (parameters.getBooleanField(L_AUTOMATICALLY_GENERATE_ORDERS_FOR_COMPONENTS)
                        && parameters.getBooleanField(L_ORDERS_GENERATED_BY_COVERAGE)
                        && Objects.nonNull(order.getDateField(OrderFields.DATE_FROM))
                        && order.getDateField(OrderFields.DATE_FROM).before(new Date())) {
                    result.addOrderWithoutGeneratedSubOrders(new SubOrderErrorHolder(order.getStringField(OrderFields.NUMBER),
                            "masterOrders.masterOrder.generationOrder.ordersWithoutGeneratedSubOrders.orderStartDateEarlierThanToday"));
                } else {
                    ordersGenerationService.generateSubOrders(result, order);
                }

                if (order.isValid() && automaticPps
                        && !parameters.getBooleanField(L_ORDERS_GENERATION_NOT_COMPLETE_DATES)) {
                    List<Entity> orders = getOrderAndSubOrders(order.getId());
                    Collections.reverse(orders);
                    Integer lastLevel = null;
                    Date lastDate = null;

                    for (Entity ord : orders) {
                        Date calculatedOrderStartDate = null;

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
                                Optional<Entity> maybeOrder = ordersGenerationService.findPreviousOrder(ord);

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
                            Date finishDate = ordersGenerationService.tryGeneratePPS(ord, calculatedOrderStartDate);

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
                result.addGeneratedOrderNumber(order.getStringField(OrderFields.NUMBER));

            }
        });

    }

    public boolean validateDate(final DataDefinition dd, final Entity ordersGenerationHelper) {
        Date dateFrom = ordersGenerationHelper.getDateField(DATE_FROM);
        Date dateTo = ordersGenerationHelper.getDateField(DATE_TO);

        if (dateFrom == null || dateTo == null || dateTo.after(dateFrom)) {
            return true;
        }

        ordersGenerationHelper.addError(dd.getField(DATE_TO), "orders.validate.global.error.datesOrder");
        return false;
    }
    

    @Transactional
    private Entity createOrder(Entity parameters, final Entity product, final BigDecimal plannedQuantity, final Date dateFrom,
            final Date dateTo) {
        Entity technology = technologyServiceO.getDefaultTechnology(product);
        Entity order = getOrderDD().create();
        order.setField(OrderFields.NUMBER,
                numberGeneratorService.generateNumber(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER));
        order.setField(OrderFields.NAME, orderService.makeDefaultName(product, technology, LocaleContextHolder.getLocale()));
        order.setField(OrderFields.PRODUCT, product);
        order.setField(OrderFields.TECHNOLOGY_PROTOTYPE, technology);
        order.setField(OrderFields.PRODUCTION_LINE, orderService.getProductionLine(technology));
        order.setField(OrderFields.DIVISION, orderService.getDivision(technology));
        order.setField(OrderFields.DATE_FROM, dateFrom);
        order.setField(OrderFields.DATE_TO, dateTo);
        order.setField(OrderFields.EXTERNAL_SYNCHRONIZED, true);
        order.setField(IS_SUBCONTRACTED, false);
        order.setField(OrderFields.STATE, OrderStateStringValues.PENDING);
        order.setField(OrderFields.PLANNED_QUANTITY, plannedQuantity);

        order.setField(IGNORE_MISSING_COMPONENTS, parameters.getBooleanField(IGNORE_MISSING_COMPONENTS));
        order.setField(OrderFields.DESCRIPTION, buildDescription(parameters, technology));
        return order.getDataDefinition().save(order);

    }

    private String buildDescription(Entity parameters, Entity technology) {
        boolean fillOrderDescriptionBasedOnTechnology = parameters
                .getBooleanField(ParameterFieldsO.FILL_ORDER_DESCRIPTION_BASED_ON_TECHNOLOGY_DESCRIPTION);
        StringBuilder descriptionBuilder = new StringBuilder();
        if (fillOrderDescriptionBasedOnTechnology && Objects.nonNull(technology)
                && StringUtils.isNoneBlank(technology.getStringField(TechnologyFields.DESCRIPTION))) {
            if (StringUtils.isNoneBlank(descriptionBuilder.toString())) {
                descriptionBuilder.append("\n");
            }
            descriptionBuilder.append(technology.getStringField(TechnologyFields.DESCRIPTION));

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
}
