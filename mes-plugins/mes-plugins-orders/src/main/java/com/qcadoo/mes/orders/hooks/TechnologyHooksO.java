/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 * <p>
 * This file is part of Qcadoo.
 * <p>
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.orders.hooks;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.states.constants.OrderStateStringValues;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.constants.TechnologyFieldsO;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class TechnologyHooksO {

    public static final String REGENERATE_PQC = "regeneratePQC";
    public static final String IS_UPDATE_TECHNOLOGIES_ON_PENDING_ORDERS = "isUpdateTechnologiesOnPendingOrders";
    @Autowired
    private ParameterService parameterService;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void onSave(final DataDefinition technologyDD, final Entity technology) {

        if (Objects.nonNull(technology.getId()) && parameterService.getParameter().getBooleanField("updateTechnologiesOnPendingOrders")) {
            Entity technologyDb = technology.getDataDefinition().get(technology.getId());
            if (technology.getBooleanField(TechnologyFields.MASTER)
                    && !technologyDb.getBooleanField(TechnologyFields.MASTER)) {

                Entity product = technology.getBelongsToField(TechnologyFields.PRODUCT);

                List<Long> mopIds = getMasterOrderProducts(product);

                for (Long mopId : mopIds) {
                    Entity mop = dataDefinitionService.get("masterOrders", "masterOrderProduct").get(mopId);

                    List<Entity> orders = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER)
                            .find()
                            .createAlias("masterOrder", "masterOrder", JoinType.LEFT)
                            .createAlias("product", "product", JoinType.LEFT)
                            .add(SearchRestrictions.eq("masterOrder.id", mop.getBelongsToField("masterOrder").getId()))
                            .add(SearchRestrictions.eq("product.id", product.getId()))
                            .list().getEntities();

                    boolean hasChildren = hasChildren(orders);

                    if (!hasChildren) {
                        changeTechnologyInMOP(technology, mop, orders);
                    }
                }

                changeTechnologyInOrdersNotAssignedToMO(technology, product);

            }
        }
    }

    private void changeTechnologyInMOP(Entity technology, Entity mop, List<Entity> orders) {
        mop.setField(IS_UPDATE_TECHNOLOGIES_ON_PENDING_ORDERS, true);
        mop.setField("technology", technology);
        Entity savedMop = mop.getDataDefinition().save(mop);
        if (savedMop.isValid()) {
            for (Entity order : orders) {
                order.setField(OrderFields.TECHNOLOGY, technology);
                order.setField(REGENERATE_PQC, true);
                order.setField(IS_UPDATE_TECHNOLOGIES_ON_PENDING_ORDERS, true);
                Entity savedOrder = order.getDataDefinition().save(order);
            }

        }
    }

    private void changeTechnologyInOrdersNotAssignedToMO(Entity technology, Entity product) {
        List<Entity> orders = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER)
                .find()
                .createAlias("product", "product", JoinType.LEFT)
                .add(SearchRestrictions.eq("product.id", product.getId()))
                .add(SearchRestrictions.eq(OrderFields.STATE, OrderStateStringValues.PENDING))
                .add(SearchRestrictions.isNull("masterOrder"))
                .list().getEntities();
        for (Entity order : orders) {
            if (order.getHasManyField("children").isEmpty()) {
                order.setField(OrderFields.TECHNOLOGY, technology);

                order.setField(REGENERATE_PQC, true);
                Entity savedOrder = order.getDataDefinition().save(order);
            }
        }
    }

    private boolean hasChildren(List<Entity> orders) {
        boolean hasChildren = false;

        for (Entity order : orders) {
            if (!order.getHasManyField("children").isEmpty()) {
                hasChildren = true;
            }
        }
        return hasChildren;
    }

    private List<Long> getMasterOrderProducts(Entity product) {
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("productId", product.getId());

        StringBuilder query = new StringBuilder();
        query.append("SELECT mop.id FROM masterorders_masterorderproduct mop ");
        query.append("where mop.product_id = :productId AND ");
        query.append("(SELECT count(*) FROM orders_order ord WHERE ord.product_id = mop.product_id AND ord.masterorder_id = mop.masterorder_id AND (ord.state = '01pending' OR ord.state = '05declined')) = (SELECT count(*) FROM orders_order ord WHERE ord.product_id = mop.product_id AND ord.masterorder_id = mop.masterorder_id) ");
        List<Long> mopIds = jdbcTemplate.queryForList(query.toString(), parameters, Long.class);
        return mopIds;
    }

    public boolean checkIfTechnologyIsPrototypeForOrders(final DataDefinition technologyDD, final Entity technology) {
        List<Entity> orders = technology.getHasManyField(TechnologyFieldsO.ORDERS_USING_PROTOTYPE);

        if (!orders.isEmpty()) {
            technology.addGlobalError("orders.technology.hasOrdersAsPrototype",
                    technology.getStringField(TechnologyFields.NUMBER));
            return false;
        }
        return true;
    }
}
