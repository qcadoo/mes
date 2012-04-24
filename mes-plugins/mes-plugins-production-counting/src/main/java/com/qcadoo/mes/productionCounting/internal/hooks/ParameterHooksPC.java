package com.qcadoo.mes.productionCounting.internal.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class ParameterHooksPC {

    public void addFieldsForParameter(final DataDefinition dataDefinition, final Entity parameter) {
        parameter.setField("registerQuantityInProduct", true);
        parameter.setField("registerQuantityOutProduct", true);
        parameter.setField("registerProductionTime", true);
        parameter.setField("justOne", false);
        parameter.setField("autoCloseOrder", false);
        parameter.setField("registerPiecework", false);
        parameter.setField("allowToClose", false);

    }
}
