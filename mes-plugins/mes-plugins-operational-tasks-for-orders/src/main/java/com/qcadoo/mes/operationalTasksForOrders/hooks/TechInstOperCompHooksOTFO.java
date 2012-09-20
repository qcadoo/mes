package com.qcadoo.mes.operationalTasksForOrders.hooks;

import static com.qcadoo.mes.technologies.constants.TechnologyInstanceOperCompFields.COMMENT;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.operationalTasks.constants.OperationalTasksConstants;
import com.qcadoo.mes.operationalTasks.constants.OperationalTasksFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class TechInstOperCompHooksOTFO {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void changedDescriptionOperationTasksWhenCommentEntityChanged(final DataDefinition dataDefinition, final Entity entity) {
        if (entity.getId() == null) {
            return;
        }
        Entity techInstOperComp = dataDefinition.get(entity.getId());
        String entityComment = entity.getStringField(COMMENT) == null ? "" : entity.getStringField(COMMENT);
        String techInstOperCompComment = techInstOperComp.getStringField(COMMENT) == null ? "" : techInstOperComp
                .getStringField(COMMENT);

        if (!entityComment.equals(techInstOperCompComment)) {
            changedDescriptionOperationTasks(entity);
        }
    }

    private void changedDescriptionOperationTasks(final Entity techInstOperComp) {
        DataDefinition operationalTasksDD = dataDefinitionService.get(OperationalTasksConstants.PLUGIN_IDENTIFIER,
                OperationalTasksConstants.MODEL_OPERATIONAL_TASK);
        List<Entity> operationalTasksList = operationalTasksDD.find()
                .add(SearchRestrictions.belongsTo("technologyInstanceOperationComponent", techInstOperComp)).list().getEntities();
        for (Entity operationalTask : operationalTasksList) {
            String comment = techInstOperComp.getStringField(COMMENT) == null ? "" : techInstOperComp.getStringField(COMMENT);
            operationalTask.setField(OperationalTasksFields.DESCRIPTION, comment);
            operationalTasksDD.save(operationalTask);
        }
    }
}
