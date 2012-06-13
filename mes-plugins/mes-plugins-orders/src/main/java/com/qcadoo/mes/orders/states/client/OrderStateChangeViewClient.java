package com.qcadoo.mes.orders.states.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.states.aop.OrderStateChangeAspect;
import com.qcadoo.mes.states.service.StateChangeService;
import com.qcadoo.mes.states.service.client.AbstractStateChangeViewClient;

@Service
public class OrderStateChangeViewClient extends AbstractStateChangeViewClient {

    @Autowired
    private OrderStateChangeAspect orderStateChangeService;

    @Override
    protected StateChangeService getStateChangeService() {
        return orderStateChangeService;
    }

}
