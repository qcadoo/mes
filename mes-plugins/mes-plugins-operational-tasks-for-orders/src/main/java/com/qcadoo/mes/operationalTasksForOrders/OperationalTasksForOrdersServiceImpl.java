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
package com.qcadoo.mes.operationalTasksForOrders;

import static com.qcadoo.mes.operationalTasksForOrders.constants.OperationalTaskFieldsOTFO.ORDER;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.operationalTasks.constants.OperationalTaskTypeTask;
import com.qcadoo.mes.operationalTasks.constants.OperationalTasksConstants;
import com.qcadoo.mes.operationalTasksForOrders.constants.OperationalTaskFieldsOTFO;
import com.qcadoo.mes.operationalTasksForOrders.constants.OperationalTaskTypeTaskOTFO;
import com.qcadoo.mes.operationalTasksForOrders.constants.OperationalTasksForOrdersConstants;
import com.qcadoo.mes.operationalTasksForOrders.constants.TechOperCompOperationalTasksFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class OperationalTasksForOrdersServiceImpl implements OperationalTasksForOrdersService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    public List<Entity> getTechnologyOperationComponentsForOperation(final Entity operation) {
        return dataDefinitionService
                .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT).find()
                .add(SearchRestrictions.belongsTo(TechnologyOperationComponentFields.OPERATION, operation)).list().getEntities();
    }

    @Override
    public List<Entity> getTechOperCompOperationalTasksForTechnologyOperationComponent(final Entity technologyOperationComponent) {
        return dataDefinitionService
                .get(OperationalTasksForOrdersConstants.PLUGIN_IDENTIFIER,
                        OperationalTasksForOrdersConstants.MODEL_TECH_OPER_COMP_OPERATIONAL_TASK)
                .find()
                .add(SearchRestrictions.belongsTo(TechOperCompOperationalTasksFields.TECHNOLOGY_OPERATION_COMPONENT,
                        technologyOperationComponent)).list().getEntities();
    }

    @Override
    public List<Entity> getOperationalTasksForTechOperCompOperationalTasks(final Entity techOperCompOperationalTask) {
        return dataDefinitionService
                .get(OperationalTasksConstants.PLUGIN_IDENTIFIER, OperationalTasksConstants.MODEL_OPERATIONAL_TASK)
                .find()
                .add(SearchRestrictions.belongsTo(OperationalTaskFieldsOTFO.TECH_OPER_COMP_OPERATIONAL_TASK,
                        techOperCompOperationalTask)).list().getEntities();
    }

    @Override
    public List<Entity> getOperationalTasksForOrder(final Entity order) {
        return dataDefinitionService
                .get(OperationalTasksConstants.PLUGIN_IDENTIFIER, OperationalTasksConstants.MODEL_OPERATIONAL_TASK).find()
                .add(SearchRestrictions.belongsTo(ORDER, order)).list().getEntities();
    }

    @Override
    public boolean isOperationalTaskTypeTaskOtherCase(final String typeTask) {
        return OperationalTaskTypeTask.OTHER_CASE.getStringValue().equals(typeTask);
    }

    @Override
    public boolean isOperationalTaskTypeTaskExecutionOperationInOrder(final String typeTask) {
        return OperationalTaskTypeTaskOTFO.EXECUTION_OPERATION_IN_ORDER.getStringValue().equals(typeTask);
    }

}
