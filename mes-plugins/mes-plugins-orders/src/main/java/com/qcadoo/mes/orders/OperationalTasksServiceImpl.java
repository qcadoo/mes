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
package com.qcadoo.mes.orders;

import com.google.common.collect.Lists;
import com.qcadoo.mes.orders.constants.OperationalTaskFields;
import com.qcadoo.mes.orders.constants.OperationalTaskType;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.states.constants.OperationalTaskStateStringValues;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OperationalTasksServiceImpl implements OperationalTasksService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    public List<Entity> getTechnologyOperationComponentsForOperation(final Entity operation) {
        return dataDefinitionService
                .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT).find()
                .add(SearchRestrictions.belongsTo(TechnologyOperationComponentFields.OPERATION, operation)).list().getEntities();
    }

    @Override
    public List<Entity> getOperationalTasksForOrder(final Entity order) {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_OPERATIONAL_TASK).find()
                .add(SearchRestrictions.belongsTo(OperationalTaskFields.ORDER, order)).list().getEntities();
    }

    @Override
    public boolean isOperationalTaskTypeOtherCase(final String type) {
        return OperationalTaskType.OTHER_CASE.getStringValue().equals(type);
    }

    @Override
    public boolean isOperationalTaskTypeExecutionOperationInOrder(final String type) {
        return OperationalTaskType.EXECUTION_OPERATION_IN_ORDER.getStringValue().equals(type);
    }

    @Override
    public Entity findOperationalTasks(Entity order, Entity toc) {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_OPERATIONAL_TASK).find()
                .add(SearchRestrictions.belongsTo(OperationalTaskFields.ORDER, order))
                .add(SearchRestrictions.belongsTo(OperationalTaskFields.TECHNOLOGY_OPERATION_COMPONENT, toc))
                .add(SearchRestrictions.in(OperationalTaskFields.STATE,
                        Lists.newArrayList(OperationalTaskStateStringValues.PENDING, OperationalTaskStateStringValues.STARTED)))
                .setMaxResults(1).uniqueResult();
    }

    @Override
    public List<Entity> getChildren(Entity technologyOperationComponent, Entity order) {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_OPERATIONAL_TASK).find()
                .createAlias(OperationalTaskFields.TECHNOLOGY_OPERATION_COMPONENT, "toc", JoinType.INNER)
                .add(SearchRestrictions.belongsTo("toc." + TechnologyOperationComponentFields.PARENT,
                        technologyOperationComponent))
                .add(SearchRestrictions.ne(OperationalTaskFields.STATE, OperationalTaskStateStringValues.REJECTED))
                .add(SearchRestrictions.belongsTo(OperationalTaskFields.ORDER, order)).list().getEntities();
    }

    @Override
    public Entity getParent(Entity technologyOperationComponent, Entity order) {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_OPERATIONAL_TASK).find()
                .add(SearchRestrictions.belongsTo(OperationalTaskFields.TECHNOLOGY_OPERATION_COMPONENT,
                        technologyOperationComponent.getBelongsToField(TechnologyOperationComponentFields.PARENT)))
                .add(SearchRestrictions.ne(OperationalTaskFields.STATE, OperationalTaskStateStringValues.REJECTED))
                .add(SearchRestrictions.belongsTo(OperationalTaskFields.ORDER, order)).uniqueResult();
    }
}
