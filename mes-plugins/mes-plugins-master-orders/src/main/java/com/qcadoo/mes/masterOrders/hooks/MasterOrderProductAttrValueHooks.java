package com.qcadoo.mes.masterOrders.hooks;

import com.google.common.base.Optional;
import com.qcadoo.commons.functional.Either;
import com.qcadoo.mes.basic.constants.AttributeDataType;
import com.qcadoo.mes.basic.constants.AttributeFields;
import com.qcadoo.mes.basic.constants.AttributeValueType;
import com.qcadoo.mes.masterOrders.constants.MasterOrderProductAttrValueFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderProductFields;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class MasterOrderProductAttrValueHooks {

    public boolean validate(final DataDefinition masterOrderProductAttrValueDD, final Entity masterOrderProductAttrValue) {
        Entity attribute = masterOrderProductAttrValue.getBelongsToField(MasterOrderProductAttrValueFields.ATTRIBUTE);

        if (AttributeDataType.CONTINUOUS.getStringValue().equals(attribute.getStringField(AttributeFields.DATA_TYPE))
                && AttributeValueType.NUMERIC.getStringValue().equals(attribute.getStringField(AttributeFields.VALUE_TYPE))) {
            Either<Exception, Optional<BigDecimal>> eitherNumber = BigDecimalUtils.tryParseAndIgnoreSeparator(
                    masterOrderProductAttrValue.getStringField(MasterOrderProductAttrValueFields.VALUE), LocaleContextHolder.getLocale());

            if (eitherNumber.isRight()) {
                if (eitherNumber.getRight().isPresent()) {
                    int scale = attribute.getIntegerField(AttributeFields.PRECISION);
                    int valueScale = eitherNumber.getRight().get().scale();

                    if (valueScale > scale) {
                        masterOrderProductAttrValue.addError(masterOrderProductAttrValueDD.getField(MasterOrderProductAttrValueFields.VALUE),
                                "qcadooView.validate.field.error.invalidScale.max", String.valueOf(scale));

                        return false;
                    }

                    masterOrderProductAttrValue.setField(MasterOrderProductAttrValueFields.VALUE, BigDecimalUtils.toString(eitherNumber.getRight().get(),
                            attribute.getIntegerField(AttributeFields.PRECISION)));
                }
            } else {
                masterOrderProductAttrValue.addError(masterOrderProductAttrValueDD.getField(MasterOrderProductAttrValueFields.VALUE),
                        "qcadooView.validate.field.error.invalidNumericFormat");

                return false;
            }
        }

        return !checkIfValueExists(masterOrderProductAttrValueDD, masterOrderProductAttrValue);
    }

    private boolean checkIfValueExists(final DataDefinition masterOrderProductAttrValueDD, final Entity masterOrderProductAttrValue) {
        Entity attribute = masterOrderProductAttrValue.getBelongsToField(MasterOrderProductAttrValueFields.ATTRIBUTE);
        Entity masterOrderProduct = masterOrderProductAttrValue.getBelongsToField(MasterOrderProductAttrValueFields.MASTER_ORDER_PRODUCT);
        Entity attributeValue = masterOrderProductAttrValue.getBelongsToField(MasterOrderProductAttrValueFields.ATTRIBUTE_VALUE);

        List<Entity> masterOrderProductAttrValuesAdded = masterOrderProduct.getHasManyField(MasterOrderProductFields.MASTER_ORDER_PRODUCT_ATTR_VALUES);

        List<Entity> sameValue;

        if (Objects.nonNull(attributeValue)) {
            sameValue = getAttributeValueSameValues(masterOrderProductAttrValuesAdded, masterOrderProductAttrValue, attribute, attributeValue);

            if (!sameValue.isEmpty()) {
                masterOrderProductAttrValue.addError(masterOrderProductAttrValueDD.getField(MasterOrderProductAttrValueFields.ATTRIBUTE_VALUE),
                        "basic.attributeValue.error.valueExists");

                return true;
            }
        } else {
            sameValue = getAttributeSameValues(masterOrderProductAttrValuesAdded, masterOrderProductAttrValue, attribute);

            if (!sameValue.isEmpty()) {
                masterOrderProductAttrValue.addError(masterOrderProductAttrValueDD.getField(MasterOrderProductAttrValueFields.VALUE),
                        "basic.attributeValue.error.valueExists");

                return true;
            }
        }

        return false;
    }

    private List<Entity> getAttributeValueSameValues(final List<Entity> masterOrderProductAttrValuesAdded, final Entity masterOrderProductAttrValue, final Entity attribute, final Entity attributeValue) {
        return masterOrderProductAttrValuesAdded
                .stream()
                .filter(masterOrderProductAttrValueAdded -> masterOrderProductAttrValueAdded.getBelongsToField(MasterOrderProductAttrValueFields.ATTRIBUTE).getId().equals(attribute.getId())
                        && Objects.nonNull(masterOrderProductAttrValueAdded.getBelongsToField(MasterOrderProductAttrValueFields.ATTRIBUTE_VALUE))
                        && masterOrderProductAttrValueAdded.getBelongsToField(MasterOrderProductAttrValueFields.ATTRIBUTE_VALUE).getId()
                        .equals(attributeValue.getId()))
                .filter(val -> !val.getId().equals(masterOrderProductAttrValue.getId())).collect(Collectors.toList());
    }

    private List<Entity> getAttributeSameValues(final List<Entity> masterOrderProductAttrValuesAdded, final Entity masterOrderProductAttrValue, final Entity attribute) {
        return masterOrderProductAttrValuesAdded
                .stream()
                .filter(masterOrderProductAttrValueAdded -> masterOrderProductAttrValueAdded.getBelongsToField(MasterOrderProductAttrValueFields.ATTRIBUTE).getId().equals(attribute.getId())
                        && Objects.isNull(masterOrderProductAttrValueAdded.getBelongsToField(MasterOrderProductAttrValueFields.ATTRIBUTE_VALUE))
                        && ((Objects.isNull(masterOrderProductAttrValueAdded.getStringField(MasterOrderProductAttrValueFields.VALUE)) &&
                        Objects.isNull(masterOrderProductAttrValue.getStringField(MasterOrderProductAttrValueFields.VALUE)))
                        || (Objects.nonNull(masterOrderProductAttrValueAdded.getStringField(MasterOrderProductAttrValueFields.VALUE)) &&
                        masterOrderProductAttrValueAdded.getStringField(MasterOrderProductAttrValueFields.VALUE).equals(masterOrderProductAttrValue.getStringField(MasterOrderProductAttrValueFields.VALUE)))))
                .filter(val -> !val.getId().equals(masterOrderProductAttrValue.getId())).collect(Collectors.toList());
    }


    public void onSave(final DataDefinition masterOrderProductAttrValueDD, final Entity masterOrderProductAttrValue) {
        Entity attribute = masterOrderProductAttrValue.getBelongsToField(MasterOrderProductAttrValueFields.ATTRIBUTE);

        if (AttributeValueType.NUMERIC.getStringValue().equals(attribute.getStringField(AttributeFields.VALUE_TYPE))) {
            Either<Exception, Optional<BigDecimal>> eitherNumber = BigDecimalUtils.tryParseAndIgnoreSeparator(
                    masterOrderProductAttrValue.getStringField(MasterOrderProductAttrValueFields.VALUE), LocaleContextHolder.getLocale());

            if (eitherNumber.isRight() && eitherNumber.getRight().isPresent()) {
                masterOrderProductAttrValue.setField(MasterOrderProductAttrValueFields.VALUE, BigDecimalUtils.toString(eitherNumber.getRight().get(),
                        attribute.getIntegerField(AttributeFields.PRECISION)));
            }
        }
    }

}
