package com.qcadoo.mes.basic.hooks;

import com.google.common.base.Optional;
import com.qcadoo.commons.functional.Either;
import com.qcadoo.mes.basic.constants.*;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import org.apache.commons.lang3.StringUtils;
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

        String dataType = attribute.getStringField(AttributeFields.DATA_TYPE);
        String valueType = attribute.getStringField(AttributeFields.VALUE_TYPE);

        if (AttributeDataType.CALCULATED.getStringValue().equals(dataType)
                && Objects.isNull(productAttributeValue.getBelongsToField(ProductAttributeValueFields.ATTRIBUTE_VALUE))) {
            productAttributeValue.setField(ProductAttributeValueFields.VALUE, null);

            if (ProductFamilyElementType.PARTICULAR_PRODUCT.getStringValue().equals(entityType)) {
                productAttributeValue.addError(productAttributeValueDD.getField(ProductAttributeValueFields.ATTRIBUTE_VALUE),
                        "qcadooView.validate.field.error.missing");

                return false;
            }
        }

        if (AttributeDataType.CONTINUOUS.getStringValue().equals(dataType)) {
            String value = productAttributeValue.getStringField(ProductAttributeValueFields.VALUE);

            if (AttributeValueType.NUMERIC.getStringValue().equals(valueType)) {
                Either<Exception, Optional<BigDecimal>> eitherNumber = BigDecimalUtils.tryParseAndIgnoreSeparator(value,
                        LocaleContextHolder.getLocale());

                if (eitherNumber.isRight()) {
                    if (eitherNumber.getRight().isPresent()) {
                        int precision = attribute.getIntegerField(AttributeFields.PRECISION);
                        int valueScale = eitherNumber.getRight().get().stripTrailingZeros().scale();

                        if (valueScale > precision) {
                            productAttributeValue.addError(productAttributeValueDD.getField(ProductAttributeValueFields.VALUE),
                                    "qcadooView.validate.field.error.invalidScale.max", String.valueOf(precision));

                            return false;
                        }

                        productAttributeValue
                                .setField(ProductAttributeValueFields.VALUE,
                                        numberService.formatWithMinimumFractionDigits(eitherNumber.getRight().get(), 0));
                    } else {
                        if (ProductFamilyElementType.PARTICULAR_PRODUCT.getStringValue().equals(entityType)) {
                            productAttributeValue.addError(productAttributeValueDD.getField(ProductAttributeValueFields.VALUE),
                                    "qcadooView.validate.field.error.missing");

                            return false;
                        }
                    }
                } else {
                    productAttributeValue.addError(productAttributeValueDD.getField(ProductAttributeValueFields.VALUE),
                            "qcadooView.validate.field.error.invalidNumericFormat");

                    return false;
                }
            }

            if (AttributeValueType.TEXT.getStringValue().equals(valueType)) {
                if (StringUtils.isEmpty(value)) {
                    if (ProductFamilyElementType.PARTICULAR_PRODUCT.getStringValue().equals(entityType)) {
                        productAttributeValue.addError(productAttributeValueDD.getField(ProductAttributeValueFields.VALUE),
                                "qcadooView.validate.field.error.missing");

                        return false;
                    }
                }
            }
        }

        return !checkIfValueExists(productAttributeValueDD, productAttributeValue);
    }

    private boolean checkIfValueExists(final DataDefinition productAttributeValueDD, final Entity productAttributeValue) {
        Entity product = productAttributeValue.getBelongsToField(ProductAttributeValueFields.PRODUCT);
        Entity attribute = productAttributeValue.getBelongsToField(ProductAttributeValueFields.ATTRIBUTE);
        Entity attributeValue = productAttributeValue.getBelongsToField(ProductAttributeValueFields.ATTRIBUTE_VALUE);

        List<Entity> productAttributeValuesAdded = product.getHasManyField(ProductFields.PRODUCT_ATTRIBUTE_VALUES);

        List<Entity> sameValue;

        if (Objects.nonNull(attributeValue)) {
            sameValue = getAttributeValueSamesValues(productAttributeValuesAdded, productAttributeValue, attribute, attributeValue);

            if (!sameValue.isEmpty()) {
                productAttributeValue.addError(productAttributeValueDD.getField(ProductAttributeValueFields.ATTRIBUTE_VALUE),
                        "basic.attributeValue.error.valueExists");

                return true;
            }
        } else {
            sameValue = getAttributeSameValues(productAttributeValuesAdded, productAttributeValue, attribute);

            if (!sameValue.isEmpty()) {
                if (AttributeDataType.CALCULATED.getStringValue().equals(attribute.getStringField(AttributeFields.DATA_TYPE))) {
                    productAttributeValue.addError(productAttributeValueDD.getField(ProductAttributeValueFields.ATTRIBUTE_VALUE),
                            "basic.attributeValue.error.valueExists");
                } else {
                    productAttributeValue.addError(productAttributeValueDD.getField(ProductAttributeValueFields.VALUE),
                            "basic.attributeValue.error.valueExists");
                }

                return true;
            }
        }

        return false;
    }

    private List<Entity> getAttributeValueSamesValues(final List<Entity> productAttributeValuesAdded, final Entity productAttributeValue, final Entity attribute, final Entity attributeValue) {
        return productAttributeValuesAdded
                .stream()
                .filter(productAttributeValueAdded -> productAttributeValueAdded.getBelongsToField(ProductAttributeValueFields.ATTRIBUTE).getId().equals(attribute.getId())
                        && Objects.nonNull(productAttributeValueAdded.getBelongsToField(ProductAttributeValueFields.ATTRIBUTE_VALUE))
                        && productAttributeValueAdded.getBelongsToField(ProductAttributeValueFields.ATTRIBUTE_VALUE).getId()
                        .equals(attributeValue.getId()))
                .filter(val -> !val.getId().equals(productAttributeValue.getId())).collect(Collectors.toList());
    }

    private List<Entity> getAttributeSameValues(final List<Entity> productAttributeValuesAdded, final Entity productAttributeValue, final Entity attribute) {
        return productAttributeValuesAdded
                .stream()
                .filter(productAttributeValueAdded -> productAttributeValueAdded.getBelongsToField(ProductAttributeValueFields.ATTRIBUTE).getId().equals(attribute.getId())
                        && Objects.isNull(productAttributeValueAdded.getBelongsToField(ProductAttributeValueFields.ATTRIBUTE_VALUE))
                        && ((Objects.isNull(productAttributeValueAdded.getStringField(ProductAttributeValueFields.VALUE)) &&
                        Objects.isNull(productAttributeValue.getStringField(ProductAttributeValueFields.VALUE)))
                        || (Objects.nonNull(productAttributeValueAdded.getStringField(ProductAttributeValueFields.VALUE)) &&
                        productAttributeValueAdded.getStringField(ProductAttributeValueFields.VALUE).equals(productAttributeValue.getStringField(ProductAttributeValueFields.VALUE)))))
                .filter(val -> !val.getId().equals(productAttributeValue.getId())).collect(Collectors.toList());
    }

    public void onSave(final DataDefinition productAttributeValueDD, final Entity productAttributeValue) {
        Entity attribute = productAttributeValue.getBelongsToField(ProductAttributeValueFields.ATTRIBUTE);
        Entity attributeValue = productAttributeValue.getBelongsToField(ProductAttributeValueFields.ATTRIBUTE_VALUE);

        if (AttributeDataType.CALCULATED.getStringValue().equals(attribute.getStringField(AttributeFields.DATA_TYPE))) {
            if (Objects.isNull(attributeValue)) {
                productAttributeValue.setField(
                        ProductAttributeValueFields.VALUE, null);
            }
        }

        if (AttributeValueType.NUMERIC.getStringValue().equals(attribute.getStringField(AttributeFields.VALUE_TYPE))) {
            Either<Exception, Optional<BigDecimal>> eitherNumber = BigDecimalUtils.tryParseAndIgnoreSeparator(
                    productAttributeValue.getStringField(ProductAttributeValueFields.VALUE), LocaleContextHolder.getLocale());

            if (eitherNumber.isRight() && eitherNumber.getRight().isPresent()) {
                productAttributeValue.setField(
                        ProductAttributeValueFields.VALUE,
                        numberService.formatWithMinimumFractionDigits(eitherNumber.getRight().get(), 0));
            }
        }
    }

}
