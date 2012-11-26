/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
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
package com.qcadoo.mes.operationalTasksForOrders.hooks;

import static com.qcadoo.mes.operationalTasks.constants.OperationalTasksFields.DESCRIPTION;
import static com.qcadoo.mes.operationalTasksForOrders.constants.OperationalTasksOTFOFields.TECHNOLOGY_INSTANCE_OPERATION_COMPONENT;
import static com.qcadoo.mes.technologies.constants.TechnologyInstanceOperCompFields.COMMENT;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.operationalTasks.constants.OperationalTasksConstants;
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
                .add(SearchRestrictions.belongsTo(TECHNOLOGY_INSTANCE_OPERATION_COMPONENT, techInstOperComp)).list()
                .getEntities();
        for (Entity operationalTask : operationalTasksList) {
            String comment = techInstOperComp.getStringField(COMMENT) == null ? "" : techInstOperComp.getStringField(COMMENT);
            operationalTask.setField(DESCRIPTION, comment);
            operationalTasksDD.save(operationalTask);
        }
    }
}
