package com.qcadoo.mes.masterOrders.listeners;

import java.util.Objects;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.AttributeValueFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderProductAttrValueFields;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class MasterOrderProductAttrValueDetailsListeners {

    public void onChangeAttribute(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        LookupComponent attributeValueLookup = (LookupComponent) view
                .getComponentByReference(MasterOrderProductAttrValueFields.ATTRIBUTE_VALUE);
        FieldComponent valueField = (FieldComponent) view.getComponentByReference(MasterOrderProductAttrValueFields.VALUE);
        attributeValueLookup.setFieldValue(null);
        attributeValueLookup.requestComponentUpdateState();
        valueField.setFieldValue(null);
        valueField.requestComponentUpdateState();
    }

    public void onChangeAttributeValue(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        LookupComponent attributeValueLookup = (LookupComponent) view
                .getComponentByReference(MasterOrderProductAttrValueFields.ATTRIBUTE_VALUE);
        FieldComponent valueField = (FieldComponent) view.getComponentByReference(MasterOrderProductAttrValueFields.VALUE);
        if (Objects.nonNull(attributeValueLookup.getEntity())) {
            valueField.setFieldValue(attributeValueLookup.getEntity().getStringField(AttributeValueFields.VALUE));
            valueField.requestComponentUpdateState();
        }
    }
}
