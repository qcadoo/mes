package com.qcadoo.mes.core.data.search;

/**
 * Restriction represents the part of WHERE clause in SQL query.
 */
public interface Restriction {

    String getFieldName();

    Object getValue();
}
