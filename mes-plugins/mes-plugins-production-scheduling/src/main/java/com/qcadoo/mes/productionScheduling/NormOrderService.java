/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.0
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
package com.qcadoo.mes.productionScheduling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.qcadoo.mes.productionScheduling.constants.ProductionSchedulingConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.EntityTreeNode;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class NormOrderService {

    private static final String TECHNOLOGY_FIELD = "technology";

    private static final String COUNT_REALIZED_FIELD = "countRealized";

    private static final String OPERATION = "operation";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public boolean checkIfChosenTechnologyTreeIsNotEmpty(final DataDefinition orderDataDefinition, final Entity order) {
        Entity technology = order.getBelongsToField(TECHNOLOGY_FIELD);
        if (technology == null) {
            return true;
        }

        List<Entity> technologyTree = technology.getTreeField("operationComponents");
        if (technologyTree.isEmpty()) {
            order.addError(orderDataDefinition.getField(TECHNOLOGY_FIELD), "productionScheduling.order.emptyTechnologyTree");
            return false;
        }
        return true;
    }

    @Transactional
    public void createTechnologyInstanceForOrder(final DataDefinition dataDefinition, final Entity entity) {
        if (!checkIfChosenTechnologyTreeIsNotEmpty(dataDefinition, entity)) {
            return;
        }
        DataDefinition orderOperationComponentDD = dataDefinitionService.get(ProductionSchedulingConstants.PLUGIN_IDENTIFIER,
                ProductionSchedulingConstants.MODEL_ORDER_OPERATION_COMPONENT);

        EntityTree orderOperationComponents = entity.getTreeField("orderOperationComponents");

        Entity technology = entity.getBelongsToField(TECHNOLOGY_FIELD);

        if (technology == null) {
            if (orderOperationComponents != null && orderOperationComponents.size() > 0) {
                orderOperationComponentDD.delete(orderOperationComponents.getRoot().getId());
            }
            return;
        }

        if (orderOperationComponents != null && orderOperationComponents.size() > 0) {
            if (orderOperationComponents.getRoot().getBelongsToField(TECHNOLOGY_FIELD).getId().equals(technology.getId())) {
                return;
            }

            orderOperationComponentDD.delete(orderOperationComponents.getRoot().getId());
        }

        EntityTree operationComponents = technology.getTreeField("operationComponents");

        entity.setField("orderOperationComponents", Collections.singletonList(createOrderOperationComponent(
                operationComponents.getRoot(), entity, technology, null, orderOperationComponentDD)));
    }

    private Entity createOrderOperationComponent(final EntityTreeNode operationComponent, final Entity order,
            final Entity technology, final Entity parent, final DataDefinition orderOperationComponentDD) {
        Entity orderOperationComponent = orderOperationComponentDD.create();

        orderOperationComponent.setField("order", order);
        orderOperationComponent.setField("technology", technology);
        orderOperationComponent.setField("parent", parent);

        if (OPERATION.equals(operationComponent.getField("entityType"))) {
            createOrCopyOrderOperationComponent(operationComponent, order, technology, orderOperationComponentDD,
                    orderOperationComponent);
        } else {
            Entity referenceTechnology = operationComponent.getBelongsToField("referenceTechnology");
            createOrCopyOrderOperationComponent(referenceTechnology.getTreeField("orderOperationComponents").getRoot(), order,
                    technology, orderOperationComponentDD, orderOperationComponent);
        }

        return orderOperationComponent;
    }

    private void createOrCopyOrderOperationComponent(final EntityTreeNode operationComponent, final Entity order,
            final Entity technology, final DataDefinition orderOperationComponentDD, final Entity orderOperationComponent) {
        orderOperationComponent.setField(OPERATION, operationComponent.getBelongsToField(OPERATION));
        orderOperationComponent.setField("technologyOperationComponent", operationComponent);
        orderOperationComponent.setField("priority", operationComponent.getField("priority"));
        orderOperationComponent.setField("entityType", OPERATION);
        orderOperationComponent.setField("tpz", operationComponent.getField("tpz"));
        orderOperationComponent.setField("tj", operationComponent.getField("tj"));
        orderOperationComponent.setField("productionInOneCycle", operationComponent.getField("productionInOneCycle"));
        orderOperationComponent.setField(
                COUNT_REALIZED_FIELD,
                operationComponent.getField(COUNT_REALIZED_FIELD) == null ? "01all" : operationComponent
                        .getField(COUNT_REALIZED_FIELD));
        orderOperationComponent.setField("countMachine", operationComponent.getField("countMachine"));
        orderOperationComponent.setField("timeNextOperation", operationComponent.getField("timeNextOperation"));
        orderOperationComponent.setField("nodeNumber", operationComponent.getField("nodeNumber"));
        orderOperationComponent.setField("machineUtilization", operationComponent.getField("machineUtilization"));
        orderOperationComponent.setField("laborUtilization", operationComponent.getField("laborUtilization"));

        List<Entity> newOrderOperationComponents = new ArrayList<Entity>();

        for (EntityTreeNode child : operationComponent.getChildren()) {
            newOrderOperationComponents.add(createOrderOperationComponent(child, order, technology, orderOperationComponent,
                    orderOperationComponentDD));
        }

        orderOperationComponent.setField("children", newOrderOperationComponents);
    }

    public void showOrderParameters(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState,
            final String[] args) {
        Long orderId = (Long) triggerState.getFieldValue();

        if (orderId != null) {
            String url = "../page/productionScheduling/orderOperationComponentList.html?context={\"form.id\":\"" + orderId
                    + "\"}";
            viewDefinitionState.redirectTo(url, false, true);
        }
    }

    public void changeCountRealized(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        // ignore
    }

    public void disableComponents(final ViewDefinitionState viewDefinitionState) {
        FieldComponent tpz = (FieldComponent) viewDefinitionState.getComponentByReference("tpz");
        FieldComponent tj = (FieldComponent) viewDefinitionState.getComponentByReference("tj");
        FieldComponent productionInOneCycle = (FieldComponent) viewDefinitionState
                .getComponentByReference("productionInOneCycle");
        FieldComponent countRealized = (FieldComponent) viewDefinitionState.getComponentByReference(COUNT_REALIZED_FIELD);
        FieldComponent countMachine = (FieldComponent) viewDefinitionState.getComponentByReference("countMachine");
        FieldComponent timeNextOperation = (FieldComponent) viewDefinitionState.getComponentByReference("timeNextOperation");

        tpz.setEnabled(true);
        tpz.setRequired(true);
        tj.setEnabled(true);
        tj.setRequired(true);
        productionInOneCycle.setEnabled(true);
        productionInOneCycle.setRequired(true);
        countRealized.setEnabled(true);
        countRealized.setRequired(true);

        if ("02specified".equals(countRealized.getFieldValue())) {
            countMachine.setVisible(true);
            countMachine.setEnabled(true);
            countMachine.setRequired(true);
            if (countMachine.getFieldValue() == null || !StringUtils.hasText(String.valueOf(countMachine.getFieldValue()))) {
                countMachine.setFieldValue("1");
            }
        } else {
            countMachine.setVisible(false);
            countMachine.setRequired(false);
        }

        timeNextOperation.setEnabled(true);
        timeNextOperation.setRequired(true);
    }

}
