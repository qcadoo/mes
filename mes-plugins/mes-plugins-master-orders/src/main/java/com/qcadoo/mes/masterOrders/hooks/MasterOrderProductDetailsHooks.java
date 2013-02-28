package com.qcadoo.mes.masterOrders.hooks;

import static com.qcadoo.mes.masterOrders.constants.MasterOrderFields.PRODUCT;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.orders.OrderService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class MasterOrderProductDetailsHooks {

    @Autowired
    private OrderService orderService;

    public void fillUnitField(final ViewDefinitionState view) {
        LookupComponent productField = (LookupComponent) view.getComponentByReference(PRODUCT);
        Entity product = productField.getEntity();

        if (product == null) {
            return;
        }

        for (String reference : Arrays.asList("cumulatedOrderQuantityUnit", "masterOrderQuantityUnit")) {
            FieldComponent field = (FieldComponent) view.getComponentByReference(reference);
            field.setFieldValue(product.getStringField(ProductFields.UNIT));
            field.requestComponentUpdateState();
        }
    }

    public void fillDefaultTechnology(final ViewDefinitionState view) {
        orderService.fillDefaultTechnology(view);
    }
}
