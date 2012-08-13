/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.7
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
package com.qcadoo.mes.lineChangeoverNorms.hooks;

import static com.qcadoo.mes.lineChangeoverNorms.constants.ChangeoverType.FOR_TECHNOLOGY;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.CHANGEOVER_TYPE;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.FROM_TECHNOLOGY;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.FROM_TECHNOLOGY_GROUP;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.TO_TECHNOLOGY;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.TO_TECHNOLOGY_GROUP;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class LineChangeoverNormsDetailsHooks {

    private static final List<String> TECHNOLOGY_FIELDS = Arrays.asList(FROM_TECHNOLOGY, TO_TECHNOLOGY);

    private static final List<String> TECHNOLOGY_GROUP_FIELDS = Arrays.asList(FROM_TECHNOLOGY_GROUP, TO_TECHNOLOGY_GROUP);

    public void setFieldsVisibleAndRequired(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        setFieldsVisibleAndRequired(view);
    }

    public void setFieldsVisibleAndRequired(final ViewDefinitionState view) {
        FieldComponent changeoverType = (FieldComponent) view.getComponentByReference(CHANGEOVER_TYPE);

        boolean selectForTechnology = FOR_TECHNOLOGY.getStringValue().equals(changeoverType.getFieldValue());

        changeFieldsState(view, TECHNOLOGY_FIELDS, selectForTechnology);
        changeFieldsState(view, TECHNOLOGY_GROUP_FIELDS, !selectForTechnology);
    }

    private void changeFieldsState(final ViewDefinitionState view, final List<String> fieldNames,
            final boolean selectForTechnology) {
        for (String fieldName : fieldNames) {
            FieldComponent field = (FieldComponent) view.getComponentByReference(fieldName);
            field.setVisible(selectForTechnology);
            field.setRequired(selectForTechnology);

            if (!selectForTechnology) {
                field.setFieldValue(null);
            }

            field.requestComponentUpdateState();
        }
    }

}
