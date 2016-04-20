package com.qcadoo.mes.productionCounting.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class ProductionCountingQuantitySetDetailsHooks {

    private static final String L_PRODUCT_UNIT = "productUnit";

    private static final String L_PRODUCT = "product";

    public void beforeRender(final ViewDefinitionState view) {
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(L_PRODUCT);
        Entity product = productLookup.getEntity();

        String unit = "";

        if (product != null) {
            unit = product.getStringField(ProductFields.UNIT);
        }

        FieldComponent field = (FieldComponent) view.getComponentByReference(L_PRODUCT_UNIT);
        field.setFieldValue(unit);
        field.requestComponentUpdateState();
    }
}
