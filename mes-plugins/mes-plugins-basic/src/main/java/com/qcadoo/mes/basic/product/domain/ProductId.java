package com.qcadoo.mes.basic.product.domain;

import com.qcadoo.mes.basic.domain.ImmutableIdWrapper;

public class ProductId extends ImmutableIdWrapper {

    public ProductId(final Long id) {
        super(id);
    }

    @Override
    public String toString() {
        return String.format("ProductId(%s)", get());
    }
}
