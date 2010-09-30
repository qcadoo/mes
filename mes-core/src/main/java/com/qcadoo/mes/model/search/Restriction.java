package com.qcadoo.mes.model.search;

/**
 * Restriction represents the part of WHERE clause in SQL query.
 */
public interface Restriction {

    String getFieldName();

    Object getValue();

}
