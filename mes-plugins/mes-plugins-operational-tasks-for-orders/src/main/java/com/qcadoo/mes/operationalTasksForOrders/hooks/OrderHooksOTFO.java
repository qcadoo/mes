package com.qcadoo.mes.operationalTasksForOrders.hooks;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.operationalTasks.constants.OperationalTasksConstants;
import com.qcadoo.mes.operationalTasks.constants.OperationalTasksFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class OrderHooksOTFO {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void changedProductionLine(final DataDefinition dataDefinition, final Entity entity) {
        if (entity.getId() == null) {
            return;
        }
        Entity order = dataDefinition.get(entity.getId());
        Entity productionLine = entity.getBelongsToField(OrderFields.PRODUCTION_LINE);
        Entity orderProductionLine = order.getBelongsToField(OrderFields.PRODUCTION_LINE);
        if ((orderProductionLine == null && productionLine == null) || orderProductionLine.equals(productionLine)) {
            return;
        } else {
            changedProductionLineInOperationalTasks(order, productionLine);
        }
    }

    private void changedProductionLineInOperationalTasks(final Entity order, final Entity productionLine) {
        DataDefinition operationalTasksDD = dataDefinitionService.get(OperationalTasksConstants.PLUGIN_IDENTIFIER,
                OperationalTasksConstants.MODEL_OPERATIONAL_TASK);
        List<Entity> operationalTasksList = operationalTasksDD.find().add(SearchRestrictions.belongsTo("order", order)).list()
                .getEntities();
        for (Entity operationalTask : operationalTasksList) {
            operationalTask.setField(OperationalTasksFields.PRODUCTION_LINE, productionLine);
            operationalTasksDD.save(operationalTask);
        }
    }
}
