package com.qcadoo.mes.techSubcontracting.listeners;

import com.qcadoo.mes.techSubcontracting.hooks.OperationDetailsHooksTS;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OperationDetailsListenersTS {

    @Autowired
    private OperationDetailsHooksTS operationDetailsHooksTS;

    public final void onIsSubcontractingChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        operationDetailsHooksTS.setUnitCostField(view);
    }

}
