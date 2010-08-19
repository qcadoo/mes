package com.qcadoo.mes.core.data.internal;

import org.apache.commons.beanutils.PropertyUtils;

import com.qcadoo.mes.core.data.definition.FieldDefinition;

public final class FieldUtils {

    public static final String FIELD_ID = "id";

    public static final String FIELD_DELETED = "deleted";

    private FieldUtils() {
    }

    public static Long getId(final Object entity) {
        return (Long) getField(entity, FIELD_ID);
    }

    public static void setId(final Object entity, final Long id) {
        setField(entity, FIELD_ID, id);
    }

    public static void setDeleted(final Object entity) {
        setField(entity, FIELD_DELETED, true);
    }

    public static void setField(final Object entity, final FieldDefinition fieldDefinition, Object value) {
        if (fieldDefinition.isCustomField()) {
            throw new UnsupportedOperationException("custom fields are not supported");
        } else {
            if (!fieldDefinition.getType().isValidType(value)) {
                throw new IllegalStateException("value of the property " + entity.getClass().getSimpleName()
                        + " has invalid type: " + value.getClass().getSimpleName());
            }
            setField(entity, fieldDefinition.getName(), value);
        }
    }

    public static Object getField(final Object entity, final FieldDefinition fieldDefinition) {
        if (fieldDefinition.isCustomField()) {
            throw new UnsupportedOperationException("custom fields are not supported");
        } else {
            Object value = getField(entity, fieldDefinition.getName());
            if (!fieldDefinition.getType().isValidType(value)) {
                throw new IllegalStateException("value of the property " + entity.getClass().getSimpleName()
                        + " has invalid type: " + value.getClass().getSimpleName());
            }
            return value;
        }
    }

    private static void setField(final Object entity, final String fieldName, Object value) {
        try {
            PropertyUtils.setProperty(entity, fieldName, value);
        } catch (Exception e) {
            throw new IllegalStateException("cannot set value of the property: " + entity.getClass().getSimpleName() + ", "
                    + fieldName, e);
        }
    }

    private static Object getField(final Object entity, final String fieldName) {
        try {
            return PropertyUtils.getProperty(entity, fieldName);
        } catch (Exception e) {
            throw new IllegalStateException("cannot get value of the property: " + entity.getClass().getSimpleName() + ", "
                    + fieldName, e);
        }
    }

}
