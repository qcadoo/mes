package com.qcadoo.mes.productionCounting.internal.states;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class ProductionRecordStateService {

    public void changeRecordStateToAccepted(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        changeRecordState(view, ProductionCountingStates.ACCEPTED.getStringValue());
    }

    public void changeRecordStateToDeclined(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        changeRecordState(view, ProductionCountingStates.DECLINED.getStringValue());
    }

    private void changeRecordState(final ViewDefinitionState view, final String state) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity productionCounting = form.getEntity();
        productionCounting.setField("state", state);
        productionCounting.getDataDefinition().save(productionCounting);
    }

}
