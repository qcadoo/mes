package com.qcadoo.mes.orders.states.aop.listener;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.orders.OrderPackService;
import com.qcadoo.mes.orders.OrderTechnologicalProcessService;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.constants.ParameterFieldsO;
import com.qcadoo.mes.orders.states.aop.OrderStateChangeAspect;
import com.qcadoo.mes.orders.states.constants.OrderStateStringValues;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.annotation.RunForStateTransition;
import com.qcadoo.mes.states.aop.AbstractStateListenerAspect;
import com.qcadoo.plugin.api.RunIfEnabled;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Aspect
@Configurable
@RunIfEnabled(OrdersConstants.PLUGIN_IDENTIFIER)
public class OrderPackOrderStateAspect extends AbstractStateListenerAspect {


    @Autowired
    private OrderPackService orderPackService;

    @Autowired
    private OrderTechnologicalProcessService orderTechnologicalProcessService;

    @Autowired
    private ParameterService parameterService;

    @Pointcut(OrderStateChangeAspect.SELECTOR_POINTCUT)
    protected void targetServicePointcut() {
    }

    @RunForStateTransition(targetState = OrderStateStringValues.IN_PROGRESS)
    @After(CHANGE_STATE_EXECUTION_POINTCUT)
    public void generateOrderPacks(final StateChangeContext stateChangeContext) {
        orderPackService.generateOrderPacks(stateChangeContext.getOwner());

        if (parameterService.getParameter().getBooleanField(ParameterFieldsO.AUTOMATICALLY_GENERATE_PROCESSES_FOR_ORDER)) {
            orderTechnologicalProcessService.generateOrderTechnologicalProcesses(stateChangeContext.getOwner());
        }
    }
}

