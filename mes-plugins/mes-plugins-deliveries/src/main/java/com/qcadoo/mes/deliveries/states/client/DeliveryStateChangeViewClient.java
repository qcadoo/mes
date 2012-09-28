package com.qcadoo.mes.deliveries.states.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.deliveries.states.aop.DeliveryStateChangeAspect;
import com.qcadoo.mes.states.service.StateChangeService;
import com.qcadoo.mes.states.service.client.AbstractStateChangeViewClient;

@Service
public class DeliveryStateChangeViewClient extends AbstractStateChangeViewClient {

    @Autowired
    private DeliveryStateChangeAspect deliveryStateChangeService;

    @Override
    protected StateChangeService getStateChangeService() {
        return deliveryStateChangeService;
    }
}
