package com.qcadoo.mes.productionScheduling;

import org.springframework.stereotype.Service;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class TimeTechnologyInOrderService {

    public void setVisibleAlert(final ViewDefinitionState viewDefinitionState) {
        // ieldComponent realizationTime = (FieldComponent) viewDefinitionState.getComponentByReference("realizationTime");
        ComponentState alert = (ComponentState) viewDefinitionState.getComponentByReference("alert");

        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");

        if (form.getEntityId() == null) {
            // if (realizationTime == null || realizationTime.getFieldValue() == null /* ||
            // "0".equals(realizationTime.getFieldValue()) */) {
            alert.setVisible(true);
        } else {
            alert.setVisible(false);
        }
    }

}
