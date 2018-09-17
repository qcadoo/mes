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
package com.qcadoo.mes.ordersForSubproductsGeneration.criteriaModifiers;

import com.google.common.collect.Lists;
import org.springframework.stereotype.Service;

import com.google.common.base.Splitter;
import com.google.common.primitives.Longs;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.ordersForSubproductsGeneration.constants.OrderFieldsOFSPG;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class CoverageForOrderCriteriaModifiersOFSPG {

    public static final String ORDER_PARAMETER = "orderID";

    public static final String ORDERS_PARAMETER = "ordersID";

    public void showGeneratedOrders(final SearchCriteriaBuilder scb, final FilterValueHolder filterValue) {
        Long orderId = null;
        if (!filterValue.has(ORDER_PARAMETER) && !filterValue.has(ORDERS_PARAMETER)) {
            orderId = 0L;
            scb.add(SearchRestrictions.belongsTo(OrderFieldsOFSPG.ROOT, OrdersConstants.PLUGIN_IDENTIFIER,
                    OrdersConstants.MODEL_ORDER, orderId)).add(SearchRestrictions.isNotNull(OrderFieldsOFSPG.PARENT));
            return;
        } else if (filterValue.has(ORDER_PARAMETER)) {
            orderId = filterValue.getLong(ORDER_PARAMETER);
            scb.add(SearchRestrictions.belongsTo(OrderFieldsOFSPG.ROOT, OrdersConstants.PLUGIN_IDENTIFIER,
                    OrdersConstants.MODEL_ORDER, orderId)).add(SearchRestrictions.isNotNull(OrderFieldsOFSPG.PARENT));
        } else if (filterValue.has(ORDERS_PARAMETER)) {
            String ids = filterValue.getString(ORDERS_PARAMETER);
            Iterable<Long> longIds = Longs.stringConverter().convertAll(
                    Splitter.on(',').trimResults().omitEmptyStrings().splitToList(ids));
            scb.createAlias(OrderFieldsOFSPG.ROOT, OrderFieldsOFSPG.ROOT, JoinType.LEFT)
                    .add(SearchRestrictions.in(OrderFieldsOFSPG.ROOT + ".id", Lists.newArrayList(longIds)))
                    .add(SearchRestrictions.isNotNull(OrderFieldsOFSPG.PARENT));

        } else {
            return;
        }

    }

}
