package com.qcadoo.mes.productionScheduling;

import org.springframework.stereotype.Service;

import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class OrderTimePredictionService {

    public void setFieldDisable(final ViewDefinitionState viewDefinitionState) {
        FieldComponent quantity = (FieldComponent) viewDefinitionState.getComponentByReference("quantity");
        FieldComponent dateFrom = (FieldComponent) viewDefinitionState.getComponentByReference("dateFrom");
        FieldComponent dateTo = (FieldComponent) viewDefinitionState.getComponentByReference("dateTo");
        FieldComponent realizationTime = (FieldComponent) viewDefinitionState.getComponentByReference("realizationTime");

        quantity.setEnabled(true);
        dateFrom.setEnabled(true);
        dateTo.setEnabled(false);
        realizationTime.setEnabled(false);
    }

    /*
     * public void selectOrder(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) { }
     */

}
