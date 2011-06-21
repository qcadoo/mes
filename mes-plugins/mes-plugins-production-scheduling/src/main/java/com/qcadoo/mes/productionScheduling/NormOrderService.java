package com.qcadoo.mes.productionScheduling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.EntityTreeNode;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;

@Service
public class NormOrderService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public boolean checkMachineInOrderOperationComponentUniqueness(final DataDefinition dataDefinition, final Entity entity) {
        Entity machine = entity.getBelongsToField("machine");
        Entity operationComponent = entity.getBelongsToField("orderOperationComponent");

        if (operationComponent == null || machine == null) {
            return false;
        }

        SearchResult searchResult = dataDefinition.find().add(SearchRestrictions.belongsTo("machine", machine))
                .add(SearchRestrictions.belongsTo("orderOperationComponent", operationComponent)).list();

        if (searchResult.getTotalNumberOfEntities() > 0 && !searchResult.getEntities().get(0).getId().equals(entity.getId())) {
            entity.addError(dataDefinition.getField("machine"),
                    "productionScheduling.validate.global.error.machineInOperationDuplicated");
            return false;
        } else {
            return true;
        }
    }

    @Transactional
    public void createTechnologyInstanceForOrder(final DataDefinition dataDefinition, final Entity entity) {
        DataDefinition orderOperationComponentDD = dataDefinitionService.get("productionScheduling", "orderOperationComponent");
        DataDefinition machineInOrderOperationComponentDD = dataDefinitionService.get("productionScheduling",
                "machineInOrderOperationComponent");

        EntityTree orderOperationComponents = entity.getTreeField("orderOperationComponents");

        Entity technology = entity.getBelongsToField("technology");

        if (technology == null) {
            if (orderOperationComponents != null && orderOperationComponents.size() > 0) {
                orderOperationComponentDD.delete(orderOperationComponents.getRoot().getId());
            }
            return;
        }

        if (orderOperationComponents != null && orderOperationComponents.size() > 0
                && orderOperationComponents.getRoot().getBelongsToField("technology").getId().equals(technology.getId())) {
            return;
        }

        if (orderOperationComponents != null && orderOperationComponents.size() > 0) {
            orderOperationComponentDD.delete(orderOperationComponents.getRoot().getId());
        }

        EntityTree operationComponents = technology.getTreeField("operationComponents");

        entity.setField("orderOperationComponents", Collections.singletonList(createOrderOperationComponent(
                operationComponents.getRoot(), entity, technology, null, orderOperationComponentDD,
                machineInOrderOperationComponentDD)));
    }

    private Entity createOrderOperationComponent(final EntityTreeNode operationComponent, final Entity order,
            final Entity technology, final Entity parent, final DataDefinition orderOperationComponentDD,
            final DataDefinition machineInOrderOperationComponentDD) {
        Entity orderOperationComponent = orderOperationComponentDD.create();
        orderOperationComponent.setField("order", order);
        orderOperationComponent.setField("technology", technology);
        orderOperationComponent.setField("operation", operationComponent.getBelongsToField("operation"));
        orderOperationComponent.setField("technologyOperationComponent", operationComponent);
        orderOperationComponent.setField("parent", parent);
        orderOperationComponent.setField("priority", operationComponent.getField("priority"));
        orderOperationComponent.setField("useDefaultValue", true);
        orderOperationComponent.setField("tpz", operationComponent.getField("tpz"));
        orderOperationComponent.setField("tj", operationComponent.getField("tj"));
        orderOperationComponent.setField("useMachineNorm", operationComponent.getField("useMachineNorm"));
        orderOperationComponent.setField("countRealized", operationComponent.getField("countRealizedNorm"));
        orderOperationComponent.setField("countMachine", operationComponent.getField("countMachineNorm"));
        orderOperationComponent.setField("timeNextOperation", operationComponent.getField("timeNextOperationNorm"));

        EntityList machineInOperationComponents = operationComponent.getHasManyField("machineInOperationComponent");

        List<Entity> newMachineInOrderOperationComponents = new ArrayList<Entity>();

        for (Entity machineInOperationComponent : machineInOperationComponents) {
            Entity machineInOrderOperationComponent = machineInOrderOperationComponentDD.create();
            machineInOrderOperationComponent.setField("orderOperationComponent", orderOperationComponent);
            machineInOrderOperationComponent.setField("machine", machineInOperationComponent.getBelongsToField("machine"));
            machineInOrderOperationComponent.setField("useDefaultValue", true);
            machineInOrderOperationComponent.setField("tpz", machineInOperationComponent.getField("tpz"));
            machineInOrderOperationComponent.setField("tj", machineInOperationComponent.getField("tj"));
            machineInOrderOperationComponent.setField("parallel", machineInOperationComponent.getField("parallel"));
            machineInOrderOperationComponent.setField("isActive", machineInOperationComponent.getField("activeMachine"));
            newMachineInOrderOperationComponents.add(machineInOrderOperationComponent);
        }

        orderOperationComponent.setField("machineInOrderOperationComponents", newMachineInOrderOperationComponents);

        List<Entity> newOrderOperationComponents = new ArrayList<Entity>();

        for (EntityTreeNode child : operationComponent.getChildren()) {
            newOrderOperationComponents.add(createOrderOperationComponent(child, order, technology, orderOperationComponent,
                    orderOperationComponentDD, machineInOrderOperationComponentDD));
        }

        orderOperationComponent.setField("children", newOrderOperationComponents);

        return orderOperationComponent;
    }

}
