/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.5
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
package com.qcadoo.mes.orders.hooks;

import static com.qcadoo.mes.orders.constants.OrderFields.TECHNOLOGY;
import static com.qcadoo.mes.orders.constants.OrderFields.TECHNOLOGY_INSTANCE_OPERATION_COMPONENTS;
import static com.qcadoo.mes.technologies.constants.TechnologyFields.OPERATION_COMPONENTS;
import static com.qcadoo.mes.technologies.constants.TechnologyInstanceOperCompFields.OPERATION;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.mes.productionLines.ProductionLinesService;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.EntityTreeNode;

@Service
public class TechnologyOperCompHooksOrder {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ProductionLinesService productionLinesService;

    @Transactional
    public void createTechnologyInstanceFromScratch(final DataDefinition dataDefinition, final Entity order) {
        Entity technology = order.getBelongsToField("technology");

        if (technology == null) {
            return;
        }

        EntityTreeNode operationComponentRoot = technology.getTreeField(TechnologyFields.OPERATION_COMPONENTS).getRoot();

        DataDefinition technologyOperationComponentDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY_INSTANCE_OPERATION_COMPONENT);

        order.setField(TECHNOLOGY_INSTANCE_OPERATION_COMPONENTS, Collections.singletonList(createTechnologyOperationComponent(
                operationComponentRoot, order, technology, null, technologyOperationComponentDD)));
    }

    @Transactional
    public void createTechnologyInstanceForOrder(final DataDefinition dataDefinition, final Entity order) {
        if (order.getBelongsToField(TECHNOLOGY) == null || !shouldCreateTechnologyInstance(order)) {
            return;
        }

        DataDefinition technologyOperationComponentDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY_INSTANCE_OPERATION_COMPONENT);

        Entity technology = order.getBelongsToField(TECHNOLOGY);

        EntityTree technologyOperationComponents = order.getTreeField(TECHNOLOGY_INSTANCE_OPERATION_COMPONENTS);

        if (technology == null) {
            if (technologyOperationComponents != null && technologyOperationComponents.size() > 0) {
                technologyOperationComponentDD.delete(technologyOperationComponents.getRoot().getId());
            }
            return;
        }

        if (technologyOperationComponents != null && technologyOperationComponents.size() > 0) {
            if (technologyOperationComponents.getRoot().getBelongsToField(TECHNOLOGY).getId().equals(technology.getId())) {
                return;
            }

            technologyOperationComponentDD.delete(technologyOperationComponents.getRoot().getId());
        }

        EntityTreeNode operationComponentRoot = technology.getTreeField(TechnologyFields.OPERATION_COMPONENTS).getRoot();

        order.setField(TECHNOLOGY_INSTANCE_OPERATION_COMPONENTS, Collections.singletonList(createTechnologyOperationComponent(
                operationComponentRoot, order, technology, null, technologyOperationComponentDD)));
    }

    private boolean shouldCreateTechnologyInstance(final Entity order) {
        return !hasTechnologyInstanceOperationComponents(order) || technologyWasChanged(order);
    }

    @SuppressWarnings("unchecked")
    private boolean hasTechnologyInstanceOperationComponents(final Entity order) {
        return ((order.getField(TECHNOLOGY_INSTANCE_OPERATION_COMPONENTS) != null) && !((List<Entity>) order
                .getField(TECHNOLOGY_INSTANCE_OPERATION_COMPONENTS)).isEmpty());
    }

    private boolean technologyWasChanged(final Entity order) {
        Entity existingOrder = getExistingOrder(order);
        if (existingOrder == null) {
            return false;
        }

        Entity technology = order.getBelongsToField(TECHNOLOGY);
        Entity existingOrderTechnology = existingOrder.getBelongsToField(TECHNOLOGY);
        if (existingOrderTechnology == null) {
            return true;
        }
        return !existingOrderTechnology.equals(technology);
    }

    private Entity getExistingOrder(final Entity order) {
        if (order.getId() == null) {
            return null;
        }
        return order.getDataDefinition().get(order.getId());
    }

    private Entity createTechnologyOperationComponent(final EntityTreeNode operationComponent, final Entity order,
            final Entity technology, final Entity parent, final DataDefinition technologyInstanceOperationComponentDD) {
        Entity technologyInstanceOperationComponent = technologyInstanceOperationComponentDD.create();

        technologyInstanceOperationComponent.setField("order", order);
        technologyInstanceOperationComponent.setField("technology", technology);
        technologyInstanceOperationComponent.setField("parent", parent);

        if (OPERATION.equals(operationComponent.getStringField("entityType"))) {
            createOrCopyTechnologyInstanceOperationComponent(operationComponent, order, technology,
                    technologyInstanceOperationComponentDD, technologyInstanceOperationComponent);
        } else {
            Entity referenceTechnology = operationComponent.getBelongsToField("referenceTechnology");
            createOrCopyTechnologyInstanceOperationComponent(referenceTechnology.getTreeField(OPERATION_COMPONENTS).getRoot(),
                    order, technology, technologyInstanceOperationComponentDD, technologyInstanceOperationComponent);
        }

        return technologyInstanceOperationComponentDD.save(technologyInstanceOperationComponent);
    }

    private void createOrCopyTechnologyInstanceOperationComponent(final EntityTreeNode operationComponent, final Entity order,
            final Entity technology, final DataDefinition technologyInstanceOperationComponentDD,
            final Entity technologyInstanceOperationComponent) {

        technologyInstanceOperationComponent.setField(OPERATION, operationComponent.getBelongsToField(OPERATION));
        technologyInstanceOperationComponent.setField("technologyOperationComponent", operationComponent);
        technologyInstanceOperationComponent.setField("priority", operationComponent.getField("priority"));
        technologyInstanceOperationComponent.setField("nodeNumber", operationComponent.getField("nodeNumber"));
        technologyInstanceOperationComponent.setField("entityType", OPERATION);
        technologyInstanceOperationComponent.setField("quantityOfWorkstationTypes",
                productionLinesService.getWorkstationTypesCount(operationComponent, order.getBelongsToField("productionLine")));

        List<Entity> newTechnologyOperationComponents = new ArrayList<Entity>();

        for (EntityTreeNode child : operationComponent.getChildren()) {
            newTechnologyOperationComponents.add(createTechnologyOperationComponent(child, order, technology,
                    technologyInstanceOperationComponent, technologyInstanceOperationComponentDD));
        }

        technologyInstanceOperationComponent.setField("children", newTechnologyOperationComponents);
    }

}
