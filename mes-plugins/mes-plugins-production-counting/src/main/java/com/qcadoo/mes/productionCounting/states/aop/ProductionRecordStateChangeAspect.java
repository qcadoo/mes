package com.qcadoo.mes.productionCounting.states.aop;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionCounting.states.constants.ProductionRecordStateChangeDescriber;
import com.qcadoo.mes.productionCounting.states.constants.ProductionRecordStateChangePhase;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.StateChangeEntityDescriber;
import com.qcadoo.mes.states.aop.AbstractStateChangeAspect;

@Aspect
@Service
public class ProductionRecordStateChangeAspect extends AbstractStateChangeAspect {

    @Autowired
    private ProductionRecordStateChangeDescriber productionRecordStateChangeDescriber;

    public static final String SELECTOR_POINTCUT = "this(com.qcadoo.mes.productionCounting.states.aop.ProductionRecordStateChangeAspect)";

    @Override
    public StateChangeEntityDescriber getChangeEntityDescriber() {
        return productionRecordStateChangeDescriber;
    }

    @Override
    protected int getNumOfPhases() {
        return ProductionRecordStateChangePhase.getNumOfPhases();
    }

    @Override
    protected void changeStatePhase(final StateChangeContext stateChangeContext, final int phase) {
    }

}
