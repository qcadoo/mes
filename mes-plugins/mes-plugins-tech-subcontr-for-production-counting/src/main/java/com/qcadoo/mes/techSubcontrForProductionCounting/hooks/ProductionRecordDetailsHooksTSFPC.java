package com.qcadoo.mes.techSubcontrForProductionCounting.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionCounting.states.constants.ProductionRecordState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class ProductionRecordDetailsHooksTSFPC {

    public void disabledSubcontractorFieldForState(final ViewDefinitionState view) {
        FieldComponent state = (FieldComponent) view.getComponentByReference("state");
        FieldComponent subcontractor = (FieldComponent) view.getComponentByReference("subcontractor");
        if (state.getFieldValue().toString().equals(ProductionRecordState.ACCEPTED.getStringValue())
                || state.getFieldValue().toString().equals(ProductionRecordState.DECLINED.getStringValue())) {
            subcontractor.setEnabled(false);
        } else {
            subcontractor.setEnabled(true);
        }
        subcontractor.requestComponentUpdateState();
    }
}