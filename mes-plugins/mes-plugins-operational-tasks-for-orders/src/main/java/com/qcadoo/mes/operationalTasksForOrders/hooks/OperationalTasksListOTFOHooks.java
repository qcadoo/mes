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

import static com.qcadoo.mes.operationalTasks.constants.OperationalTasksFields.PRODUCT_IN;
import static com.qcadoo.mes.operationalTasks.constants.OperationalTasksFields.PRODUCT_OUT;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.operationalTasks.constants.OperationalTasksConstants;
import com.qcadoo.mes.operationalTasksForOrders.constants.OperationalTasksForOrdersConstants;
import com.qcadoo.mes.operationalTasksForOrders.constants.TechOperCompOperationalTasksFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class OperationalTasksListOTFOHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void addDiscriminatorRestrictionToGrid(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        addDiscriminatorRestrictionToGrid(view);
    }

    public final void addDiscriminatorRestrictionToGrid(final ViewDefinitionState view) {
        LookupComponent productIn = (LookupComponent) view.getComponentByReference(PRODUCT_IN);
        LookupComponent productOut = (LookupComponent) view.getComponentByReference(PRODUCT_OUT);
        Entity productInEntity = productIn.getEntity();
        Entity productOutEntity = productOut.getEntity();
        if (productInEntity == null && productOutEntity == null) {
            return;
        }
        List<Entity> operations = new ArrayList<Entity>();
        if (productInEntity != null) {
            operations = getTechOperationComponent(getProductInCompList(productInEntity));
        }
        if (productOutEntity != null) {
            operations.addAll(getTechOperationComponent(getProductOutCompList(productOutEntity)));
        }

        GridComponent grid = (GridComponent) view.getComponentByReference("grid");
        grid.setEntities(getTasks(operations));
        grid.performEvent(view, "refresh");
    }

    private List<Entity> getTasks(final List<Entity> tocs) {
        List<Entity> tasks = new ArrayList<Entity>();
        for (Entity toc : tocs) {
            Entity techOperCompOperationalTask = dataDefinitionService
                    .get(OperationalTasksForOrdersConstants.PLUGIN_IDENTIFIER,
                            OperationalTasksForOrdersConstants.MODEL_TECH_OPER_COMP_OPERATIONAL_TASKS).find()
                    .add(SearchRestrictions.belongsTo(TechOperCompOperationalTasksFields.TECHNOLOGY_OPERATION_COMPONENT, toc))
                    .uniqueResult();
            List<Entity> tasksForTOCOT = dataDefinitionService
                    .get(OperationalTasksConstants.PLUGIN_IDENTIFIER, OperationalTasksConstants.MODEL_OPERATIONAL_TASK).find()
                    .add(SearchRestrictions.belongsTo("techOperCompOperationalTasks", techOperCompOperationalTask)).list()
                    .getEntities();
            for (Entity taskForTOCOT : tasksForTOCOT) {
                if (!tasks.contains(taskForTOCOT)) {
                    tasks.add(taskForTOCOT);
                }
            }
        }
        return tasks;
    }

    private List<Entity> getTechOperationComponent(final List<Entity> products) {
        List<Entity> operations = new ArrayList<Entity>();
        for (Entity product : products) {
            Entity operComp = product.getBelongsToField("operationComponent");
            if (!operations.contains(operComp)) {
                operations.add(operComp);
            }
        }
        return operations;
    }

    private List<Entity> getProductInCompList(final Entity productIn) {
        return dataDefinitionService
                .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT).find()
                .add(SearchRestrictions.belongsTo("product", productIn)).list().getEntities();
    }

    private List<Entity> getProductOutCompList(final Entity productOut) {
        return dataDefinitionService
                .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_OPERATION_PRODUCT_OUT_COMPONENT).find()
                .add(SearchRestrictions.belongsTo("product", productOut)).list().getEntities();
    }

}
