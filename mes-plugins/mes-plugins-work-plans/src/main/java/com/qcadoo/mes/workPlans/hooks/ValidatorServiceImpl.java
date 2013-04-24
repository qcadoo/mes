/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.workPlans.constants.WorkPlansConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;

@Service
public class ValidatorServiceImpl implements ValidatorService {

    public final boolean checkAttachmentExtension(final DataDefinition dataDefinition, final FieldDefinition attachmentFieldDef,
            final Entity entity, final Object oldValue, final Object newValue) {
        if (StringUtils.equals((String) oldValue, (String) newValue) || checkAttachmentExtension((String) newValue)) {
            return true;
        }
        entity.addError(attachmentFieldDef, "workPlans.imageUrlInWorkPlan.message.attachmentExtensionIsNotValid");
        return false;
    }

    private boolean checkAttachmentExtension(final String attachementPathValue) {
        if (StringUtils.isBlank(attachementPathValue)) {
            return true;
        }

        // TODO DEV_TEAM after upgrade Apache's commons-lang this loop
        // may be replaced with StringUtils.endsWithAny(attachementPathValue.toLowerCase(), WorkPlansConstants.FILE_EXTENSIONS)
        for (String allowedFileExtension : WorkPlansConstants.FILE_EXTENSIONS) {
            if (StringUtils.endsWithIgnoreCase(attachementPathValue, '.' + allowedFileExtension)) {
                return true;
            }
        }
        return false;
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
                    for (Entity modelComponent : model.getHasManyField(componentName)) {
                        Entity columnUsed = modelComponent.getBelongsToField(columnName);

                        if (columnUsed.getId().equals(column.getId())) {
                            component.addError(componentDD.getField(columnName), "workPlans.column.message.columnIsAlreadyUsed");

                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

}
