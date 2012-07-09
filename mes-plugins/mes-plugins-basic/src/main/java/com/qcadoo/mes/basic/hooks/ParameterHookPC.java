package com.qcadoo.mes.basic.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class ParameterHookPC {

    public void setDefaultParameterToProductionCounting(final DataDefinition dataDefinition, final Entity parameter) {
        if (parameter.getStringField("typeOfProductionRecording") == null) {
            parameter.setField("typeOfProductionRecording", "01basic");
        }

    }
}
