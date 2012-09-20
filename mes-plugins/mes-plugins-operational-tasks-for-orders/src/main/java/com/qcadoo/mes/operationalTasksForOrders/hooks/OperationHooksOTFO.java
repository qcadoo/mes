package com.qcadoo.mes.operationalTasksForOrders.hooks;

import static com.qcadoo.mes.technologies.constants.OperationFields.NAME;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.operationalTasks.constants.OperationalTasksConstants;
import com.qcadoo.mes.operationalTasks.constants.OperationalTasksFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyInstanceOperCompFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class OperationHooksOTFO {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void changedNameOperationTasksWhenEntityNameChanged(final DataDefinition dataDefinition, final Entity entity) {
        if (entity.getId() == null) {
            return;
        }
        Entity operation = dataDefinition.get(entity.getId());
        String entityName = entity.getStringField(NAME);
        String operationName = operation.getStringField(NAME);

        if (!entityName.equals(operationName)) {
            changedNameOperationTasks(entity);
        }
    }

    private void changedNameOperationTasks(final Entity operation) {
        DataDefinition techInstOperCompDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY_INSTANCE_OPERATION_COMPONENT);
        DataDefinition operationalTasksDD = dataDefinitionService.get(OperationalTasksConstants.PLUGIN_IDENTIFIER,
                OperationalTasksConstants.MODEL_OPERATIONAL_TASK);
        List<Entity> techInstOperCompsWithOperation = techInstOperCompDD.find()
                .add(SearchRestrictions.belongsTo(TechnologyInstanceOperCompFields.OPERATION, operation)).list().getEntities();
        for (Entity techInstOperComp : techInstOperCompsWithOperation) {
            List<Entity> operationalTasksList = operationalTasksDD.find()
                    .add(SearchRestrictions.belongsTo("technologyInstanceOperationComponent", techInstOperComp)).list()
                    .getEntities();
            for (Entity operationalTask : operationalTasksList) {
                operationalTask.setField(OperationalTasksFields.NAME, operation.getStringField(NAME));
                operationalTasksDD.save(operationalTask);
            }
        }
    }
}
