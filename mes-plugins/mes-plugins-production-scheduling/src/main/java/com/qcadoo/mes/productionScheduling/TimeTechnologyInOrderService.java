package com.qcadoo.mes.productionScheduling;

import org.springframework.stereotype.Service;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class TimeTechnologyInOrderService {

    public void setVisibleAlert(final ViewDefinitionState viewDefinitionState) {
        FieldComponent realizationTime = (FieldComponent) viewDefinitionState.getComponentByReference("realizationTime");
        ComponentState alert = (ComponentState) viewDefinitionState.getComponentByReference("alert");

        if (realizationTime == null || realizationTime.getFieldValue() == null) {
            alert.setVisible(true);
        } else {
            alert.setVisible(false);
        }
    }

}
