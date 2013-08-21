package com.qcadoo.mes.technologies.tree.builder.api;

import java.math.BigDecimal;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * This is simple container for some value of arbitrary type with (BigDecimal) quantity.
 * 
 * If item's type is immutable one then whole container will be also immutable.
 * 
 * @param <T>
 *            type of the containing item
 * 
 * @author Marcin Kubala
 * @since 1.2.1
 */
public class ItemWithQuantity<T> {

    public final BigDecimal quantity;

    public final T item;

    public ItemWithQuantity(final T item, final BigDecimal quantity) {
        this.item = item;
        this.quantity = quantity;
    }

    /**
     * @return quantity
     */
    public BigDecimal getQuantity() {
        return quantity;
    }

    /**
     * @return item
     */
    public T getItem() {
        return item;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(item).append(quantity).toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ItemWithQuantity)) {
            return false;
        }
        ItemWithQuantity<?> other = (ItemWithQuantity<?>) obj;
        return new EqualsBuilder().append(item, other.item).append(quantity, other.quantity).isEquals();
    }

}
