package com.qcadoo.mes.masterOrders.hooks;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

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

@Service
public class MasterOrderProductAttrValueHooks {

    public boolean validate(final DataDefinition dataDefinition, final Entity entity) {
        Entity attribute = entity.getBelongsToField(MasterOrderProductAttrValueFields.ATTRIBUTE);

        if (AttributeDataType.CALCULATED.getStringValue().equals(attribute.getStringField(AttributeFields.DATA_TYPE))
                && Objects.isNull(entity.getBelongsToField(MasterOrderProductAttrValueFields.ATTRIBUTE_VALUE))) {
            entity.addError(dataDefinition.getField(MasterOrderProductAttrValueFields.ATTRIBUTE_VALUE),
                    "qcadooView.validate.field.error.missing");
            return false;
        }

        if (AttributeDataType.CONTINUOUS.getStringValue().equals(attribute.getStringField(AttributeFields.DATA_TYPE))
                && AttributeValueType.NUMERIC.getStringValue().equals(attribute.getStringField(AttributeFields.VALUE_TYPE))) {
            Either<Exception, Optional<BigDecimal>> eitherNumber = BigDecimalUtils.tryParseAndIgnoreSeparator(
                    entity.getStringField(MasterOrderProductAttrValueFields.VALUE), LocaleContextHolder.getLocale());
            if (eitherNumber.isRight() && eitherNumber.getRight().isPresent()) {
                int scale = attribute.getIntegerField(AttributeFields.PRECISION);
                int valueScale = eitherNumber.getRight().get().scale();
                if (valueScale > scale) {
                    entity.addError(dataDefinition.getField(MasterOrderProductAttrValueFields.VALUE),
                            "qcadooView.validate.field.error.invalidScale.max", String.valueOf(scale));
                    return false;
                }
            } else {
                entity.addError(dataDefinition.getField(MasterOrderProductAttrValueFields.VALUE),
                        "qcadooView.validate.field.error.invalidNumericFormat");
                return false;
            }
            entity.setField(MasterOrderProductAttrValueFields.VALUE, BigDecimalUtils.toString(eitherNumber.getRight().get(),
                    attribute.getIntegerField(AttributeFields.PRECISION)));
        }
        return !checkIfValueExists(dataDefinition, entity);
    }

    private boolean checkIfValueExists(DataDefinition dataDefinition, Entity entity) {
        Entity attribute = entity.getBelongsToField(MasterOrderProductAttrValueFields.ATTRIBUTE);
        Entity masterOrderProduct = entity.getBelongsToField(MasterOrderProductAttrValueFields.MASTER_ORDER_PRODUCT);
        Entity attributeValue = entity.getBelongsToField(MasterOrderProductAttrValueFields.ATTRIBUTE_VALUE);

        List<Entity> values = masterOrderProduct.getHasManyField(MasterOrderProductFields.MASTER_ORDER_PRODUCT_ATTR_VALUES);

        List sameValue;
        if (Objects.nonNull(attributeValue)) {
            sameValue = values.stream().filter(
                    val -> val.getBelongsToField(MasterOrderProductAttrValueFields.ATTRIBUTE).getId().equals(attribute.getId())
                            && Objects.nonNull(val.getBelongsToField(MasterOrderProductAttrValueFields.ATTRIBUTE_VALUE))
                            && val.getBelongsToField(MasterOrderProductAttrValueFields.ATTRIBUTE_VALUE).getId()
                                    .equals(attributeValue.getId()))
                    .filter(val -> !val.getId().equals(entity.getId())).collect(Collectors.toList());
            if (!sameValue.isEmpty()) {
                entity.addError(dataDefinition.getField(MasterOrderProductAttrValueFields.ATTRIBUTE_VALUE),
                        "basic.attributeValue.error.valueExists");
                return true;
            }
        } else {
            sameValue = values.stream().filter(
                    val -> val.getBelongsToField(MasterOrderProductAttrValueFields.ATTRIBUTE).getId().equals(attribute.getId())
                            && Objects.isNull(val.getBelongsToField(MasterOrderProductAttrValueFields.ATTRIBUTE_VALUE))
                            && val.getStringField(MasterOrderProductAttrValueFields.VALUE)
                                    .equals(entity.getStringField(MasterOrderProductAttrValueFields.VALUE)))
                    .filter(val -> !val.getId().equals(entity.getId())).collect(Collectors.toList());
            if (!sameValue.isEmpty()) {
                entity.addError(dataDefinition.getField(MasterOrderProductAttrValueFields.VALUE),
                        "basic.attributeValue.error.valueExists");
                return true;
            }
        }

        return false;
    }

    public void onSave(final DataDefinition dataDefinition, final Entity entity) {
        Entity attribute = entity.getBelongsToField(MasterOrderProductAttrValueFields.ATTRIBUTE);
        if (AttributeValueType.NUMERIC.getStringValue().equals(attribute.getStringField(AttributeFields.VALUE_TYPE))) {
            Either<Exception, Optional<BigDecimal>> eitherNumber = BigDecimalUtils.tryParseAndIgnoreSeparator(
                    entity.getStringField(MasterOrderProductAttrValueFields.VALUE), LocaleContextHolder.getLocale());
            if (eitherNumber.isRight() && eitherNumber.getRight().isPresent()) {
                entity.setField(MasterOrderProductAttrValueFields.VALUE, BigDecimalUtils.toString(eitherNumber.getRight().get(),
                        attribute.getIntegerField(AttributeFields.PRECISION)));
            }
        }
    }
}
