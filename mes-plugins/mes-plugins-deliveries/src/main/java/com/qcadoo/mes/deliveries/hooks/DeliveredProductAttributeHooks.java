package com.qcadoo.mes.deliveries.hooks;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.qcadoo.commons.functional.Either;
import com.qcadoo.mes.basic.constants.AttributeDataType;
import com.qcadoo.mes.basic.constants.AttributeFields;
import com.qcadoo.mes.basic.constants.AttributeValueType;
import com.qcadoo.mes.basic.constants.ProductAttributeValueFields;
import com.qcadoo.mes.deliveries.constants.DeliveredProductAttributeValFields;
import com.qcadoo.mes.deliveries.constants.DeliveredProductFields;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
public class DeliveredProductAttributeHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    public boolean validate(final DataDefinition resourceAttributeValueDD, final Entity resourceAttributeValue) {
        Entity attribute = resourceAttributeValue.getBelongsToField(DeliveredProductAttributeValFields.ATTRIBUTE);

        if (AttributeDataType.CALCULATED.getStringValue().equals(attribute.getStringField(AttributeFields.DATA_TYPE))
                && Objects.isNull(resourceAttributeValue.getBelongsToField(DeliveredProductAttributeValFields.ATTRIBUTE_VALUE))) {
            resourceAttributeValue.addError(
                    resourceAttributeValueDD.getField(DeliveredProductAttributeValFields.ATTRIBUTE_VALUE),
                    "qcadooView.validate.field.error.missing");
            return false;
        }

        if (AttributeDataType.CONTINUOUS.getStringValue().equals(attribute.getStringField(AttributeFields.DATA_TYPE))
                && AttributeValueType.NUMERIC.getStringValue().equals(attribute.getStringField(AttributeFields.VALUE_TYPE))) {
            Either<Exception, Optional<BigDecimal>> eitherNumber = BigDecimalUtils.tryParseAndIgnoreSeparator(
                    resourceAttributeValue.getStringField(DeliveredProductAttributeValFields.VALUE),
                    LocaleContextHolder.getLocale());
            if (eitherNumber.isRight() && eitherNumber.getRight().isPresent()) {
                int scale = attribute.getIntegerField(AttributeFields.PRECISION);
                int valueScale = eitherNumber.getRight().get().scale();
                if (valueScale > scale) {
                    resourceAttributeValue.addError(resourceAttributeValueDD.getField(DeliveredProductAttributeValFields.VALUE),
                            "qcadooView.validate.field.error.invalidScale.max", String.valueOf(scale));
                    return false;
                }
            } else {
                resourceAttributeValue.addError(resourceAttributeValueDD.getField(DeliveredProductAttributeValFields.VALUE),
                        "qcadooView.validate.field.error.invalidNumericFormat");
                return false;
            }
            resourceAttributeValue
                    .setField(
                            ProductAttributeValueFields.VALUE,
                            BigDecimalUtils.toString(eitherNumber.getRight().get(),
                                    attribute.getIntegerField(AttributeFields.PRECISION)));
        }
        return !checkIfValueExists(resourceAttributeValueDD, resourceAttributeValue);
    }

    private boolean checkIfValueExists(DataDefinition resourceAttributeValueDD, Entity resourceAttributeValue) {
        Entity attribute = resourceAttributeValue.getBelongsToField(DeliveredProductAttributeValFields.ATTRIBUTE);
        Entity deliveredProduct = resourceAttributeValue.getBelongsToField(DeliveredProductAttributeValFields.DELIVERED_PRODUCT);
        Entity attributeValue = resourceAttributeValue.getBelongsToField(DeliveredProductAttributeValFields.ATTRIBUTE_VALUE);

        List<Entity> values = deliveredProduct.getHasManyField(DeliveredProductFields.DELIVERED_PRODUCT_ATTRIBUTE_VALS);

        List sameValue = Lists.newArrayList();
        if (Objects.nonNull(attributeValue)) {
            sameValue = values
                    .stream()
                    .filter(val -> val.getBelongsToField(DeliveredProductAttributeValFields.ATTRIBUTE).getId()
                            .equals(attribute.getId())
                            && Objects.nonNull(val.getBelongsToField(DeliveredProductAttributeValFields.ATTRIBUTE_VALUE))
                            && val.getBelongsToField(DeliveredProductAttributeValFields.ATTRIBUTE_VALUE).getId()
                                    .equals(attributeValue.getId()))
                    .filter(val -> !val.getId().equals(resourceAttributeValue.getId())).collect(Collectors.toList());
            if (!sameValue.isEmpty()) {
                resourceAttributeValue.addError(
                        resourceAttributeValueDD.getField(DeliveredProductAttributeValFields.ATTRIBUTE_VALUE),
                        "basic.attributeValue.error.valueExists");
                return true;
            }
        } else {
            sameValue = values
                    .stream()
                    .filter(val -> val.getBelongsToField(DeliveredProductAttributeValFields.ATTRIBUTE).getId()
                            .equals(attribute.getId())
                            && Objects.isNull(val.getBelongsToField(DeliveredProductAttributeValFields.ATTRIBUTE_VALUE))
                            && val.getStringField(DeliveredProductAttributeValFields.VALUE).equals(
                                    resourceAttributeValue.getStringField(DeliveredProductAttributeValFields.VALUE)))
                    .filter(val -> !val.getId().equals(resourceAttributeValue.getId())).collect(Collectors.toList());
            if (!sameValue.isEmpty()) {
                resourceAttributeValue.addError(resourceAttributeValueDD.getField(DeliveredProductAttributeValFields.VALUE),
                        "basic.attributeValue.error.valueExists");
                return true;
            }
        }

        return false;
    }

    public void onSave(final DataDefinition attributeValueDD, final Entity attributeValue) {
        Entity attribute = attributeValue.getBelongsToField(DeliveredProductAttributeValFields.ATTRIBUTE);
        if (AttributeValueType.NUMERIC.getStringValue().equals(attribute.getStringField(AttributeFields.VALUE_TYPE))) {
            Either<Exception, Optional<BigDecimal>> eitherNumber = BigDecimalUtils.tryParseAndIgnoreSeparator(
                    attributeValue.getStringField(DeliveredProductAttributeValFields.VALUE), LocaleContextHolder.getLocale());
            if (eitherNumber.isRight() && eitherNumber.getRight().isPresent()) {
                attributeValue.setField(
                        DeliveredProductAttributeValFields.VALUE,
                        BigDecimalUtils.toString(eitherNumber.getRight().get(),
                                attribute.getIntegerField(AttributeFields.PRECISION)));
            }
        }
    }
}
