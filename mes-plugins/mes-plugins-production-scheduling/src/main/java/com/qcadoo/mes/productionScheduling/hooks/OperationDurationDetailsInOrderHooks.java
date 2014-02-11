package com.qcadoo.mes.productionScheduling.hooks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.productionScheduling.constants.OrderFieldsPS;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class OperationDurationDetailsInOrderHooks {

    private static final String L_FORM = "form";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void fillUnitField(final ViewDefinitionState view) {
        FormComponent orderForm = (FormComponent) view.getComponentByReference(L_FORM);
        FieldComponent unitField = (FieldComponent) view.getComponentByReference(OrderFieldsPS.OPERATION_DURATION_QUANTITY_UNIT);

        Long orderId = orderForm.getEntityId();

        if (orderId != null) {
            Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(orderId);

            if (order != null) {
                Entity product = order.getBelongsToField(OrderFields.PRODUCT);

                if (product != null) {
                    unitField.setFieldValue(product.getField(ProductFields.UNIT));
                }
            }
        }
    }

}
