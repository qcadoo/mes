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
package com.qcadoo.mes.technologiesGenerator.customization.product.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.qcadoo.model.api.Entity;

public class ProductSuffixes {

    private final ProductNameSuffix nameSuffix;

    private final ProductNumberSuffix numberSuffix;

    public static ProductSuffixes from(final Entity product) {
        return new ProductSuffixes(ProductNumberSuffix.from(product), ProductNameSuffix.from(product));
    }

    public ProductSuffixes(final ProductNumberSuffix productNumberSuffix, final ProductNameSuffix productNameSuffix) {
        this.nameSuffix = productNameSuffix;
        this.numberSuffix = productNumberSuffix;
    }

    public ProductNameSuffix getNameSuffix() {
        return nameSuffix;
    }

    public ProductNumberSuffix getNumberSuffix() {
        return numberSuffix;
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
        ProductSuffixes rhs = (ProductSuffixes) obj;
        return new EqualsBuilder().append(this.nameSuffix, rhs.nameSuffix).append(this.numberSuffix, rhs.numberSuffix).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(nameSuffix).append(numberSuffix).toHashCode();
    }

    @Override
    public String toString() {
        return String.format("ProductSuffixes(numberSuffix=%s, nameSuffix=%s)", numberSuffix, nameSuffix);
    }
}
