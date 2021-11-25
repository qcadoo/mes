package com.qcadoo.mes.orders.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Lists;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.controllers.dataProvider.DashboardKanbanDataProvider;
import com.qcadoo.mes.orders.controllers.dto.OperationalTaskHolder;
import com.qcadoo.mes.orders.controllers.dto.OrderHolder;
import com.qcadoo.mes.orders.controllers.responses.OrderResponse;
import com.qcadoo.mes.orders.states.aop.OrderStateChangeAspect;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.messages.util.MessagesUtil;
import com.qcadoo.mes.states.service.StateChangeContextBuilder;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.validators.ErrorMessage;

@Controller
@RequestMapping("/dashboardKanban")
public class DashboardKanbanController {

    @Autowired
    private DashboardKanbanDataProvider dashboardKanbanDataProvider;

    @Autowired
    private OrderStateChangeAspect orderStateChangeAspect;

    @Autowired
    private StateChangeContextBuilder stateChangeContextBuilder;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    @ResponseBody
    @RequestMapping(value = "/ordersPending", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<OrderHolder> getOrdersPending() {
        return dashboardKanbanDataProvider.getOrdersPending();
    }

    @ResponseBody
    @RequestMapping(value = "/ordersInProgress", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<OrderHolder> getOrdersInProgress() {
        return dashboardKanbanDataProvider.getOrdersInProgress();
    }

    @ResponseBody
    @RequestMapping(value = "/ordersCompleted", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<OrderHolder> getOrdersCompleted() {
        return dashboardKanbanDataProvider.getOrdersCompleted();
    }

    @ResponseBody
    @RequestMapping(value = "/operationalTasksPending", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<OperationalTaskHolder> getOperationalTasksPending() {
        return dashboardKanbanDataProvider.getOperationalTasksPending();
    }

    @ResponseBody
    @RequestMapping(value = "/operationalTasksInProgress", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<OperationalTaskHolder> getOperationalTasksInProgress() {
        return dashboardKanbanDataProvider.getOperationalTasksInProgress();
    }

    @ResponseBody
    @RequestMapping(value = "/operationalTasksCompleted", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<OperationalTaskHolder> getOperationalTasksCompleted() {
        return dashboardKanbanDataProvider.getOperationalTasksCompleted();
    }

    @ResponseBody
    @RequestMapping(value = "/updateOrderState/{orderId}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public OrderResponse updateOrderState(@PathVariable final Long orderId) {
        Entity order = getOrderDD().get(orderId);

        String targetState = OrderState.IN_PROGRESS.getStringValue();

        if (OrderState.IN_PROGRESS.getStringValue().equals(order.getStringField(OrderFields.STATE))) {
            targetState = OrderState.COMPLETED.getStringValue();
        }

        StateChangeContext orderStateChangeContext = stateChangeContextBuilder
                .build(orderStateChangeAspect.getChangeEntityDescriber(), order, targetState);

        orderStateChangeAspect.changeState(orderStateChangeContext);

        OrderResponse orderResponse = new OrderResponse(dashboardKanbanDataProvider.getOrder(orderId));

        List<ErrorMessage> errors = Lists.newArrayList();

        if (!orderStateChangeContext.getAllMessages().isEmpty()) {
            for (Entity entity : orderStateChangeContext.getAllMessages()) {
                errors.add(new ErrorMessage(MessagesUtil.getKey(entity), MessagesUtil.getArgs(entity)));
            }
        }

        if (!errors.isEmpty()) {
            String errorMessages = errors.stream().map(errorMessage -> translationService.translate(errorMessage.getMessage(),
                    LocaleContextHolder.getLocale(), errorMessage.getVars())).collect(Collectors.joining(", "));

            orderResponse.setMessage(translationService.translate("orders.order.orderStates.error",
                    LocaleContextHolder.getLocale(), errorMessages));
        }

        return orderResponse;
    }

    private DataDefinition getOrderDD() {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER);
    }

}
