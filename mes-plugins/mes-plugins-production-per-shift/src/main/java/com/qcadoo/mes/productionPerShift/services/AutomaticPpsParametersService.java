package com.qcadoo.mes.productionPerShift.services;

import com.qcadoo.mes.productionPerShift.constants.ParameterFieldsPPS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.productionPerShift.constants.PpsAlgorithm;

@Service
public class AutomaticPpsParametersService {

    @Autowired
    private ParameterService parameterService;

    public PpsAlgorithm getPpsAlgorithm(){
        String value = parameterService.getParameter().getStringField(ParameterFieldsPPS.PPS_ALGORITHM);
        return PpsAlgorithm.fromStringValue(value);
    }

    public boolean isAutomaticPlanForShiftOn(){
        return parameterService.getParameter().getBooleanField(ParameterFieldsPPS.PPS_IS_AUTOMATIC);
    }
}
