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
package com.qcadoo.mes.operationalTasksForOrders.hooks;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.operationalTasks.constants.OperationalTaskFields;
import com.qcadoo.mes.operationalTasks.constants.OperationalTasksConstants;
import com.qcadoo.mes.operationalTasksForOrders.constants.OperationalTaskFieldsOTFO;
import com.qcadoo.mes.operationalTasksForOrders.constants.OperationalTasksForOrdersConstants;
import com.qcadoo.mes.operationalTasksForOrders.constants.TechOperCompOperationalTasksFields;
import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.mes.technologies.constants.OperationProductOutComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class OperationalTasksListHooksOTFO {

    private static final String L_GRID = "grid";

    private static final String L_OPERATION_COMPONENT = "operationComponent";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void addDiscriminatorRestrictionToGrid(final ViewDefinitionState view) {
        LookupComponent productInLookup = (LookupComponent) view.getComponentByReference(OperationalTaskFields.PRODUCT_IN);
        LookupComponent productOutLookup = (LookupComponent) view.getComponentByReference(OperationalTaskFields.PRODUCT_OUT);

        Entity productIn = productInLookup.getEntity();
        Entity productOut = productOutLookup.getEntity();

        if ((productIn == null) && (productOut == null)) {
            return;
        }

        List<Entity> operations = Lists.newArrayList();

        if (productIn != null) {
            operations = getTechnologyOperationComponents(getOperatonProductInComponents(productIn));
        }
        if (productOut != null) {
            operations.addAll(getTechnologyOperationComponents(getOperationProductOutComponents(productOut)));
        }

        GridComponent grid = (GridComponent) view.getComponentByReference(L_GRID);

        List<Entity> tasks = getOperationalTasks(operations);

        grid.setEntities(tasks);
        grid.performEvent(view, "refresh");
    }

    private List<Entity> getOperationalTasks(final List<Entity> technologyOperationComponents) {
        List<Entity> operationalTasks = Lists.newArrayList();

        for (Entity technologyOperationComponent : technologyOperationComponents) {
            Entity techOperCompOperationalTask = dataDefinitionService
                    .get(OperationalTasksForOrdersConstants.PLUGIN_IDENTIFIER,
                            OperationalTasksForOrdersConstants.MODEL_TECH_OPER_COMP_OPERATIONAL_TASK)
                    .find()
                    .add(SearchRestrictions.belongsTo(TechOperCompOperationalTasksFields.TECHNOLOGY_OPERATION_COMPONENT,
                            technologyOperationComponent)).setMaxResults(1).uniqueResult();

            List<Entity> tasksForTOCOT = dataDefinitionService
                    .get(OperationalTasksConstants.PLUGIN_IDENTIFIER, OperationalTasksConstants.MODEL_OPERATIONAL_TASK)
                    .find()
                    .add(SearchRestrictions.belongsTo(OperationalTaskFieldsOTFO.TECH_OPER_COMP_OPERATIONAL_TASK,
                            techOperCompOperationalTask)).list().getEntities();

            for (Entity taskForTOCOT : tasksForTOCOT) {
                if (!operationalTasks.contains(taskForTOCOT)) {
                    operationalTasks.add(taskForTOCOT);
                }
            }
        }

        return operationalTasks;
    }

    private List<Entity> getTechnologyOperationComponents(final List<Entity> operationProductComponents) {
        List<Entity> technologyOperationComponents = Lists.newArrayList();

        for (Entity operationProductComponent : operationProductComponents) {
            Entity operationComponent = operationProductComponent.getBelongsToField(L_OPERATION_COMPONENT);

            if (!technologyOperationComponents.contains(operationComponent)) {
                technologyOperationComponents.add(operationComponent);
            }
        }

        return technologyOperationComponents;
    }

    private List<Entity> getOperatonProductInComponents(final Entity productIn) {
        return dataDefinitionService
                .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT).find()
                .add(SearchRestrictions.belongsTo(OperationProductInComponentFields.PRODUCT, productIn)).list().getEntities();
    }

    private List<Entity> getOperationProductOutComponents(final Entity productOut) {
        return dataDefinitionService
                .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_OPERATION_PRODUCT_OUT_COMPONENT).find()
                .add(SearchRestrictions.belongsTo(OperationProductOutComponentFields.PRODUCT, productOut)).list().getEntities();
    }

}
