/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
package com.qcadoo.mes.lineChangeoverNorms.listeners;

import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.CHANGEOVER_TYPE;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.DURATION;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.NUMBER;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.lineChangeoverNorms.ChangeoverNormsService;
import com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsConstants;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;

@Service
public class MatchingChangeoverNormsDetailsListeners {

    @Autowired
    private ChangeoverNormsService changeoverNormsService;

    private static final String MATCHING_FROM_TECHNOLOGY = "matchingFromTechnology";

    private static final String MATCHING_TO_TECHNOLOGY = "matchingToTechnology";

    private static final String MATCHING_PRODUCTION_LINE = "matchingProductionLine";

    public void matchingChangeoverNorm(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {

        Entity fromTechnology = ((LookupComponent) viewDefinitionState.getComponentByReference(MATCHING_FROM_TECHNOLOGY))
                .getEntity();

        Entity toTechnology = ((LookupComponent) viewDefinitionState.getComponentByReference(MATCHING_TO_TECHNOLOGY)).getEntity();
        Entity productionLine = ((LookupComponent) viewDefinitionState.getComponentByReference(MATCHING_PRODUCTION_LINE))
                .getEntity();
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");
        Entity changeoverNorm = changeoverNormsService.getMatchingChangeoverNorms(fromTechnology, toTechnology, productionLine);
        if (changeoverNorm == null) {
            clearField(viewDefinitionState);
            changeStateEditButton(viewDefinitionState, false);
            form.setFieldValue(null);
        } else {
            form.setFieldValue(changeoverNorm.getId());
            fillField(viewDefinitionState, changeoverNorm);
            changeStateEditButton(viewDefinitionState, true);
        }
    }

    public void redirectToChangedNormsDetails(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        Long changeoverNormId = (Long) state.getFieldValue();
        if (changeoverNormId != null) {
            String url = "../page/lineChangeoverNorms/lineChangeoverNormsDetails.html?context={\"form.id\":\"" + changeoverNormId
                    + "\"}";
            viewDefinitionState.redirectTo(url, false, true);
        }
    }

    public void enabledButtonAfterSelectionTechnologies(final ViewDefinitionState viewDefinitionState,
            final ComponentState state, final String[] args) {
        WindowComponent window = (WindowComponent) viewDefinitionState.getComponentByReference("window");
        RibbonActionItem matchingChangeoverNorm = window.getRibbon().getGroupByName("matching")
                .getItemByName("matchingChangeoverNorm");
        Entity fromTechnology = ((LookupComponent) viewDefinitionState.getComponentByReference(MATCHING_FROM_TECHNOLOGY))
                .getEntity();
        Entity toTechnology = ((LookupComponent) viewDefinitionState.getComponentByReference(MATCHING_TO_TECHNOLOGY)).getEntity();
        if ((fromTechnology == null) || (toTechnology == null)) {
            matchingChangeoverNorm.setEnabled(false);
        } else {
            matchingChangeoverNorm.setEnabled(true);
        }
        matchingChangeoverNorm.requestUpdate(true);
    }

    public void changeStateEditButton(final ViewDefinitionState view, final boolean enabled) {
        WindowComponent window = (WindowComponent) view.getComponentByReference("window");
        RibbonActionItem edit = window.getRibbon().getGroupByName("matching").getItemByName("edit");
        edit.setEnabled(enabled);
        edit.requestUpdate(true);
    }

    public void clearField(final ViewDefinitionState view) {
        for (String reference : Arrays.asList(NUMBER, CHANGEOVER_TYPE, DURATION)) {
            FieldComponent field = (FieldComponent) view.getComponentByReference(reference);
            field.setFieldValue(null);
            field.requestComponentUpdateState();
        }
        for (String reference : LineChangeoverNormsConstants.FIELDS_ENTITY) {
            FieldComponent field = (FieldComponent) view.getComponentByReference(reference);
            field.setFieldValue(null);
            field.requestComponentUpdateState();
        }
    }

    public void fillField(final ViewDefinitionState view, final Entity entity) {
        for (String reference : Arrays.asList(NUMBER, CHANGEOVER_TYPE, DURATION)) {
            FieldComponent field = (FieldComponent) view.getComponentByReference(reference);
            field.setFieldValue(entity.getField(reference));
            field.requestComponentUpdateState();
        }
        for (String reference : LineChangeoverNormsConstants.FIELDS_ENTITY) {
            FieldComponent field = (FieldComponent) view.getComponentByReference(reference);
            field.setFieldValue(entity.getBelongsToField(reference) == null ? null : entity.getBelongsToField(reference).getId());
            field.requestComponentUpdateState();
        }
    }

}
