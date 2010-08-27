package com.qcadoo.mes.core.data.internal;

public interface ValidatableFieldType {

    Class<?> getType();

    String validateValue(Object value);

}
