package com.qcadoo.mes.orders.hooks;

import static com.qcadoo.mes.orders.constants.OrderFields.TECHNOLOGY;
import static com.qcadoo.mes.orders.constants.OrderFields.TECHNOLOGY_OPERATION_COMPONENT;
import static com.qcadoo.mes.technologies.constants.TechnologyFields.OPERATION_COMPONENTS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.mes.orders.constants.OrderFields;
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

    @Transactional
    public void createTechnologyInstanceForOrder(final DataDefinition dataDefinition, final Entity entity) {
        if (entity.getBelongsToField(TECHNOLOGY) == null || !shouldPropagateFromLowerInstance(entity)) {
            return;
        }
        DataDefinition technologyOperationComponentDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY_INSTANCE_OPERATION_COMPONENT);

        Entity technology = entity.getBelongsToField(TECHNOLOGY);

        EntityTree technologyOperationComponents = entity.getTreeField(OrderFields.TECHNOLOGY_OPERATION_COMPONENT);

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

        entity.setField(OPERATION_COMPONENTS, Collections.singletonList(createTechnologyOperationComponent(
                operationComponentRoot, entity, technology, null, technologyOperationComponentDD)));
    }

    private boolean shouldPropagateFromLowerInstance(final Entity order) {
        return !hasOrderOperationComponents(order) || technologyWasChanged(order);
    }

    @SuppressWarnings("unchecked")
    private boolean hasOrderOperationComponents(final Entity order) {
        return ((order.getField(TECHNOLOGY_OPERATION_COMPONENT) != null) && !((List<Entity>) order
                .getField(TECHNOLOGY_OPERATION_COMPONENT)).isEmpty());
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

        if ("operation".equals(operationComponent.getField("entityType"))) {
            createOrCopyOrderOperationComponent(operationComponent, order, technology, technologyInstanceOperationComponentDD,
                    technologyInstanceOperationComponent);
        } else {
            Entity referenceTechnology = operationComponent.getBelongsToField("referenceTechnology");
            createOrCopyOrderOperationComponent(referenceTechnology.getTreeField(OPERATION_COMPONENTS).getRoot(), order,
                    technology, technologyInstanceOperationComponentDD, technologyInstanceOperationComponent);
        }

        return technologyInstanceOperationComponentDD.save(technologyInstanceOperationComponent);
    }

    private void createOrCopyOrderOperationComponent(final EntityTreeNode operationComponent, final Entity order,
            final Entity technology, final DataDefinition orderOperationComponentDD,
            final Entity technologyInstanceOperationComponent) {

        technologyInstanceOperationComponent.setField("operation", operationComponent.getBelongsToField("operation"));
        technologyInstanceOperationComponent.setField("technologyOperationComponent", operationComponent);
        technologyInstanceOperationComponent.setField("priority", operationComponent.getField("priority"));
        technologyInstanceOperationComponent.setField("entityType", "operation");

        List<Entity> newTechnologyOperationComponents = new ArrayList<Entity>();

        for (EntityTreeNode child : operationComponent.getChildren()) {
            newTechnologyOperationComponents.add(createTechnologyOperationComponent(child, order, technology,
                    technologyInstanceOperationComponent, orderOperationComponentDD));
        }

        technologyInstanceOperationComponent.setField("children", newTechnologyOperationComponents);
    }

}
