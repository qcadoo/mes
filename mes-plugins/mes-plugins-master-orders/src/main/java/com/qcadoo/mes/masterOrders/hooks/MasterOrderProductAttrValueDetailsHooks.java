package com.qcadoo.mes.masterOrders.hooks;

import java.util.Objects;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.AttributeDataType;
import com.qcadoo.mes.basic.constants.AttributeFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderProductAttrValueFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class MasterOrderProductAttrValueDetailsHooks {

    private static final String L_ATTRIBUTE_ID = "attributeId";

    private static final String L_UNIT = "unit";

    public void onBeforeRender(final ViewDefinitionState view) {
        setValueBold(view);
        setFilters(view);
        LookupComponent attributeLookup = (LookupComponent) view
                .getComponentByReference(MasterOrderProductAttrValueFields.ATTRIBUTE);
        LookupComponent attributeValueLookup = (LookupComponent) view
                .getComponentByReference(MasterOrderProductAttrValueFields.ATTRIBUTE_VALUE);
        FieldComponent valueField = (FieldComponent) view.getComponentByReference(MasterOrderProductAttrValueFields.VALUE);

        if (Objects.nonNull(attributeLookup.getEntity())) {
            Entity attribute = attributeLookup.getEntity();
            FieldComponent unitField = (FieldComponent) view.getComponentByReference(L_UNIT);
            unitField.setFieldValue(attribute.getStringField(AttributeFields.UNIT));
            unitField.requestComponentUpdateState();
            if (AttributeDataType.CONTINUOUS.getStringValue().equals(attribute.getStringField(AttributeFields.DATA_TYPE))) {
                valueField.setVisible(true);
                attributeValueLookup.setVisible(false);
            } else {
                valueField.setVisible(false);
                attributeValueLookup.setVisible(true);
            }
        } else {
            valueField.setVisible(false);
            attributeValueLookup.setVisible(false);
        }

    }

    private void setValueBold(ViewDefinitionState view) {
        LookupComponent attributeValueLookup = (LookupComponent) view
                .getComponentByReference(MasterOrderProductAttrValueFields.ATTRIBUTE_VALUE);
        attributeValueLookup.setRequired(true);
        attributeValueLookup.requestComponentUpdateState();
    }

    private void setFilters(ViewDefinitionState view) {
        LookupComponent attributeLookup = (LookupComponent) view
                .getComponentByReference(MasterOrderProductAttrValueFields.ATTRIBUTE);
        LookupComponent attributeValueLookup = (LookupComponent) view
                .getComponentByReference(MasterOrderProductAttrValueFields.ATTRIBUTE_VALUE);
        FilterValueHolder attributeValueLookupFilters = attributeValueLookup.getFilterValue();
        if (Objects.nonNull(attributeLookup.getEntity())) {
            attributeValueLookupFilters.put(L_ATTRIBUTE_ID, attributeLookup.getEntity().getId());
        } else if (attributeValueLookupFilters.has(L_ATTRIBUTE_ID)) {
            attributeValueLookupFilters.remove(L_ATTRIBUTE_ID);
        }
        attributeValueLookup.setFilterValue(attributeValueLookupFilters);
    }
}
