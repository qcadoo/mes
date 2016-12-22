/*
 * **************************************************************************
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
 * **************************************************************************
 */
package com.qcadoo.mes.basic.product.importing;

import com.qcadoo.mes.basic.constants.ProductFamilyElementType;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

class ProductRowBuilderFactory {

    private final DataDefinition dataDefinition;
    private final RowBuilder rowBuilder;

    ProductRowBuilderFactory(final DataDefinition dataDefinition) {
        this.dataDefinition = dataDefinition;
        this.rowBuilder = new RowBuilder();
    }

    RowBuilder builder() {
        Entity entity = dataDefinition.create();
        entity.setField("entityType", ProductFamilyElementType.PARTICULAR_PRODUCT.getStringValue());
        rowBuilder.initialize(entity); // reuse rowBuilder instead of creating new one for every row
        return rowBuilder;
    }
}
