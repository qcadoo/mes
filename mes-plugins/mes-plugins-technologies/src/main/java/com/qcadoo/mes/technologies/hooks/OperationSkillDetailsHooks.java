/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
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
package com.qcadoo.mes.technologies.hooks;

import java.util.Objects;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.technologies.constants.OperationSkillFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class OperationSkillDetailsHooks {

    private static final String L_FORM = "form";

    private static final String L_OPERATION_ID = "operationId";

    public void onBeforeRender(final ViewDefinitionState view) {
        FormComponent operationSkillForm = (FormComponent) view.getComponentByReference(L_FORM);
        LookupComponent skillLookup = (LookupComponent) view.getComponentByReference(OperationSkillFields.SKILL);

        Entity operationSkill = operationSkillForm.getEntity();
        Entity operation = operationSkill.getBelongsToField(OperationSkillFields.OPERATION);

        filterSkillLookup(skillLookup, operation);
    }

    private void filterSkillLookup(final LookupComponent skillLookup, final Entity operation) {
        FilterValueHolder filterValueHolder = skillLookup.getFilterValue();

        Long operationId = operation.getId();

        if (Objects.isNull(operationId)) {
            filterValueHolder.remove(L_OPERATION_ID);
        } else {
            filterValueHolder.put(L_OPERATION_ID, operationId);
        }

        skillLookup.setFilterValue(filterValueHolder);
    }

}
