package com.qcadoo.mes.orders.listeners;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.orders.OrderService;
import com.qcadoo.mes.orders.TechnologyServiceO;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrderType;
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

@Service
public class OrdersGenerationFromProductsListeners {



    private static final String DATE_FROM = "dateFrom";

    private static final String DATE_TO = "dateTo";

    private static final String IS_SUBCONTRACTED = "isSubcontracted";

    private static final String IGNORE_MISSING_COMPONENTS = "ignoreMissingComponents";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TechnologyServiceO technologyServiceO;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private OrderService orderService;

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

        try {
            generateOrders(view, ids, entity);
        } catch (Exception exc) {
            view.addMessage("orders.ordersGenerationFromProducts.error.ordersNotGenerated ", ComponentState.MessageType.FAILURE);

        }
        generated.setChecked(true);
    }

    private void generateOrders(ViewDefinitionState view, final Set<Long> ids, final Entity ordersGenerationHelper) {
        Entity parameters = parameterService.getParameter();
        BigDecimal plannedQuantity = ordersGenerationHelper.getDecimalField("plannedQuantity");
        Date dateFrom = ordersGenerationHelper.getDateField(DATE_FROM);
        Date dateTo = ordersGenerationHelper.getDateField(DATE_TO);
        List<String> failNumbers = Lists.newArrayList();
        List<String> correctNumbers = Lists.newArrayList();
        ids.forEach(productId -> {
            Entity product = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT).get(
                    productId);
            Entity order = createOrder(parameters, product, plannedQuantity, dateFrom, dateTo);
            if (!order.isValid()) {
                failNumbers.add(product.getStringField(ProductFields.NUMBER));
            } else {
                correctNumbers.add(product.getStringField(ProductFields.NUMBER));

            }
        });
        if (!failNumbers.isEmpty()) {
            String numbers = String.join(", ", failNumbers);
            view.addMessage("orders.ordersGenerationFromProducts.info.notGeneratedOrders", ComponentState.MessageType.INFO,
                    false, numbers);
        }
        if (!correctNumbers.isEmpty()) {
            String numbers = String.join(", ", correctNumbers);
            view.addMessage("orders.ordersGenerationFromProducts.info.generatedOrders", ComponentState.MessageType.INFO, false,
                    numbers);
        }
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
        order.setField(OrderFields.ORDER_TYPE, OrderType.WITH_PATTERN_TECHNOLOGY.getStringValue());
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

    public boolean validateDate(final DataDefinition dd, final Entity ordersGenerationHelper) {
        Date dateFrom = ordersGenerationHelper.getDateField(DATE_FROM);
        Date dateTo = ordersGenerationHelper.getDateField(DATE_TO);

        if (dateFrom == null || dateTo == null || dateTo.after(dateFrom)) {
            return true;
        }

        ordersGenerationHelper.addError(dd.getField(DATE_TO), "orders.validate.global.error.datesOrder");
        return false;
    }

    private DataDefinition getOrderDD() {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER);
    }
}
