/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.4
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
package com.qcadoo.mes.productionTimeNorms.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;

@Service
public class TechnologyModelValidators {

    public boolean checkIfAllOperationComponenthHaveTJSet(final DataDefinition dataDefinition, final Entity technology) {
        if (!"02accepted".equals(technology.getStringField("state"))) {
            return true;
        }
        MultiFieldErrorHandler errors = new MultiFieldErrorHandler();
        Entity savedTechnology = dataDefinition.get(technology.getId());
        EntityTree operationComponents = savedTechnology.getTreeField("operationComponents");

        for (Entity operationComponent : operationComponents) {
            if ("operation".equals(operationComponent.getField("entityType")) && !checkIfTJSet(operationComponent)) {
                StringBuilder fieldName = new StringBuilder();
                fieldName.append(operationComponent.getStringField("nodeNumber")).append(" ");
                fieldName.append(operationComponent.getBelongsToField("operation").getStringField("number")).append(" ");
                fieldName.append(operationComponent.getBelongsToField("operation").getStringField("name"));
                errors.addToErrorMessage(fieldName.toString());
            }
        }

        return errors.getMessages("technologies.technology.validate.global.error.noTJSpecified", technology);
    }

    private boolean checkIfTJSet(final Entity operationComponent) {
        if (operationComponent.getField("tj") == null) {
            return false;
        }

        return true;
    }

    private class MultiFieldErrorHandler {

        private boolean hasError = false;

        private final StringBuilder errorString = new StringBuilder();

        public void addToErrorMessage(final String field) {
            errorString.append(" ").append(field).append(";");
            hasError = true;
        }

        public boolean getMessages(final String error, final Entity entity) {
            if (hasError) {
                entity.addGlobalError(error, errorString.toString());
                return false;
            }
            return true;
        }
    }
}
