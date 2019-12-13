package com.qcadoo.mes.basic.hooks;

import com.google.common.base.Optional;
import com.qcadoo.commons.functional.Either;
import com.qcadoo.mes.basic.constants.AttributeFields;
import com.qcadoo.mes.basic.constants.AttributeValueFields;
import com.qcadoo.mes.basic.constants.AttributeValueType;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;

import java.math.BigDecimal;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AttributeValueHooks {

    @Autowired
    private NumberService numberService;

    public boolean validate(final DataDefinition attributeValueDD, final Entity attributeValue) {
        Entity attribute = attributeValue.getBelongsToField(AttributeValueFields.ATTRIBUTE);
        String value = attributeValue.getStringField(AttributeValueFields.VALUE);

        if (checkIfValueExists(attributeValueDD, attributeValue, attribute, value)) {
            return false;
        }

        if (AttributeValueType.NUMERIC.getStringValue().equals(attribute.getStringField(AttributeFields.VALUE_TYPE))) {
            int scale = attribute.getIntegerField(AttributeFields.PRECISION);

            Either<Exception, Optional<BigDecimal>> eitherNumber = BigDecimalUtils.tryParseAndIgnoreSeparator(
                    attributeValue.getStringField(AttributeValueFields.VALUE), LocaleContextHolder.getLocale());
            if (eitherNumber.isRight() && eitherNumber.getRight().isPresent()) {

                int valueScale = eitherNumber.getRight().get().scale();
                if (valueScale > scale) {
                    attributeValue.addError(attributeValueDD.getField(AttributeValueFields.VALUE),
                            "qcadooView.validate.field.error.invalidScale.max", String.valueOf(scale));
                    return false;
                }

            } else {
                attributeValue.addError(attributeValueDD.getField(AttributeValueFields.VALUE),
                        "qcadooView.validate.field.error.invalidNumericFormat");
                return false;
            }
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(LocaleContextHolder.getLocale());

            return !checkIfValueExists(attributeValueDD, attributeValue, attribute,
                    BigDecimalUtils.toString(eitherNumber.getRight().get(), scale));
        }
        return true;
    }

    private boolean checkIfValueExists(DataDefinition attributeValueDD, Entity attributeValue, Entity attribute, String value) {
        List<Entity> values = attribute.getHasManyField(AttributeFields.ATTRIBUTE_VALUES);
        List sameValue = values.stream().filter(val -> val.getStringField(AttributeValueFields.VALUE).equals(value))
                .filter(val -> !val.getId().equals(attributeValue.getId())).collect(Collectors.toList());
        if (!sameValue.isEmpty()) {
            attributeValue.addError(attributeValueDD.getField(AttributeValueFields.VALUE),
                    "basic.attributeValue.error.valueExists");
            return true;
        }
        return false;
    }

    public void onSave(final DataDefinition attributeValueDD, final Entity attributeValue) {
        Entity attribute = attributeValue.getBelongsToField(AttributeValueFields.ATTRIBUTE);
        if (AttributeValueType.NUMERIC.getStringValue().equals(attribute.getStringField(AttributeFields.VALUE_TYPE))) {
            Either<Exception, Optional<BigDecimal>> eitherNumber = BigDecimalUtils.tryParseAndIgnoreSeparator(
                    attributeValue.getStringField(AttributeValueFields.VALUE), LocaleContextHolder.getLocale());
            if (eitherNumber.isRight() && eitherNumber.getRight().isPresent()) {
                attributeValue.setField(
                        AttributeValueFields.VALUE,
                        BigDecimalUtils.toString(eitherNumber.getRight().get(),
                                attribute.getIntegerField(AttributeFields.PRECISION)));
            }
        }
    }
}
