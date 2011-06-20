package com.qcadoo.mes.productionScheduling;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;

@Service
public class OrderService {

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
    public void copyDefaultDataToOperationComponent(final DataDefinition dataDefinition, final Entity entity) {
        Entity technology = entity.getBelongsToField("technology");

        if (technology == null) {
            return;
        }

        EntityTree orderOperationComponents = entity.getTreeField("orderOperationComponents");

        if (orderOperationComponents.size() > 0
                && orderOperationComponents.getRoot().getBelongsToField("technology").getId().equals(technology.getId())) {
            return;
        }

        DataDefinition orderOperationComponentDD = dataDefinitionService.get("productionScheduling", "orderOperationComponent");
        DataDefinition machineInOrderOperationComponentDD = dataDefinitionService.get("productionScheduling", "machineInOrderOperationComponent");

        if (orderOperationComponents.size() > 0) {
            orderOperationComponentDD.delete(orderOperationComponents.getRoot().getId());
        }

        EntityTree operationComponents = technology.getTreeField("operationComponents");

        Entity orderOperationComponent = orderOperationComponentDD.create();

        Map<Long, Entity> newOrderOperationComponents = new LinkedHashMap<Long, Entity>();

        for (Entity operationComponent : operationComponents) {
            orderOperationComponent.setField("order", entity);
            orderOperationComponent.setField("technology", technology);
            orderOperationComponent.setField("operation", operationComponent.getBelongsToField("operation"));
            orderOperationComponent.setField("technologyOperationComponent", operationComponent);
            orderOperationComponent.setField("parent",
                    newOrderOperationComponents.get(operationComponent.getBelongsToField("parent").getId()));
            orderOperationComponent.setField("priority", operationComponent.getField("priority"));
            orderOperationComponent.setField("tpz", operationComponent.getField("priority"));
            orderOperationComponent.setField("tj", operationComponent.getField("priority"));
            orderOperationComponent.setField("useDefaultValue", operationComponent.getField("priority"));
            orderOperationComponent.setField("useMachineNorm", operationComponent.getField("priority"));
            orderOperationComponent.setField("countRealized", operationComponent.getField("priority"));
            orderOperationComponent.setField("countMachine", operationComponent.getField("priority"));
            orderOperationComponent.setField("timeNextOperation", operationComponent.getField("priority"));

            EntityList machineInOperationComponents = operationComponent.getHasManyField("machineInOperationComponent");
            
            List<Entity> newMachineInOrderOperationComponents = new ArrayList<Entity>();
            
            for(Entity machineInOperationComponent : machineInOperationComponents) {
                Entity machineInOrderOperationComponent = machineInOrderOperationComponentDD.create();
                machineInOrderOperationComponent.setField("orderOperationComponent", orderOperationComponent);
                machineInOrderOperationComponent.setField("machine", machineInOperationComponent.getBelongsToField("machine"));
                
                <boolean name="useDefaultValue" required="true" />
                <string name="tpz" />
                <string name="tj" />
                <string name="parallel" />
                <boolean name="isActive" />
                <belongsTo name="machine" plugin="basic" model="machine" required="true" />
                <belongsTo name="orderOperationComponent" model="orderOperationComponent" required="true" />
                
                
            }
            
            newOrderOperationComponents.put(operationComponent.getId(), orderOperationComponent);
        }
    }
}
