package com.qcadoo.mes.productionCounting.states.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionCounting.states.aop.ProductionRecordStateChangeAspect;
import com.qcadoo.mes.states.service.StateChangeService;
import com.qcadoo.mes.states.service.client.AbstractStateChangeViewClient;

@Service
public class ProductionRecordStateChangeViewClient extends AbstractStateChangeViewClient {

    @Autowired
    private ProductionRecordStateChangeAspect productionRecordStateChangeAspect;

    @Override
    protected StateChangeService getStateChangeService() {
        return productionRecordStateChangeAspect;
    }

}
