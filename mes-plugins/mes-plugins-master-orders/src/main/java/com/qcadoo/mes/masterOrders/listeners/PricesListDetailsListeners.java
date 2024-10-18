package com.qcadoo.mes.masterOrders.listeners;

import com.qcadoo.mes.basic.constants.AttributeValueFields;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.masterOrders.constants.PricesListFields;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.LookupComponent;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class PricesListDetailsListeners {

    public void onChangeAttribute1Value(final ViewDefinitionState view, final ComponentState state,
                                        final String[] args) {
        LookupComponent attributeValueLookup = (LookupComponent) view.getComponentByReference(PricesListFields.ATTRIBUTE_1_VALUE);
        FieldComponent valueField = (FieldComponent) view.getComponentByReference(PricesListFields.VALUE_1);
        if (Objects.nonNull(attributeValueLookup.getEntity())) {
            valueField.setFieldValue(attributeValueLookup.getEntity().getStringField(AttributeValueFields.VALUE));
        } else {
            valueField.setFieldValue(null);
        }
        valueField.requestComponentUpdateState();
    }

    public void onChangeAttribute2Value(final ViewDefinitionState view, final ComponentState state,
                                        final String[] args) {
        LookupComponent attributeValueLookup = (LookupComponent) view.getComponentByReference(PricesListFields.ATTRIBUTE_2_VALUE);
        FieldComponent valueField = (FieldComponent) view.getComponentByReference(PricesListFields.VALUE_2);
        if (Objects.nonNull(attributeValueLookup.getEntity())) {
            valueField.setFieldValue(attributeValueLookup.getEntity().getStringField(AttributeValueFields.VALUE));
        } else {
            valueField.setFieldValue(null);
        }
        valueField.requestComponentUpdateState();
    }

    public void onChangeProduct(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(PricesListFields.PRODUCT);
        FieldComponent productCategoryField = (FieldComponent) view.getComponentByReference(PricesListFields.PRODUCT_CATEGORY);
        if (Objects.nonNull(productLookup.getEntity())) {
            productCategoryField.setFieldValue(productLookup.getEntity().getStringField(ProductFields.CATEGORY));
        } else {
            productCategoryField.setFieldValue("");
        }
        productCategoryField.requestComponentUpdateState();
    }
}
