package com.qcadoo.mes.masterOrders.hooks;

import com.google.common.base.Optional;
import com.qcadoo.commons.functional.Either;
import com.qcadoo.mes.basic.constants.*;
import com.qcadoo.mes.masterOrders.constants.ProductsByAttributeEntryHelperFields;
import com.qcadoo.mes.masterOrders.constants.ProductsByAttributeHelperFields;
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
public class ProductsByAttributeEntryHelperHooks {

    @Autowired
    private NumberService numberService;

    public boolean validate(final DataDefinition productsByAttributeEntryHelperDD, final Entity productsByAttributeEntryHelper) {
        Entity product = productsByAttributeEntryHelper.getBelongsToField(ProductsByAttributeEntryHelperFields.PRODUCT);
        Entity attribute = productsByAttributeEntryHelper.getBelongsToField(ProductsByAttributeEntryHelperFields.ATTRIBUTE);

        String entityType = product.getStringField(ProductFields.ENTITY_TYPE);

        if (AttributeDataType.CALCULATED.getStringValue().equals(attribute.getStringField(AttributeFields.DATA_TYPE))
                && Objects.isNull(productsByAttributeEntryHelper.getBelongsToField(ProductsByAttributeEntryHelperFields.ATTRIBUTE_VALUE))) {
            productsByAttributeEntryHelper.setField(ProductsByAttributeEntryHelperFields.VALUE, null);

            if (ProductFamilyElementType.PARTICULAR_PRODUCT.getStringValue().equals(entityType)) {
                productsByAttributeEntryHelper.addError(productsByAttributeEntryHelperDD.getField(ProductsByAttributeEntryHelperFields.ATTRIBUTE_VALUE),
                        "qcadooView.validate.field.error.missing");

                return false;
            }
        }

        if (AttributeDataType.CONTINUOUS.getStringValue().equals(attribute.getStringField(AttributeFields.DATA_TYPE))) {
            String value = productsByAttributeEntryHelper.getStringField(ProductsByAttributeEntryHelperFields.VALUE);

            if (AttributeValueType.NUMERIC.getStringValue().equals(attribute.getStringField(AttributeFields.VALUE_TYPE))) {
                Either<Exception, Optional<BigDecimal>> eitherNumber = BigDecimalUtils.tryParseAndIgnoreSeparator(value,
                        LocaleContextHolder.getLocale());

                if (eitherNumber.isRight()) {
                    if (eitherNumber.getRight().isPresent()) {
                        int scale = attribute.getIntegerField(AttributeFields.PRECISION);
                        int valueScale = eitherNumber.getRight().get().scale();

                        if (valueScale > scale) {
                            productsByAttributeEntryHelper.addError(productsByAttributeEntryHelperDD.getField(ProductsByAttributeEntryHelperFields.VALUE),
                                    "qcadooView.validate.field.error.invalidScale.max", String.valueOf(scale));

                            return false;
                        }

                        productsByAttributeEntryHelper
                                .setField(
                                        ProductsByAttributeEntryHelperFields.VALUE,
                                        BigDecimalUtils.toString(eitherNumber.getRight().get(),
                                                attribute.getIntegerField(AttributeFields.PRECISION)));
                    } else {
                        if (ProductFamilyElementType.PARTICULAR_PRODUCT.getStringValue().equals(entityType)) {
                            productsByAttributeEntryHelper.addError(productsByAttributeEntryHelperDD.getField(ProductsByAttributeEntryHelperFields.VALUE),
                                    "qcadooView.validate.field.error.missing");

                            return false;
                        }
                    }
                } else {
                    productsByAttributeEntryHelper.addError(productsByAttributeEntryHelperDD.getField(ProductsByAttributeEntryHelperFields.VALUE),
                            "qcadooView.validate.field.error.invalidNumericFormat");

                    return false;
                }
            }

            if (AttributeValueType.TEXT.getStringValue().equals(attribute.getStringField(AttributeFields.VALUE_TYPE))) {
                if (StringUtils.isEmpty(value)) {
                    if (ProductFamilyElementType.PARTICULAR_PRODUCT.getStringValue().equals(entityType)) {
                        productsByAttributeEntryHelper.addError(productsByAttributeEntryHelperDD.getField(ProductsByAttributeEntryHelperFields.VALUE),
                                "qcadooView.validate.field.error.missing");

                        return false;
                    }
                }
            }
        }

        return !checkIfValueExists(productsByAttributeEntryHelperDD, productsByAttributeEntryHelper);
    }

