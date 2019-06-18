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
package com.qcadoo.mes.technologiesGenerator.customization.product;

import static com.qcadoo.model.api.search.SearchOrders.desc;
import static com.qcadoo.model.api.search.SearchRestrictions.belongsTo;
import static com.qcadoo.model.api.search.SearchRestrictions.eq;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;

@Service
class CustomizedProductDataProvider {

    public Optional<Entity> tryFind(final Entity product, final String numberWithSuffix) {
        SearchCriteriaBuilder scb = product.getDataDefinition().find();
        scb.add(belongsTo(ProductFields.PARENT, product));
        scb.add(eq(ProductFields.NUMBER, numberWithSuffix));
        scb.addOrder(desc("id"));
        return Optional.ofNullable(scb.setMaxResults(1).uniqueResult());
    }

}
