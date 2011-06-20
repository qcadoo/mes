package com.qcadoo.mes.productionScheduling;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
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

    public void copyDefaultDataToOperationComponent(final DataDefinition dataDefinition, final Entity entity) {
        Entity technology = entity.getBelongsToField("technology");

        if (technology == null) {
            return;
        }

        if (dataDefinitionService.get("productionScheduling", "orderOperationComponent").find()
                .add(SearchRestrictions.belongsTo("order", entity)).add(SearchRestrictions.belongsTo("technology", technology))
                .list().getTotalNumberOfEntities() > 0) {
            return;
        }

        List<Entity> entities = dataDefinitionService.get("productionScheduling", "orderOperationComponent").find()
                .add(SearchRestrictions.belongsTo("order", entity)).list().getEntities();

    }

}
