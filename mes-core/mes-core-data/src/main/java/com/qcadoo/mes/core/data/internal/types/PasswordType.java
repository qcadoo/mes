package com.qcadoo.mes.core.data.internal.types;

import org.springframework.security.authentication.encoding.PasswordEncoder;

import com.qcadoo.mes.core.data.definition.DataFieldDefinition;
import com.qcadoo.mes.core.data.types.FieldType;
import com.qcadoo.mes.core.data.validation.ValidationResults;

public final class PasswordType implements FieldType {

    private final PasswordEncoder passwordEncoder;

    public PasswordType(final PasswordEncoder passwordEncoder) {
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
    public Object toObject(final DataFieldDefinition fieldDefinition, final Object value,
            final ValidationResults validationResults) {
        return passwordEncoder.encodePassword(String.valueOf(value), null);
    }

    @Override
    public String toString(final Object value) {
        return null;
    }

}
