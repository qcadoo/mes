package com.qcadoo.mes.basic.hooks;

import com.google.common.base.Optional;
import com.qcadoo.commons.functional.Either;
import com.qcadoo.mes.basic.constants.*;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ProductAttributeValueHooks {

    @Autowired
    private NumberService numberService;

    public boolean validate(final DataDefinition productAttributeValueDD, final Entity productAttributeValue) {
        Entity product = productAttributeValue.getBelongsToField(ProductAttributeValueFields.PRODUCT);
        Entity attribute = productAttributeValue.getBelongsToField(ProductAttributeValueFields.ATTRIBUTE);

        String entityType = product.getStringField(ProductFields.ENTITY_TYPE);

        if (AttributeDataType.CALCULATED.getStringValue().equals(attribute.getStringField(AttributeFields.DATA_TYPE))
                && Objects.isNull(productAttributeValue.getBelongsToField(ProductAttributeValueFields.ATTRIBUTE_VALUE))) {
            if (ProductFamilyElementType.PARTICULAR_PRODUCT.getStringValue().equals(entityType)) {
                productAttributeValue.addError(productAttributeValueDD.getField(ProductAttributeValueFields.ATTRIBUTE_VALUE),
                        "qcadooView.validate.field.error.missing");

                return false;
            }
        }

        if (AttributeDataType.CONTINUOUS.getStringValue().equals(attribute.getStringField(AttributeFields.DATA_TYPE))
                && AttributeValueType.NUMERIC.getStringValue().equals(attribute.getStringField(AttributeFields.VALUE_TYPE))) {
            Either<Exception, Optional<BigDecimal>> eitherNumber = BigDecimalUtils.tryParseAndIgnoreSeparator(
                    productAttributeValue.getStringField(ProductAttributeValueFields.VALUE), LocaleContextHolder.getLocale());

            if (eitherNumber.isRight() && eitherNumber.getRight().isPresent()) {
                int scale = attribute.getIntegerField(AttributeFields.PRECISION);
                int valueScale = eitherNumber.getRight().get().scale();

                if (valueScale > scale) {
                    productAttributeValue.addError(productAttributeValueDD.getField(ProductAttributeValueFields.VALUE),
                            "qcadooView.validate.field.error.invalidScale.max", String.valueOf(scale));

                    return false;
                }

                productAttributeValue
                        .setField(
                                ProductAttributeValueFields.VALUE,
                                BigDecimalUtils.toString(eitherNumber.getRight().get(),
                                        attribute.getIntegerField(AttributeFields.PRECISION)));
            } else {
                if (ProductFamilyElementType.PARTICULAR_PRODUCT.getStringValue().equals(entityType)) {
                    productAttributeValue.addError(productAttributeValueDD.getField(ProductAttributeValueFields.VALUE),
                            "qcadooView.validate.field.error.invalidNumericFormat");

                    return false;
                }
            }
        }

        return !checkIfValueExists(productAttributeValueDD, productAttributeValue);
    }

    private boolean checkIfValueExists(final DataDefinition productAttributeValueDD, final Entity productAttributeValue) {
        Entity product = productAttributeValue.getBelongsToField(ProductAttributeValueFields.PRODUCT);
        Entity attribute = productAttributeValue.getBelongsToField(ProductAttributeValueFields.ATTRIBUTE);
        Entity attributeValue = productAttributeValue.getBelongsToField(ProductAttributeValueFields.ATTRIBUTE_VALUE);

        List<Entity> productAttributeValues = product.getHasManyField(ProductFields.PRODUCT_ATTRIBUTE_VALUES);

        List sameValue;

        if (Objects.nonNull(attributeValue)) {
            sameValue = productAttributeValues
                    .stream()
                    .filter(val -> val.getBelongsToField(ProductAttributeValueFields.ATTRIBUTE).getId().equals(attribute.getId())
                            && Objects.nonNull(val.getBelongsToField(ProductAttributeValueFields.ATTRIBUTE_VALUE))
                            && val.getBelongsToField(ProductAttributeValueFields.ATTRIBUTE_VALUE).getId()
                            .equals(attributeValue.getId()))
                    .filter(val -> !val.getId().equals(productAttributeValue.getId())).collect(Collectors.toList());

            if (!sameValue.isEmpty()) {
                productAttributeValue.addError(productAttributeValueDD.getField(ProductAttributeValueFields.ATTRIBUTE_VALUE),
                        "basic.attributeValue.error.valueExists");

                return true;
            }
        } else {
            sameValue = productAttributeValues
                    .stream()
                    .filter(val -> val.getBelongsToField(ProductAttributeValueFields.ATTRIBUTE).getId().equals(attribute.getId())
                            && Objects.isNull(val.getBelongsToField(ProductAttributeValueFields.ATTRIBUTE_VALUE))
                            && ((Objects.isNull(val.getStringField(ProductAttributeValueFields.VALUE)) &&
                            Objects.isNull(productAttributeValue.getStringField(ProductAttributeValueFields.VALUE)))
                            || (Objects.nonNull(val.getStringField(ProductAttributeValueFields.VALUE)) &&
                            val.getStringField(ProductAttributeValueFields.VALUE).equals(productAttributeValue.getStringField(ProductAttributeValueFields.VALUE)))))
                    .filter(val -> !val.getId().equals(productAttributeValue.getId())).collect(Collectors.toList());

            if (!sameValue.isEmpty()) {
                if (AttributeDataType.CALCULATED.getStringValue().equals(attribute.getStringField(AttributeFields.DATA_TYPE))) {
                    productAttributeValue.addError(productAttributeValueDD.getField(ProductAttributeValueFields.ATTRIBUTE_VALUE),
                            "basic.attributeValue.error.valueExists");
                } else{
                    productAttributeValue.addError(productAttributeValueDD.getField(ProductAttributeValueFields.VALUE),
                            "basic.attributeValue.error.valueExists");
                }

                return true;
            }
        }

        return false;
    }

    public void onSave(final DataDefinition productAttributeValueDD, final Entity productAttributeValue) {
        Entity attribute = productAttributeValue.getBelongsToField(ProductAttributeValueFields.ATTRIBUTE);

        if (AttributeValueType.NUMERIC.getStringValue().equals(attribute.getStringField(AttributeFields.VALUE_TYPE))) {
            Either<Exception, Optional<BigDecimal>> eitherNumber = BigDecimalUtils.tryParseAndIgnoreSeparator(
                    productAttributeValue.getStringField(ProductAttributeValueFields.VALUE), LocaleContextHolder.getLocale());

            if (eitherNumber.isRight() && eitherNumber.getRight().isPresent()) {
                productAttributeValue.setField(
                        ProductAttributeValueFields.VALUE,
                        BigDecimalUtils.toString(eitherNumber.getRight().get(),
                                attribute.getIntegerField(AttributeFields.PRECISION)));
            }
        }
    }

}
