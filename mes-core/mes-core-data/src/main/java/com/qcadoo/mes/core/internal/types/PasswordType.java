package com.qcadoo.mes.core.internal.types;

import org.springframework.security.authentication.encoding.PasswordEncoder;

import com.qcadoo.mes.core.api.Entity;
import com.qcadoo.mes.core.model.FieldDefinition;
import com.qcadoo.mes.core.types.FieldType;

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
    public Object toObject(final FieldDefinition fieldDefinition, final Object value, final Entity validatedEntity) {
        return passwordEncoder.encodePassword(String.valueOf(value), null);
    }

    @Override
    public String toString(final Object value) {
        return null;
    }

}
