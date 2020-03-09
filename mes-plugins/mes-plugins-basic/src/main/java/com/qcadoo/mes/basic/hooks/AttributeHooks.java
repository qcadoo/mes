package com.qcadoo.mes.basic.hooks;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.constants.AttributeDataType;
import com.qcadoo.mes.basic.constants.AttributeFields;
import com.qcadoo.mes.basic.constants.AttributeValueType;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class AttributeHooks {

    public static final int MAX_PRECISION = 5;

    public boolean validate(final DataDefinition attributeDD, final Entity attribute) {

        if (!attribute.getBooleanField(AttributeFields.FOR_PRODUCT) && !attribute.getBooleanField(AttributeFields.FOR_RESOURCE)
                && !attribute.getBooleanField(AttributeFields.FOR_QUALITY_CONTROL)) {
            attribute.addGlobalError("basic.attribute.error.attributeFlagNotFilled");
            return false;
        }

        if (AttributeValueType.NUMERIC.getStringValue().equals(attribute.getStringField(AttributeFields.VALUE_TYPE))) {
            if (Objects.isNull(attribute.getIntegerField(AttributeFields.PRECISION))) {
                attribute.addError(attributeDD.getField(AttributeFields.PRECISION), "qcadooView.validate.field.error.missing");
                return false;
            } else if(MAX_PRECISION < attribute.getIntegerField(AttributeFields.PRECISION)) {
                attribute.addError(attributeDD.getField(AttributeFields.PRECISION), "basic.attribute.error.precisionToHeight");
                return false;
            } else if (0 > attribute.getIntegerField(AttributeFields.PRECISION)) {
                attribute.addError(attributeDD.getField(AttributeFields.PRECISION), "basic.attribute.error.precisionLowerThanZero");
                return false;
            }
        }
        return true;
    }

    public void onSave(final DataDefinition attributeDD, final Entity attribute) {
        if (AttributeDataType.CONTINUOUS.getStringValue().equals(attribute.getStringField(AttributeFields.DATA_TYPE))) {
            attribute.setField(AttributeFields.ATTRIBUTE_VALUES, Lists.newArrayList());
        }

        if (AttributeValueType.TEXT.getStringValue().equals(attribute.getStringField(AttributeFields.VALUE_TYPE))) {
            attribute.setField(AttributeFields.PRECISION, null);
        }

    }
}
