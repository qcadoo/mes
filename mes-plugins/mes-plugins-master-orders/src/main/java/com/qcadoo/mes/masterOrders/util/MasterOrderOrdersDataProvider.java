/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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

import static com.qcadoo.model.api.search.SearchProjections.alias;
import static com.qcadoo.model.api.search.SearchProjections.field;
import static com.qcadoo.model.api.search.SearchProjections.id;
import static com.qcadoo.model.api.search.SearchProjections.list;
import static com.qcadoo.model.api.search.SearchProjections.rowCount;
import static com.qcadoo.model.api.search.SearchProjections.sum;
import static com.qcadoo.model.api.search.SearchRestrictions.belongsTo;
import static com.qcadoo.model.api.search.SearchRestrictions.eq;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.masterOrders.constants.OrderFieldsMO;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchOrder;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchProjection;
import com.qcadoo.model.api.utils.EntityUtils;

@Service
public class MasterOrderOrdersDataProvider {

    public static final SearchProjection ORDER_NUMBER_PROJECTION = list().add(
            alias(field(OrderFields.NUMBER), OrderFields.NUMBER)).add(alias(id(), "id"));

    private static final String QUANTITIES_SUM_ALIAS = "quantity";

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

    public BigDecimal sumBelongingOrdersPlannedQuantities(final Entity masterOrder, final Entity product) {
        SearchProjection quantitiesSumProjection = list().add(alias(sum(OrderFields.PLANNED_QUANTITY), QUANTITIES_SUM_ALIAS))
                .add(rowCount());
        SearchCriterion productCriterion = belongsTo(OrderFields.PRODUCT, product);
        List<Entity> quantitiesSumProjectionResults = findBelongingOrders(masterOrder, quantitiesSumProjection, productCriterion,
                SearchOrders.desc(QUANTITIES_SUM_ALIAS));
        for (Entity entity : quantitiesSumProjectionResults) {
            return entity.getDecimalField(QUANTITIES_SUM_ALIAS);
        }
        return BigDecimal.ZERO;
    }

    public Collection<String> findBelongingOrderNumbers(final Entity masterOrder, final SearchCriterion searchCriteria) {
        List<Entity> ordersProjection = findBelongingOrders(masterOrder, ORDER_NUMBER_PROJECTION, searchCriteria, null);
        return EntityUtils.getFieldsView(ordersProjection, OrderFields.NUMBER);
    }

    public List<Entity> findBelongingOrders(final Entity masterOrder, final SearchProjection projection,
            final SearchCriterion additionalCriteria, final SearchOrder searchOrder) {
        SearchCriteriaBuilder scb = getOrderDD().find();
        // TODO we have to fix problem with converting Form's entity into hibernate's generic entity.
        // scb.add(belongsTo(OrderFieldsMO.MASTER_ORDER, masterOrder));
        // we can't use belongsTo(fieldName, dataDefinition, id)) because it'll cause
        // StackOverflows (this method is used MasterOrder's on View hook
        // Below is my [maku] workaround:
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
