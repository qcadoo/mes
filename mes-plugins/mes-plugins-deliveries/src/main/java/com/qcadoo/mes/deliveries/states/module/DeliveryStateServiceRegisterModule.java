package com.qcadoo.mes.deliveries.states.module;

import org.springframework.beans.factory.annotation.Autowired;

import com.qcadoo.mes.deliveries.states.aop.DeliveryStateChangeAspect;
import com.qcadoo.mes.states.module.AbstractStateServiceRegisterModule;
import com.qcadoo.mes.states.service.StateChangeService;

public class DeliveryStateServiceRegisterModule extends AbstractStateServiceRegisterModule {

    @Autowired
    private DeliveryStateChangeAspect deliveryStateChangeAspect;

    @Override
    protected StateChangeService getStateChangeService() {
        return deliveryStateChangeAspect;
    }

}
