/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.2
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

import com.google.common.collect.Sets;
import com.qcadoo.mes.productionScheduling.constants.ProductionSchedulingConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.EntityTreeNode;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class NormOrderService {

    private static final String OPERATION_COMPONENTS_L = "operationComponents";

    private static final String TECHNOLOGY_FIELD = "technology";

    private static final String COUNT_REALIZED_FIELD = "countRealized";

    private static final String OPERATION = "operation";

    private static final String ORDER_OPERATION_COMPONENTS = "orderOperationComponents";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public boolean checkIfChosenTechnologyTreeIsNotEmpty(final DataDefinition orderDataDefinition, final Entity order) {
        Entity technology = order.getBelongsToField(TECHNOLOGY_FIELD);
        if (technology == null) {
            return true;
        }

        List<Entity> technologyTree = technology.getTreeField(OPERATION_COMPONENTS_L);
        if (technologyTree.isEmpty()) {
            order.addError(orderDataDefinition.getField(TECHNOLOGY_FIELD), "productionScheduling.order.emptyTechnologyTree");
            return false;
        }
        return true;
    }

    @Transactional
    public void createTechnologyInstanceForOrder(final DataDefinition dataDefinition, final Entity entity) {
        if (!checkIfChosenTechnologyTreeIsNotEmpty(dataDefinition, entity) || !shouldPropagateFromLowerInstance(entity)) {
            return;
        }
        DataDefinition orderOperationComponentDD = dataDefinitionService.get(ProductionSchedulingConstants.PLUGIN_IDENTIFIER,
                ProductionSchedulingConstants.MODEL_ORDER_OPERATION_COMPONENT);

        EntityTree orderOperationComponents = entity.getTreeField(ORDER_OPERATION_COMPONENTS);

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

        EntityTree operationComponents = technology.getTreeField(OPERATION_COMPONENTS_L);

        entity.setField(ORDER_OPERATION_COMPONENTS, Collections.singletonList(createOrderOperationComponent(
                operationComponents.getRoot(), entity, technology, null, orderOperationComponentDD)));
    }

    private boolean shouldPropagateFromLowerInstance(final Entity order) {
        return !hasOrderOperationComponents(order) || technologyWasChanged(order);
    }

    @SuppressWarnings("unchecked")
    private boolean hasOrderOperationComponents(final Entity order) {
        if (order.getField(ORDER_OPERATION_COMPONENTS) == null) {
            return false;
        }
        return !((List<Entity>) order.getField(ORDER_OPERATION_COMPONENTS)).isEmpty();
    }

    private boolean technologyWasChanged(final Entity order) {
        Entity existingOrder = getExistingOrder(order);
        if (existingOrder == null) {
            return false;
        }

        Entity technology = order.getBelongsToField(TECHNOLOGY_FIELD);
        Entity existingOrderTechnology = existingOrder.getBelongsToField(TECHNOLOGY_FIELD);
        return !existingOrderTechnology.equals(technology);
    }

    private Entity getExistingOrder(final Entity order) {
        if (order.getId() == null) {
            return null;
        }
        return order.getDataDefinition().get(order.getId());
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
            createOrCopyOrderOperationComponent(referenceTechnology.getTreeField(OPERATION_COMPONENTS_L).getRoot(), order,
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
        FieldComponent countMachineUnit = (FieldComponent) viewDefinitionState.getComponentByReference("countMachineUNIT");
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
            countMachineUnit.setVisible(true);
            if (countMachine.getFieldValue() == null || !StringUtils.hasText(String.valueOf(countMachine.getFieldValue()))) {
                countMachine.setFieldValue("1");
            }
        } else {
            countMachine.setVisible(false);
            countMachine.setRequired(false);
            countMachineUnit.setVisible(false);
        }

        timeNextOperation.setEnabled(true);
        timeNextOperation.setRequired(true);
    }

    public void fillUnitFields(final ViewDefinitionState view) {
        FieldComponent component = null;
        Entity formEntity = ((FormComponent) view.getComponentByReference("form")).getEntity();

        // we can pass units only to technology level operations
        if (formEntity.getId() == null || !"orderOperationComponent".equals(formEntity.getDataDefinition().getName())) {
            return;
        }

        // be sure that entity isn't in detached state before you wander through the relationship
        formEntity = formEntity.getDataDefinition().get(formEntity.getId());
        // you can use someEntity.getSTH().getSTH() only when you are 100% sure that all the passers-relations
        // will not return null (i.e. all relations using below are mandatory on the model definition level)
        String unit = formEntity.getBelongsToField("technology").getBelongsToField("product").getField("unit").toString();
        for (String referenceName : Sets.newHashSet("countMachineUNIT", "productionInOneCycleUNIT")) {
            component = (FieldComponent) view.getComponentByReference(referenceName);
            if (component == null) {
                continue;
            }
            component.setFieldValue(unit);
            component.requestComponentUpdateState();
        }
    }

}
