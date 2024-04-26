/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
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
package com.qcadoo.mes.masterOrders.util;

import com.qcadoo.mes.masterOrders.constants.OrderFieldsMO;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.*;
import com.qcadoo.model.api.utils.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

import static com.qcadoo.model.api.search.SearchProjections.*;
import static com.qcadoo.model.api.search.SearchRestrictions.eq;

@Service
public class MasterOrderOrdersDataProvider {

    public static final SearchProjection ORDER_NUMBER_PROJECTION = list().add(
            alias(field(OrderFields.NUMBER), OrderFields.NUMBER)).add(alias(id(), "id"));

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public long countBelongingOrders(final Entity masterOrder, final SearchCriterion additionalCriteria) {
        List<Entity> ordersCountProjection = findBelongingOrders(masterOrder, alias(rowCount(), "count"), additionalCriteria,
                SearchOrders.desc("count"));
        for (Entity entity : ordersCountProjection) {
            return (Long) entity.getField("count");
        }
        return 0L;
    }

    public Collection<String> findBelongingOrderNumbers(final Entity masterOrder,
                                                        final SearchCriterion searchCriteria) {
        List<Entity> ordersProjection = findBelongingOrders(masterOrder, ORDER_NUMBER_PROJECTION, searchCriteria, null);
        return EntityUtils.getFieldsView(ordersProjection, OrderFields.NUMBER);
    }

    public List<Entity> findBelongingOrders(final Entity masterOrder, final SearchProjection projection,
                                            final SearchCriterion additionalCriteria, final SearchOrder searchOrder) {
        SearchCriteriaBuilder scb = getOrderDD().find();
        scb.createAlias(OrderFieldsMO.MASTER_ORDER, "mo_alias", JoinType.INNER);
        scb.add(eq("mo_alias.id", masterOrder.getId()));
        if (additionalCriteria != null) {
            scb.add(additionalCriteria);
        }
        if (projection != null) {
            scb.setProjection(projection);
        }
        if (searchOrder != null) {
            scb.addOrder(searchOrder);
        }
        return scb.list().getEntities();
    }

    private DataDefinition getOrderDD() {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER);
    }
}
