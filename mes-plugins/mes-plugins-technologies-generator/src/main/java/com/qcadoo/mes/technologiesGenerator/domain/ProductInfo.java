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
import com.qcadoo.mes.technologies.domain.OperationProductInComponentId;
import com.qcadoo.mes.technologies.domain.SizeGroupId;
import com.qcadoo.mes.technologies.domain.TechnologyId;
import com.qcadoo.mes.technologies.domain.TechnologyInputProductTypeId;
import com.qcadoo.mes.technologies.tree.domain.TechnologyOperationId;

public class ProductInfo {

    private final Optional<OperationProductInComponentId> opicId;

    private final TechnologyOperationId tocId;

    private final Optional<TechnologyOperationId> parentId;

    private final ProductId product;

    private final Optional<TechnologyId> productTechnology;

    private final Optional<TechnologyId> originalTechnology;

    private final Optional<TechnologyInputProductTypeId> technologyInputProductType;

    private final boolean differentProductsInDifferentSizes;

    private final OperationId operation;

    private final BigDecimal quantity;

    private final String unit;

    private final Optional<SizeGroupId> sizeGroup;

    private final boolean isIntermediate;

    public ProductInfo(final Optional<OperationProductInComponentId> opicId, TechnologyOperationId tocId,
            final Optional<TechnologyOperationId> parentId, final ProductId product, final BigDecimal quantity,
            final Optional<TechnologyId> productTechnology, final Optional<TechnologyId> originalTechnology,
            final Optional<TechnologyInputProductTypeId> technologyInputProductType,
            final boolean differentProductsInDifferentSizes, final OperationId operation, final boolean isIntermediate,
            final String unit, final Optional<SizeGroupId> sizeGroup) {
        this.opicId = opicId;
        this.tocId = tocId;
        this.parentId = parentId;
        this.product = product;
        this.quantity = quantity;
        this.productTechnology = productTechnology;
        this.originalTechnology = originalTechnology;
        this.technologyInputProductType = technologyInputProductType;
        this.differentProductsInDifferentSizes = differentProductsInDifferentSizes;
        this.operation = operation;
        this.isIntermediate = isIntermediate;
        this.unit = unit;
        this.sizeGroup = sizeGroup;
    }

    public ProductInfo withProductTechnology(final Optional<TechnologyId> newProductTechnology) {
        return new ProductInfo(opicId, tocId, parentId, product, quantity, newProductTechnology, originalTechnology,
                technologyInputProductType, differentProductsInDifferentSizes, operation, isIntermediate, unit, sizeGroup);
    }

    public ProductInfo withOriginalProductTechnology(final Optional<TechnologyId> newOriginalTechnology) {
        return new ProductInfo(opicId, tocId, parentId, product, quantity, productTechnology, newOriginalTechnology,
                technologyInputProductType, differentProductsInDifferentSizes, operation, isIntermediate, unit, sizeGroup);
    }

    public Optional<OperationProductInComponentId> getOpicId() {
        return opicId;
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

    public Optional<TechnologyInputProductTypeId> getTechnologyInputProductType() {
        return technologyInputProductType;
    }

    public boolean getDifferentProductsInDifferentSizes() {
        return differentProductsInDifferentSizes;
    }

    public OperationId getOperation() {
        return operation;
    }

    public String getUnit() {
        return unit;
    }

    public Optional<SizeGroupId> getSizeGroup() {
        return sizeGroup;
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

        return new EqualsBuilder().append(this.opicId, rhs.opicId).append(this.tocId, rhs.tocId)
                .append(this.parentId, rhs.parentId).append(this.product, rhs.product)
                .append(this.productTechnology, rhs.productTechnology).append(this.originalTechnology, rhs.originalTechnology)
                .append(this.technologyInputProductType, rhs.technologyInputProductType)
                .append(this.differentProductsInDifferentSizes, rhs.differentProductsInDifferentSizes)
                .append(this.operation, rhs.operation).append(this.quantity, rhs.quantity)
                .append(this.isIntermediate, rhs.isIntermediate).append(this.unit, rhs.unit).append(this.sizeGroup, rhs.sizeGroup)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(opicId).append(tocId).append(parentId).append(product).append(productTechnology)
                .append(originalTechnology).append(technologyInputProductType).append(differentProductsInDifferentSizes)
                .append(operation).append(quantity).append(unit).append(sizeGroup).append(isIntermediate).toHashCode();
    }

}
