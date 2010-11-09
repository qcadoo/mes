package com.qcadoo.mes.model.search;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Object holds order for search criteria.
 */
public final class Order {

    private static final Order DEFAULT_ORDER = Order.asc("id");

    private final String fieldName;

    private final boolean asc;

    private Order(final String fieldName, final boolean asc) {
        this.fieldName = fieldName;
        this.asc = asc;
    }

    /**
     * Create asc order for given field.
     * 
     * @param fieldName
     *            field's name
     * @return order
     */
    public static Order asc(final String fieldName) {
        return new Order(fieldName, true);
    }

    /**
     * Create desc order for given field.
     * 
     * @param fieldName
     *            field's name
     * @return order
     */
    public static Order desc(final String fieldName) {
        return new Order(fieldName, false);
    }

    /**
     * Create asc order using id field.
     * 
     * @return order
     */
    public static Order asc() {
        return DEFAULT_ORDER;
    }

    /**
     * Return true if order is asc.
     * 
     * @return is asc
     */
    public boolean isAsc() {
        return asc;
    }

    /**
     * Return true if order is desc.
     * 
     * @return is desc
     */
    public boolean isDesc() {
        return !asc;
    }

    /**
     * Return field's name use for ordering.
     * 
     * @return field's name
     */
    public String getFieldName() {
        return fieldName;
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public String toString() {
        return fieldName + (asc ? " asc" : " desc");
    }

}
