package com.qcadoo.mes.masterOrders.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.masterOrders.constants.MasterOrderFields;
import com.qcadoo.mes.masterOrders.constants.OrderFieldsMO;
import com.qcadoo.mes.masterOrders.constants.ParameterFieldsMO;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class OrderDetailsListenersMO {

    @Autowired
    private ParameterService parameterService;

    public void updateDescription(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        if (parameterService.getParameter().getBooleanField(ParameterFieldsMO.COPY_DESCRIPTION)) {
            FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
            Entity order = form.getEntity();
            Entity masterOrder = order.getBelongsToField(OrderFieldsMO.MASTER_ORDER);
            if (masterOrder != null) {
                order.setField(OrderFields.DESCRIPTION, masterOrder.getStringField(MasterOrderFields.DESCRIPTION));
                form.setEntity(order);
            }
        }

    }
}
