package com.qcadoo.mes.orders.states.module;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.states.aop.OrderStateChangeAspect;
import com.qcadoo.mes.states.module.AbstractStateServiceRegisterModule;
import com.qcadoo.mes.states.service.StateChangeService;

@Service
public final class OrderStateServiceRegisterModule extends AbstractStateServiceRegisterModule {

    @Autowired
    private OrderStateChangeAspect orderStateChangeAspect;

    @Override
    protected StateChangeService getStateChangeService() {
        return orderStateChangeAspect;
    }

}
