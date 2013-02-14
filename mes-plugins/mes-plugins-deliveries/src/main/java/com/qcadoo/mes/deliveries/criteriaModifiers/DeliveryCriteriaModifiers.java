/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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
package com.qcadoo.mes.deliveries.criteriaModifiers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.deliveries.DeliveriesService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchQueryBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class DeliveryCriteriaModifiers {

    @Autowired
    private DeliveriesService deliveriesService;

    public void showActiveSupplyItems(final SearchCriteriaBuilder scb) {
        final SearchQueryBuilder sqb = deliveriesService
                .getOrderedProductDD()
                .find("select orderedProduct from #deliveries_orderedProduct as orderedProduct INNER JOIN orderedProduct.delivery as delivery WHERE delivery.active = 'true'");

        List<Entity> orderedProductsList = sqb.list().getEntities();
        List<Long> orderedProductsListLong = new ArrayList<Long>();

        for (Entity entity : orderedProductsList) {
            orderedProductsListLong.add(entity.getId());
        }

        if (orderedProductsListLong.isEmpty()) {
            final BigDecimal bg = new BigDecimal(-1);
            scb.add(SearchRestrictions.eq("orderedQuantity", bg));
        } else {
            scb.add(SearchRestrictions.in("id", orderedProductsListLong));
        }
    }

}
