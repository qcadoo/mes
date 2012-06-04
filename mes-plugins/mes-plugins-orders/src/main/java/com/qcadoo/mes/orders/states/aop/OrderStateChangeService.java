package com.qcadoo.mes.orders.states.aop;

import static com.qcadoo.mes.orders.constants.OrderStateChangeFields.FINISHED;
import static com.qcadoo.mes.orders.constants.OrderStateChangeFields.ORDER;
import static com.qcadoo.mes.orders.constants.OrderStateChangeFields.TARGET_STATE;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.mes.orders.constants.OrderState;
import com.qcadoo.mes.states.annotation.StateChangePhase;
import com.qcadoo.mes.states.aop.AbstractStateChangeAspect;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

@Aspect
@Service
public class OrderStateChangeService extends AbstractStateChangeAspect {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    public Entity createStateChangeEntity() {
        DataDefinition dataDef = dataDefinitionService.get(getPluginIdentifier(), "orderStateChange");
        Entity stateChangeEntity = dataDef.create();
        return stateChangeEntity;
    }

    @Override
    public void changeState(final Entity stateChangeEntity) {
        phaseOne(stateChangeEntity);
    }

    @StateChangePhase
    public void phaseOne(final Entity stateChangeEntity) {
        phaseTwo(stateChangeEntity);
    }

    @StateChangePhase
    public void phaseTwo(final Entity stateChangeEntity) {
        phaseThree(stateChangeEntity);
    }

    @StateChangePhase
    public void phaseThree(final Entity stateChangeEntity) {
        phaseFour(stateChangeEntity);
    }

    @StateChangePhase
    public void phaseFour(final Entity stateChangeEntity) {
        performChangeEntityState(stateChangeEntity);
    }

    @StateChangePhase
    @Transactional
    protected void performChangeEntityState(final Entity stateChangeEntity) {
        Entity order = stateChangeEntity.getBelongsToField(ORDER);
        OrderState targetState = OrderState.parseString(stateChangeEntity.getStringField(TARGET_STATE));
        order.setField(getStateFieldName(), targetState);
        order.getDataDefinition().save(order);
        stateChangeEntity.setField(FINISHED, true);
        stateChangeEntity.getDataDefinition().save(stateChangeEntity);
    }

    @Override
    protected String getStateFieldName() {
        return "state";
    }

    @Override
    protected String getPluginIdentifier() {
        return "orders";
    }

    @Override
    protected String getModelName() {
        return "order";
    }

    @Pointcut("this(OrderStateChangeService)")
    public void stateChangeServiceSelector() {
    }

}
