package com.qcadoo.mes.productionCounting.states.aop.listener;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.states.aop.ProductionRecordStateChangeAspect;
import com.qcadoo.mes.productionCounting.states.constants.ProductionRecordStateChangePhase;
import com.qcadoo.mes.productionCounting.states.constants.ProductionRecordStateStringValues;
import com.qcadoo.mes.productionCounting.states.listener.ProductionRecordBasicListenerService;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.annotation.RunForStateTransition;
import com.qcadoo.mes.states.annotation.RunInPhase;
import com.qcadoo.mes.states.aop.AbstractStateListenerAspect;
import com.qcadoo.plugin.api.RunIfEnabled;

@Aspect
@Configurable
@RunIfEnabled(ProductionCountingConstants.PLUGIN_IDENTIFIER)
public class ProductionRecordBasicListenerAspect extends AbstractStateListenerAspect {

    @Autowired
    private ProductionRecordBasicListenerService productionRecordBasicListenerService;

    @Pointcut(ProductionRecordStateChangeAspect.SELECTOR_POINTCUT)
    protected void targetServicePointcut() {
    }

    @RunInPhase(ProductionRecordStateChangePhase.PRE_VALIDATION)
    @RunForStateTransition(targetState = ProductionRecordStateStringValues.ACCEPTED)
    @Before(PHASE_EXECUTION_POINTCUT)
    public void validationOnAccept(final StateChangeContext stateChangeContext, final int phase) {
        productionRecordBasicListenerService.checkIfExistsFinalRecord(stateChangeContext);
    }

    @RunInPhase(ProductionRecordStateChangePhase.DEFAULT)
    @RunForStateTransition(targetState = ProductionRecordStateStringValues.ACCEPTED)
    @Before(PHASE_EXECUTION_POINTCUT)
    public void onAccept(final StateChangeContext stateChangeContext, final int phase) {
        productionRecordBasicListenerService.onAccept(stateChangeContext);
    }

    @RunInPhase(ProductionRecordStateChangePhase.DEFAULT)
    @RunForStateTransition(sourceState = ProductionRecordStateStringValues.ACCEPTED, targetState = ProductionRecordStateStringValues.DECLINED)
    @Before(PHASE_EXECUTION_POINTCUT)
    public void onChangeFromAcceptedToDecline(final StateChangeContext stateChangeContext, final int phase) {
        productionRecordBasicListenerService.onChangeFromAcceptedToDeclined(stateChangeContext);
    }

}
