package com.qcadoo.mes.techSubcontrForOperTasks.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.qcadoo.plugin.api.PluginUtils;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Aspect
@Configurable
public class OperationalTasksDetailsListenersOTFOOverrideAspect {

    @Autowired
    private OperationalTasksDetailsListenersOTFOOverrideUtil tasksDetailsListenersOTFOOverrideUtil;

    @Pointcut("execution(public void com.qcadoo.mes.operationalTasksForOrders.listeners.OperationalTasksDetailsListenersOTFO.setProductionLineFromOrderAndClearOperation(..)) "
            + "&& args(viewDefinitionState, state, args)")
    public void setProductionLineFromOrderListenerExecution(final ViewDefinitionState viewDefinitionState,
            final ComponentState state, final String[] args) {
    }

    @Around("setProductionLineFromOrderListenerExecution(viewDefinitionState, state, args)")
    public void aroundSetProductionLineFromOrderListenerExecution(final ProceedingJoinPoint pjp,
            final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) throws Throwable {
        if (PluginUtils.isEnabled("techSubcontracting")) {
            tasksDetailsListenersOTFOOverrideUtil.checkIfOperationIsSubcontracted(viewDefinitionState);
        } else {
            pjp.proceed();
        }
    }

    @Pointcut("execution(public void com.qcadoo.mes.operationalTasksForOrders.listeners.OperationalTasksDetailsListenersOTFO.setOperationalNameAndDescription(..)) "
            + "&& args(viewDefinitionState, state, args)")
    public void setOperationalNameAndDescriptionListenerExecution(final ViewDefinitionState viewDefinitionState,
            final ComponentState state, final String[] args) {
    }

    @Around("setOperationalNameAndDescriptionListenerExecution(viewDefinitionState, state, args)")
    public void aroundSetOperationalNameAndDescriptionListenerExecution(final ProceedingJoinPoint pjp,
            final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) throws Throwable {
        if (PluginUtils.isEnabled("techSubcontracting")) {
            tasksDetailsListenersOTFOOverrideUtil.setOperationalNameAndDescriptionForSubcontractedOperation(viewDefinitionState);
        } else {
            pjp.proceed();
        }
    }
}
