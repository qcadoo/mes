package com.qcadoo.mes.productionCounting.states.module;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionCounting.states.aop.ProductionRecordStateChangeAspect;
import com.qcadoo.mes.states.module.AbstractStateServiceRegisterModule;
import com.qcadoo.mes.states.service.StateChangeService;

@Service
public final class ProductionRecordStateServiceRegisterModule extends AbstractStateServiceRegisterModule {

    @Autowired
    private ProductionRecordStateChangeAspect productionRecordStateChangeAspect;

    @Override
    protected StateChangeService getStateChangeService() {
        return productionRecordStateChangeAspect;
    }

}
