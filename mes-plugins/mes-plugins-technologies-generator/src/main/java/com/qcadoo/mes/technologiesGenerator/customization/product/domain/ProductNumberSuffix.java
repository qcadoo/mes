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

import java.util.Objects;

import com.google.common.base.Preconditions;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.model.api.Entity;

public class ProductNumberSuffix {

    private final String suffix;

    public static ProductNumberSuffix from(final Entity product) {
        Preconditions.checkArgument(product != null, "you have to pass product from which you want to extract number suffix");
        return new ProductNumberSuffix(product.getStringField(ProductFields.NUMBER));
    }

    public ProductNumberSuffix(final String suffix) {
        this.suffix = suffix;
    }

    public String get() {
        return suffix;
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
        ProductNumberSuffix rhs = (ProductNumberSuffix) obj;
        return Objects.equals(suffix, rhs.suffix);
    }

    @Override
    public int hashCode() {
        return suffix.hashCode();
    }

    @Override
    public String toString() {
        return String.format("ProductNumberSuffix('%s')", suffix);
    }
}
