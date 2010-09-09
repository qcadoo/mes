package com.qcadoo.mes.core.data.internal.types;

import org.apache.commons.lang.StringUtils;
import org.springframework.security.authentication.encoding.PasswordEncoder;

import com.qcadoo.mes.core.data.definition.DataFieldDefinition;
import com.qcadoo.mes.core.data.types.FieldType;
import com.qcadoo.mes.core.data.validation.ValidationResults;

public final class PasswordType implements FieldType {

    private final int lenght;

    private final PasswordEncoder passwordEncoder;

    public PasswordType(final int lenght, final PasswordEncoder passwordEncoder) {
        this.lenght = lenght;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public boolean isSearchable() {
        return false;
    }

    @Override
    public boolean isOrderable() {
        return false;
    }

    @Override
    public boolean isAggregable() {
        return false;
    }

    @Override
    public Class<?> getType() {
        return String.class;
    }

    @Override
    public Object fromString(final DataFieldDefinition fieldDefinition, final String value,
            final ValidationResults validationResults) {
        return passwordEncoder.encodePassword(value, null);
    }

    @Override
    public String toString(final Object value) {
        return null;
    }

    @Override
    public boolean validate(final DataFieldDefinition fieldDefinition, final Object value,
            final ValidationResults validationResults) {
        if (StringUtils.length((String) value) > lenght) {
            validationResults.addError(fieldDefinition, "form.validate.errors.stringIsTooLong", String.valueOf(lenght));
            return false;
        }
        return true;
    }

}
