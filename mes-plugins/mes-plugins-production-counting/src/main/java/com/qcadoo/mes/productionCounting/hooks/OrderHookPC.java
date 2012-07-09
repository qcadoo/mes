package com.qcadoo.mes.productionCounting.hooks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class OrderHookPC {

    @Autowired
    private ParameterService parameterService;

    public void setDefaultParameterToProductionCounting(final DataDefinition dataDefinition, final Entity order) {
        String typeOfProductionRecording = order.getStringField("typeOfProductionRecording");
        Entity parameter = parameterService.getParameter();
        String defaultTypeOfProductionRecording = parameter.getStringField("typeOfProductionRecording");

        if ("".equals(typeOfProductionRecording) || typeOfProductionRecording == null) {
            order.setField("typeOfProductionRecording", defaultTypeOfProductionRecording);
        }
    }
}
