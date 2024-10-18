package com.qcadoo.mes.masterOrders.hooks;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.AttributeDataType;
import com.qcadoo.mes.basic.constants.AttributeFields;
import com.qcadoo.mes.masterOrders.constants.ParameterFieldsMO;
import com.qcadoo.mes.masterOrders.constants.PricesListFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class PricesListDetailsHooks {

    private static final String UNIT_1 = "unit1";

    private static final String UNIT_2 = "unit2";

    public static final String ATTRIBUTE_1_ID = "attribute1Id";

    public static final String ATTRIBUTE_2_ID = "attribute2Id";

    @Autowired
    private ParameterService parameterService;

    public void onBeforeRender(final ViewDefinitionState view) {
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(PricesListFields.PRODUCT);
        FieldComponent productCategoryField = (FieldComponent) view.getComponentByReference(PricesListFields.PRODUCT_CATEGORY);
        productCategoryField.setEnabled(Objects.isNull(productLookup.getEntity()));
        productCategoryField.requestComponentUpdateState();
        Entity parameter = parameterService.getParameter();
        Entity priceListAttribute1 = parameter.getBelongsToField(ParameterFieldsMO.PRICE_LIST_ATTRIBUTE_1);
        Entity priceListAttribute2 = parameter.getBelongsToField(ParameterFieldsMO.PRICE_LIST_ATTRIBUTE_2);
        LookupComponent attribute1ValueLookup = (LookupComponent) view
                .getComponentByReference(PricesListFields.ATTRIBUTE_1_VALUE);
        LookupComponent attribute2ValueLookup = (LookupComponent) view
                .getComponentByReference(PricesListFields.ATTRIBUTE_2_VALUE);
        setFilters(attribute1ValueLookup, attribute2ValueLookup, priceListAttribute1, priceListAttribute2);
        FieldComponent value1Field = (FieldComponent) view.getComponentByReference(PricesListFields.VALUE_1);
        FieldComponent value2Field = (FieldComponent) view.getComponentByReference(PricesListFields.VALUE_2);

        if (Objects.nonNull(priceListAttribute1)) {
            FieldComponent unit1Field = (FieldComponent) view.getComponentByReference(UNIT_1);
            unit1Field.setFieldValue(priceListAttribute1.getStringField(AttributeFields.UNIT));
            unit1Field.requestComponentUpdateState();
            if (AttributeDataType.CONTINUOUS.getStringValue().equals(priceListAttribute1.getStringField(AttributeFields.DATA_TYPE))) {
                value1Field.setVisible(true);
                attribute1ValueLookup.setVisible(false);
            } else {
                value1Field.setVisible(false);
                attribute1ValueLookup.setVisible(true);
            }
        } else {
            value1Field.setVisible(false);
            attribute1ValueLookup.setVisible(false);
        }
        if (Objects.nonNull(priceListAttribute2)) {
            FieldComponent unit2Field = (FieldComponent) view.getComponentByReference(UNIT_2);
            unit2Field.setFieldValue(priceListAttribute2.getStringField(AttributeFields.UNIT));
            unit2Field.requestComponentUpdateState();
            if (AttributeDataType.CONTINUOUS.getStringValue().equals(priceListAttribute2.getStringField(AttributeFields.DATA_TYPE))) {
                value2Field.setVisible(true);
                attribute2ValueLookup.setVisible(false);
            } else {
                value2Field.setVisible(false);
                attribute2ValueLookup.setVisible(true);
            }
        } else {
            value2Field.setVisible(false);
            attribute2ValueLookup.setVisible(false);
        }
        FieldComponent dateToField = (FieldComponent) view.getComponentByReference(PricesListFields.DATE_TO);
        FieldComponent priceField = (FieldComponent) view.getComponentByReference(PricesListFields.PRICE);
        priceField.setEnabled(dateToField.getFieldValue() == null || "".equals(dateToField.getFieldValue()));
    }

    private void setFilters(LookupComponent attribute1ValueLookup, LookupComponent attribute2ValueLookup,
                            Entity priceListAttribute1, Entity priceListAttribute2) {
        FilterValueHolder attribute1ValueLookupFilters = attribute1ValueLookup.getFilterValue();
        if (Objects.nonNull(priceListAttribute1)) {
            attribute1ValueLookupFilters.put(ATTRIBUTE_1_ID, priceListAttribute1.getId());
        } else if (attribute1ValueLookupFilters.has(ATTRIBUTE_1_ID)) {
            attribute1ValueLookupFilters.remove(ATTRIBUTE_1_ID);
        }
        attribute1ValueLookup.setFilterValue(attribute1ValueLookupFilters);

        FilterValueHolder attribute2ValueLookupFilters = attribute2ValueLookup.getFilterValue();
        if (Objects.nonNull(priceListAttribute2)) {
            attribute2ValueLookupFilters.put(ATTRIBUTE_2_ID, priceListAttribute2.getId());
        } else if (attribute2ValueLookupFilters.has(ATTRIBUTE_2_ID)) {
            attribute2ValueLookupFilters.remove(ATTRIBUTE_2_ID);
        }
        attribute2ValueLookup.setFilterValue(attribute2ValueLookupFilters);
    }
}
