/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
 * Version: 1.4
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.technologiesGenerator.domain;

import java.math.BigDecimal;
import java.util.Optional;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.qcadoo.mes.basic.product.domain.ProductId;
import com.qcadoo.mes.technologies.domain.OperationId;
import com.qcadoo.mes.technologies.domain.TechnologyId;
import com.qcadoo.mes.technologies.tree.domain.TechnologyOperationId;

public class ProductInfo {

    private final TechnologyOperationId tocId;

    private final Optional<TechnologyOperationId> parentId;

    private final ProductId product;

    private final Optional<TechnologyId> productTechnology;

    private final Optional<TechnologyId> originalTechnology;

    private final OperationId operation;

    private final BigDecimal quantity;

    private final boolean isIntermediate;

    // TODO extract some builder
    public ProductInfo(final TechnologyOperationId tocId, final Optional<TechnologyOperationId> parentId,
            final ProductId product, final BigDecimal quantity, final Optional<TechnologyId> productTechnology,
            final Optional<TechnologyId> originalTechnology, final OperationId operation, final boolean isIntermediate) {
        this.tocId = tocId;
        this.parentId = parentId;
        this.product = product;
        this.quantity = quantity;
        this.productTechnology = productTechnology;
        this.originalTechnology = originalTechnology;
        this.operation = operation;
        this.isIntermediate = isIntermediate;
    }

    public ProductInfo withProductTechnology(final Optional<TechnologyId> newProductTechnology) {
        return new ProductInfo(tocId, parentId, product, quantity, newProductTechnology, originalTechnology, operation,
                isIntermediate);
    }

    public ProductInfo withOriginalProductTechnology(final Optional<TechnologyId> newOriginalTechnology) {
        return new ProductInfo(tocId, parentId, product, quantity, productTechnology, newOriginalTechnology, operation,
                isIntermediate);
    }

    public TechnologyOperationId getTocId() {
        return tocId;
    }

    public ProductId getProduct() {
        return product;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public Optional<TechnologyId> getProductTechnology() {
        return productTechnology;
    }

    public Optional<TechnologyId> getOriginalTechnology() {
        return originalTechnology;
    }

    public OperationId getOperation() {
        return operation;
    }

    public boolean isIntermediate() {
        return isIntermediate;
    }

    public boolean hasParent() {
        return parentId.isPresent();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        ProductInfo rhs = (ProductInfo) obj;
        return new EqualsBuilder().append(this.tocId, rhs.tocId).append(this.parentId, rhs.parentId)
                .append(this.product, rhs.product).append(this.productTechnology, rhs.productTechnology)
                .append(this.originalTechnology, rhs.originalTechnology).append(this.operation, rhs.operation)
                .append(this.quantity, rhs.quantity).append(this.isIntermediate, rhs.isIntermediate).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(tocId).append(parentId).append(product).append(productTechnology)
                .append(originalTechnology).append(operation).append(quantity).append(isIntermediate).toHashCode();
    }
}