    private boolean checkIfValueExists(final DataDefinition productsByAttributeEntryHelperDD, final Entity productsByAttributeEntryHelper) {
        Entity productsByAttributeHelper = productsByAttributeEntryHelper.getBelongsToField(ProductsByAttributeEntryHelperFields.PRODUCTS_BY_ATTRIBUTE_HELPER);
        Entity attribute = productsByAttributeEntryHelper.getBelongsToField(ProductsByAttributeEntryHelperFields.ATTRIBUTE);
        Entity attributeValue = productsByAttributeEntryHelper.getBelongsToField(ProductsByAttributeEntryHelperFields.ATTRIBUTE_VALUE);

        List<Entity> productsByAttributeEntryHelpersAdded = productsByAttributeHelper.getHasManyField(ProductsByAttributeHelperFields.PRODUCTS_BY_ATTRIBUTE_ENTRY_HELPERS);

        List<Entity> sameValue;

        if (Objects.nonNull(attributeValue)) {
            sameValue = getAttributeValueSameValues(productsByAttributeEntryHelpersAdded, productsByAttributeEntryHelper, attribute, attributeValue);

            if (!sameValue.isEmpty()) {
                productsByAttributeEntryHelper.addError(productsByAttributeEntryHelperDD.getField(ProductsByAttributeEntryHelperFields.ATTRIBUTE_VALUE),
                        "masterOrders.productsByAttributeEntryHelpers.error.valueExists");

                return true;
            }
        } else {
            sameValue = getAttributeSameValues(productsByAttributeEntryHelpersAdded, productsByAttributeEntryHelper, attribute);

            if (!sameValue.isEmpty()) {
                if (AttributeDataType.CALCULATED.getStringValue().equals(attribute.getStringField(AttributeFields.DATA_TYPE))) {
                    productsByAttributeEntryHelper.addError(productsByAttributeEntryHelperDD.getField(ProductsByAttributeEntryHelperFields.ATTRIBUTE_VALUE),
                            "masterOrders.productsByAttributeEntryHelpers.error.valueExists");
                } else {
                    productsByAttributeEntryHelper.addError(productsByAttributeEntryHelperDD.getField(ProductsByAttributeEntryHelperFields.VALUE),
                            "masterOrders.productsByAttributeEntryHelpers.error.valueExists");
                }

                return true;
            }
        }

        return false;
    }

    private List<Entity> getAttributeValueSameValues(final List<Entity> productsByAttributeEntryHelpersAdded, final Entity productsByAttributeEntryHelper, final Entity attribute, final Entity attributeValue) {
        return productsByAttributeEntryHelpersAdded
                .stream()
                .filter(productsByAttributeEntryHelperAdded -> productsByAttributeEntryHelperAdded.getBelongsToField(ProductsByAttributeEntryHelperFields.ATTRIBUTE).getId().equals(attribute.getId())
                        && Objects.nonNull(productsByAttributeEntryHelperAdded.getBelongsToField(ProductsByAttributeEntryHelperFields.ATTRIBUTE_VALUE))
                        && productsByAttributeEntryHelperAdded.getBelongsToField(ProductsByAttributeEntryHelperFields.ATTRIBUTE_VALUE).getId()
                        .equals(attributeValue.getId()))
                .filter(val -> !val.getId().equals(productsByAttributeEntryHelper.getId())).collect(Collectors.toList());
    }

    private List<Entity> getAttributeSameValues(final List<Entity> productsByAttributeEntryHelpersAdded, final Entity productsByAttributeEntryHelper, final Entity attribute) {
        return productsByAttributeEntryHelpersAdded
                .stream()
                .filter(productsByAttributeEntryHelperAdded -> productsByAttributeEntryHelperAdded.getBelongsToField(ProductsByAttributeEntryHelperFields.ATTRIBUTE).getId().equals(attribute.getId())
                        && Objects.isNull(productsByAttributeEntryHelperAdded.getBelongsToField(ProductsByAttributeEntryHelperFields.ATTRIBUTE_VALUE))
                        && ((Objects.isNull(productsByAttributeEntryHelperAdded.getStringField(ProductsByAttributeEntryHelperFields.VALUE)) &&
                        Objects.isNull(productsByAttributeEntryHelper.getStringField(ProductsByAttributeEntryHelperFields.VALUE)))
                        || (Objects.nonNull(productsByAttributeEntryHelperAdded.getStringField(ProductsByAttributeEntryHelperFields.VALUE)) &&
                        productsByAttributeEntryHelperAdded.getStringField(ProductsByAttributeEntryHelperFields.VALUE).equals(productsByAttributeEntryHelper.getStringField(ProductsByAttributeEntryHelperFields.VALUE)))))
                .filter(val -> !val.getId().equals(productsByAttributeEntryHelper.getId())).collect(Collectors.toList());
    }

    public void onSave(final DataDefinition productsByAttributeEntryHelperDD, final Entity productsByAttributeEntryHelper) {
        Entity attribute = productsByAttributeEntryHelper.getBelongsToField(ProductsByAttributeEntryHelperFields.ATTRIBUTE);
        Entity attributeValue = productsByAttributeEntryHelper.getBelongsToField(ProductsByAttributeEntryHelperFields.ATTRIBUTE_VALUE);

        if (AttributeDataType.CALCULATED.getStringValue().equals(attribute.getStringField(AttributeFields.DATA_TYPE))) {
            if (Objects.isNull(attributeValue)) {
                productsByAttributeEntryHelper.setField(
                        ProductsByAttributeEntryHelperFields.VALUE, null);
            }
        }

        if (AttributeValueType.NUMERIC.getStringValue().equals(attribute.getStringField(AttributeFields.VALUE_TYPE))) {
            Either<Exception, Optional<BigDecimal>> eitherNumber = BigDecimalUtils.tryParseAndIgnoreSeparator(
                    productsByAttributeEntryHelper.getStringField(ProductsByAttributeEntryHelperFields.VALUE), LocaleContextHolder.getLocale());

            if (eitherNumber.isRight() && eitherNumber.getRight().isPresent()) {
                productsByAttributeEntryHelper.setField(
                        ProductsByAttributeEntryHelperFields.VALUE,
                        BigDecimalUtils.toString(eitherNumber.getRight().get(),
                                attribute.getIntegerField(AttributeFields.PRECISION)));
            }
        }
    }

}
