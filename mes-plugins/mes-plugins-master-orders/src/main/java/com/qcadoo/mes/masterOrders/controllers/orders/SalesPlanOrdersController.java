package com.qcadoo.mes.masterOrders.controllers.orders;

import com.google.common.collect.Lists;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.masterOrders.GenerationOrderResult;
import com.qcadoo.mes.masterOrders.OrdersGenerationService;
import com.qcadoo.mes.masterOrders.SubOrderErrorHolder;
import com.qcadoo.mes.masterOrders.constants.MasterOrdersConstants;
import com.qcadoo.mes.masterOrders.constants.SalesPlanOrdersGroupEntryHelperFields;
import com.qcadoo.mes.masterOrders.constants.SalesPlanOrdersGroupHelperFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SalesPlanOrdersController {

    private static final String IGNORE_MISSING_COMPONENTS = "ignoreMissingComponents";

    private static final String L_AUTOMATICALLY_GENERATE_ORDERS_FOR_COMPONENTS = "automaticallyGenerateOrdersForComponents";

    private static final String L_ORDERS_GENERATED_BY_COVERAGE = "ordersGeneratedByCoverage";

    private static final String L_ORDERS_GENERATION_NOT_COMPLETE_DATES = "ordersGenerationNotCompleteDates";

    private static final String L_PPS_IS_AUTOMATIC = "ppsIsAutomatic";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private OrdersGenerationService ordersGenerationService;

    @ResponseBody
    @RequestMapping(value = "/masterOrders/generateOrdersSalePlan", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public GenerateOrdersSalePlanResponse generateOrdersSalePlan(
            @RequestBody GenerateOrdersSalePlanRequest generateOrdersSalePlanRequest) {
        return generateOrders(generateOrdersSalePlanRequest);
    }

    private GenerateOrdersSalePlanResponse generateOrders(GenerateOrdersSalePlanRequest generateOrdersSalePlanRequest) {
        Entity helper = dataDefinitionService.get(MasterOrdersConstants.PLUGIN_IDENTIFIER,
                MasterOrdersConstants.MODEL_SALES_PLAN_ORDERS_GROUP_HELPER).get(generateOrdersSalePlanRequest.getEntityId());

        List<OrderSalePlanPosition> positionsWithQuantities = generateOrdersSalePlanRequest.getPositions();
        List<Entity> positions = helper.getHasManyField(SalesPlanOrdersGroupHelperFields.SALES_PLAN_ORDERS_GROUP_ENTRY_HELPERS);
        Entity salesPlan = helper.getBelongsToField(SalesPlanOrdersGroupHelperFields.SALES_PLAN);
        GenerationOrderResult result = new GenerationOrderResult(translationService, parameterService);
        try {
            generateOrders(result, salesPlan, positionsWithQuantities, positions);
            GenerateOrdersSalePlanResponse response = new GenerateOrdersSalePlanResponse();
            response.setStatus(GenerateOrdersSalePlanResponse.SimpleResponseStatus.OK);
            response.setMessages(result.extractMessages());
            return response;
        } catch (Exception exc) {
            GenerateOrdersSalePlanResponse response = new GenerateOrdersSalePlanResponse();
            response.setStatus(GenerateOrdersSalePlanResponse.SimpleResponseStatus.ERROR);
            response.setErrorMessages(Lists.newArrayList(translationService.translate(
                    "masterOrders.ordersGenerationFromProducts.error.ordersNotGenerated", LocaleContextHolder.getLocale())));
            return response;
        }
    }

    private void generateOrders(GenerationOrderResult result, Entity salesPlan,
            List<OrderSalePlanPosition> positionsWithQuantities, List<Entity> positions) {
        Entity parameters = parameterService.getParameter();
        boolean automaticPps = parameters.getBooleanField(L_PPS_IS_AUTOMATIC);

        for (OrderSalePlanPosition pos : positionsWithQuantities) {
            Entity salesPlanOrderGroupEntry = positions.stream().filter(p -> p.getId().equals(pos.getId())).findAny().get();
            Entity product = salesPlanOrderGroupEntry.getBelongsToField(SalesPlanOrdersGroupEntryHelperFields.PRODUCT);
            Entity order = ordersGenerationService.createOrder(parameters,
                    salesPlanOrderGroupEntry.getBelongsToField(SalesPlanOrdersGroupEntryHelperFields.TECHNOLOGY), product,
                    pos.getValue(), salesPlan, null, null);
            if (!order.isValid()) {
                result.addProductOrderSimpleError(product.getStringField(ProductFields.NUMBER));

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

                ordersGenerationService.createPps(result, parameters, automaticPps, order);
                result.addGeneratedOrderNumber(order.getStringField(OrderFields.NUMBER));

            }
        }
    }

}
