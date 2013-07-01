package com.qcadoo.mes.technologies.tree.builder.api;

import java.math.BigDecimal;

import com.qcadoo.model.api.Entity;

/**
 * This is OperationProductComponent extension for internal use.
 * 
 * @author Marcin Kubala
 * @since 1.2.1
 */
public interface InternalOperationProductComponent extends OperationProductComponent {

    /**
     * Sets a quantity of product
     * 
     * @param quantity
     */
    void setQuantity(final BigDecimal quantity);

    /**
     * Sets an product entity
     * 
     * @param product
     *            product entity
     * @throws IllegalArgumentException
     *             when given entity has type other than #basic_product.
     */
    void setProduct(final Entity product) throws IllegalArgumentException;

}
