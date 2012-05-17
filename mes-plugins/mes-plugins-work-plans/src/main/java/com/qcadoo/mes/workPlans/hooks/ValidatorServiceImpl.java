/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.6
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
package com.qcadoo.mes.workPlans.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.workPlans.constants.WorkPlansConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;

@Service
public class ValidatorServiceImpl implements ValidatorService {

    public boolean checkIfAttachmentExtensionIsValid(final DataDefinition modelDD, final Entity model, final String attachmentName) {

        String attachment = model.getStringField(attachmentName);

        if (attachment != null) {
            String attachemntExtension = attachment.substring((attachment.lastIndexOf('.') + 1), attachment.length());

            boolean contains = false;

            for (String fileExtension : WorkPlansConstants.FILE_EXTENSIONS) {
                if (fileExtension.equals(attachemntExtension)) {
                    contains = true;
                }
            }

            if (!contains) {
                model.addError(modelDD.getField(attachmentName),
                        "workPlans.imageUrlInWorkPlan.message.attachmentExtensionIsNotValid");

                return false;
            }
        }

        return true;
    }

    public boolean checkIfColumnIsNotUsed(final DataDefinition componentDD, final Entity component, final String modelName,
            final String columnName, final String componentName) {

        if (component.getId() == null) {
            Entity column = component.getBelongsToField(columnName);

            if (column == null) {
                return true;
            } else {
                Entity model = component.getBelongsToField(modelName);

                if (model == null) {
                    return true;
                } else {
                    EntityList modelComponents = model.getHasManyField(componentName);

                    if (modelComponents == null) {
                        return true;
                    } else {
                        for (Entity modelComponent : modelComponents) {
                            Entity columnUsed = modelComponent.getBelongsToField(columnName);

                            if (columnUsed.getId().equals(column.getId())) {
                                component.addError(componentDD.getField(columnName),
                                        "workPlans.column.message.columnIsAlreadyUsed");

                                return false;
                            }
                        }
                    }
                }
            }
        }

        return true;
    }

}
