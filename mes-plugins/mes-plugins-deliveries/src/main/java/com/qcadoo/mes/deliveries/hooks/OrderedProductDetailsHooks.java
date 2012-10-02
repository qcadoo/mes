package com.qcadoo.mes.deliveries.hooks;

import static com.qcadoo.mes.deliveries.constants.DeliveredProductFields.PRODUCT;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class OrderedProductDetailsHooks {

    public void fillUnitsFields(final ViewDefinitionState view) {
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(PRODUCT);
        Entity product = productLookup.getEntity();
        String unit = "";
        if (product != null) {
            unit = product.getStringField(ProductFields.UNIT);
        }
        FieldComponent field = (FieldComponent) view.getComponentByReference("orderedQuantityUNIT");
        field.setFieldValue(unit);
        field.requestComponentUpdateState();
    }
}
