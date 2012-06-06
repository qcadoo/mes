package com.qcadoo.mes.orders.states.aop;

import static com.qcadoo.mes.orders.constants.OrderFields.STATE;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.states.StateChangeEntityDescriber;
import com.qcadoo.mes.states.aop.AbstractStateChangeAspect;
import com.qcadoo.model.api.Entity;

@Aspect
@Service
public class OrderStateChangeAspect extends AbstractStateChangeAspect {

    private static final OrderStateChangeDescriber DESCRIBER = new OrderStateChangeDescriber();

    @Override
    public StateChangeEntityDescriber getChangeEntityDescriber() {
        return DESCRIBER;
    }

    @Override
    protected String getStateFieldName() {
        return STATE;
    }

    @Pointcut("this(OrderStateChangeAspect)")
    public void stateChangeServiceSelector() {
    }

    @Override
    protected void changeStatePhase(final Entity stateChangeEntity, final Integer phaseNumber) {
    }

}
