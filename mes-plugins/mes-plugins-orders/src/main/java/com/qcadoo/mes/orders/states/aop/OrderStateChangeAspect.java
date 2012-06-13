package com.qcadoo.mes.orders.states.aop;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderStateChangePhase;
import com.qcadoo.mes.states.StateChangeEntityDescriber;
import com.qcadoo.mes.states.aop.AbstractStateChangeAspect;
import com.qcadoo.model.api.Entity;

@Aspect
@Service
public class OrderStateChangeAspect extends AbstractStateChangeAspect {

    @Autowired
    private OrderStateChangeDescriber describer;

    @Override
    public StateChangeEntityDescriber getChangeEntityDescriber() {
        return describer;
    }

    @Override
    protected String getStateFieldName() {
        return OrderFields.STATE;
    }

    @Override
    protected void changeStatePhase(final Entity stateChangeEntity, final int phaseNumber) {
    }

    @Override
    protected int getNumOfPhases() {
        return OrderStateChangePhase.getNumOfPhases();
    }

}
