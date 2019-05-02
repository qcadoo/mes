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
package com.qcadoo.mes.operationalTasksForOrders.hooks;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.operationalTasks.constants.OperationalTaskDtoFields;
import com.qcadoo.mes.operationalTasks.constants.OperationalTasksConstants;
import com.qcadoo.mes.operationalTasksForOrders.OperationalTasksForOrdersService;
import com.qcadoo.mes.operationalTasksForOrders.constants.OperationalTaskDtoFieldsOTFO;
import com.qcadoo.mes.operationalTasksForOrders.constants.TechOperCompOperationalTasksFields;
import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.mes.technologies.constants.OperationProductOutComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchProjections;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class OperationalTasksListHooksOTFO {

    private static final String L_GRID = "grid";

    private static final String L_DOT = ".";

    private static final String L_ID = "id";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private OperationalTasksForOrdersService operationalTasksForOrdersService;

    public void beforeRender(final ViewDefinitionState view) {
        addDiscriminatorRestrictionToGrid(view);
    }

    public void addDiscriminatorRestrictionToGrid(final ViewDefinitionState view) {
        LookupComponent productInLookup = (LookupComponent) view.getComponentByReference(OperationalTaskDtoFields.PRODUCT_IN);
        LookupComponent productOutLookup = (LookupComponent) view.getComponentByReference(OperationalTaskDtoFields.PRODUCT_OUT);

        Entity productIn = productInLookup.getEntity();
        Entity productOut = productOutLookup.getEntity();

        if (Objects.isNull(productIn) && Objects.isNull(productOut)) {
            return;
        }

        List<Entity> technologyOperationComponents = Lists.newArrayList();

        if (!Objects.isNull(productIn)) {
            technologyOperationComponents.addAll(getTechnologyOperationComponents(
                    TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT,
                    OperationProductInComponentFields.OPERATION_COMPONENT, OperationProductInComponentFields.PRODUCT, productIn));
        }
        if (!Objects.isNull(productOut)) {
            technologyOperationComponents
                    .addAll(getTechnologyOperationComponents(TechnologiesConstants.MODEL_OPERATION_PRODUCT_OUT_COMPONENT,
                            OperationProductOutComponentFields.OPERATION_COMPONENT, OperationProductOutComponentFields.PRODUCT,
                            productOut));
        }

        GridComponent grid = (GridComponent) view.getComponentByReference(L_GRID);

        List<Entity> operationalTaskDtos = getOperationalTaskDtos(technologyOperationComponents);

        grid.setEntities(operationalTaskDtos);
        grid.performEvent(view, "refresh");
    }

    private List<Entity> getOperationalTaskDtos(final List<Entity> technologyOperationComponents) {
        List<Entity> operationalTaskDtos = Lists.newArrayList();

        if (!technologyOperationComponents.isEmpty()) {
            List<Long> technologyOperationComponentIds = technologyOperationComponents.stream()
                    .map(technologyOperationComponent -> technologyOperationComponent.getId()).collect(Collectors.toList());

            operationalTaskDtos = dataDefinitionService
                    .get(OperationalTasksConstants.PLUGIN_IDENTIFIER, OperationalTasksConstants.MODEL_OPERATIONAL_TASK_DTO).find()
                    .createAlias(OperationalTaskDtoFieldsOTFO.TECH_OPER_COMP_OPERATIONAL_TASK,
                            OperationalTaskDtoFieldsOTFO.TECH_OPER_COMP_OPERATIONAL_TASK, JoinType.LEFT)
                    .createAlias(
                            OperationalTaskDtoFieldsOTFO.TECH_OPER_COMP_OPERATIONAL_TASK + L_DOT
                                    + TechOperCompOperationalTasksFields.TECHNOLOGY_OPERATION_COMPONENT,
                            TechOperCompOperationalTasksFields.TECHNOLOGY_OPERATION_COMPONENT, JoinType.LEFT)
                    .add(SearchRestrictions.in(TechOperCompOperationalTasksFields.TECHNOLOGY_OPERATION_COMPONENT + L_DOT + L_ID,
                            technologyOperationComponentIds))
                    .list().getEntities();
        }

        return operationalTaskDtos;
    }

    private List<Entity> getTechnologyOperationComponents(final String modelName,
            final String technologyOperationComponentFieldName, final String productFieldName, final Entity product) {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, modelName).find()
                .add(SearchRestrictions.belongsTo(productFieldName, product))
                .setProjection(SearchProjections.distinct(SearchProjections.field(technologyOperationComponentFieldName)))
                .addOrder(SearchOrders.desc(technologyOperationComponentFieldName)).list().getEntities();
    }

}
