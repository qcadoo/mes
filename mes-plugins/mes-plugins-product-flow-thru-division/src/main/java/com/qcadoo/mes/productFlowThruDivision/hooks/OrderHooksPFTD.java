package com.qcadoo.mes.productFlowThruDivision.hooks;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.productFlowThruDivision.constants.OrderFieldsPFTD;
import com.qcadoo.mes.productFlowThruDivision.constants.ParameterFieldsPFTD;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderHooksPFTD {

    @Autowired
    private ParameterService parameterService;

    public void onCreate(final DataDefinition orderDD, final Entity order) {
        order.setField(OrderFieldsPFTD.IGNORE_MISSING_COMPONENTS,
                parameterService.getParameter().getBooleanField(ParameterFieldsPFTD.IGNORE_MISSING_COMPONENTS));
    }

}
