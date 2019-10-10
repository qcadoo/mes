package com.qcadoo.mes.basic.hooks;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.qcadoo.commons.functional.Either;
import com.qcadoo.mes.basic.constants.AttributeDataType;
import com.qcadoo.mes.basic.constants.AttributeFields;
import com.qcadoo.mes.basic.constants.AttributeValueType;
import com.qcadoo.mes.basic.constants.ProductAttributeValueFields;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinition;
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
public class ProductAttributeValueHooks {

    @Autowired
    private NumberService numberService;

    public boolean validate(final DataDefinition productAttributeValueDD, final Entity productAttributeValue) {
        Entity attribute = productAttributeValue.getBelongsToField(ProductAttributeValueFields.ATTRIBUTE);

        if(AttributeDataType.CALCULATED.getStringValue().equals(attribute.getStringField(AttributeFields.DATA_TYPE))) {
            productAttributeValue.addError(productAttributeValueDD.getField(ProductAttributeValueFields.ATTRIBUTE_VALUE),
                    "qcadooView.validate.field.error.missing");
            return false;
        }

        if (AttributeDataType.CONTINUOUS.getStringValue().equals(attribute.getStringField(AttributeFields.DATA_TYPE))
                && AttributeValueType.NUMERIC.getStringValue().equals(attribute.getStringField(AttributeFields.VALUE_TYPE))) {
            Either<Exception, Optional<BigDecimal>> eitherNumber = BigDecimalUtils.tryParse(
                    productAttributeValue.getStringField(ProductAttributeValueFields.VALUE), LocaleContextHolder.getLocale());
            if (eitherNumber.isRight() && eitherNumber.getRight().isPresent()) {
                int scale = attribute.getIntegerField(AttributeFields.PRECISION);
                int valueScale = eitherNumber.getRight().get().scale();
                if (valueScale > scale) {
                    productAttributeValue.addError(productAttributeValueDD.getField(ProductAttributeValueFields.VALUE),
                            "qcadooView.validate.field.error.invalidScale.max", String.valueOf(scale));
                    return false;
                }
            } else {
                productAttributeValue.addError(productAttributeValueDD.getField(ProductAttributeValueFields.VALUE),
                        "qcadooView.validate.field.error.invalidNumericFormat");
                return false;
            }
        }
        if (checkIfValueExists(productAttributeValueDD, productAttributeValue)) {
            return false;
        }

        return true;
    }

    private boolean checkIfValueExists(DataDefinition productAttributeValueDD, Entity productAttributeValue) {
        Entity attribute = productAttributeValue.getBelongsToField(ProductAttributeValueFields.ATTRIBUTE);
        Entity product = productAttributeValue.getBelongsToField(ProductAttributeValueFields.PRODUCT);
        Entity attributeValue = productAttributeValue.getBelongsToField(ProductAttributeValueFields.ATTRIBUTE_VALUE);

        List<Entity> values = product.getHasManyField(ProductFields.PRODUCT_ATTRIBUTE_VALUES);

        List sameValue = Lists.newArrayList();
        if(Objects.nonNull(attributeValue)) {
            sameValue = values.stream()
                    .filter(val -> val.getBelongsToField(ProductAttributeValueFields.ATTRIBUTE).getId().equals(attribute.getId())
                            && Objects.nonNull(val.getBelongsToField(ProductAttributeValueFields.ATTRIBUTE_VALUE))
                            && val.getBelongsToField(ProductAttributeValueFields.ATTRIBUTE_VALUE).getId().equals(attribute.getId()))
                    .filter(val -> !val.getId().equals(productAttributeValue.getId())).collect(Collectors.toList());
            if (!sameValue.isEmpty()) {
                productAttributeValue.addError(productAttributeValueDD.getField(ProductAttributeValueFields.ATTRIBUTE_VALUE),
                        "basic.attributeValue.error.valueExists");
                return true;
            }
        } else {
            sameValue = values.stream()
                    .filter(val -> val.getBelongsToField(ProductAttributeValueFields.ATTRIBUTE).getId().equals(attribute.getId())
                            && Objects.isNull(val.getBelongsToField(ProductAttributeValueFields.ATTRIBUTE_VALUE))
                            && val.getStringField(ProductAttributeValueFields.VALUE).equals(productAttributeValue.getStringField(ProductAttributeValueFields.VALUE)))
                    .filter(val -> !val.getId().equals(productAttributeValue.getId())).collect(Collectors.toList());
            if (!sameValue.isEmpty()) {
                productAttributeValue.addError(productAttributeValueDD.getField(ProductAttributeValueFields.VALUE),
                        "basic.attributeValue.error.valueExists");
                return true;
            }
        }



        return false;
    }

    public void onSave(final DataDefinition productAttributeValueDD, final Entity productAttributeValue) {

    }
}
