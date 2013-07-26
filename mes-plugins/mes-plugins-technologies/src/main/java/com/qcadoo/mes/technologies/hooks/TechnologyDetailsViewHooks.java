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
package com.qcadoo.mes.technologies.hooks;

import static com.qcadoo.mes.technologies.states.constants.TechnologyStateChangeFields.STATUS;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.qcadoo.mes.states.constants.StateChangeStatus;
import com.qcadoo.mes.states.service.client.util.StateChangeHistoryService;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.states.constants.TechnologyState;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.CustomRestriction;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class TechnologyDetailsViewHooks {

    private static final String L_FORM = "form";

    private static final String OUT_PRODUCTS_REFERENCE = "outProducts";

    private static final String IN_PRODUCTS_REFERENCE = "inProducts";

    private static final String TECHNOLOGY_TREE_REFERENCE = "technologyTree";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private StateChangeHistoryService stateChangeHistoryService;

    public void filterStateChangeHistory(final ViewDefinitionState view) {
        final GridComponent historyGrid = (GridComponent) view.getComponentByReference("grid");
        final CustomRestriction onlySuccessfulRestriction = stateChangeHistoryService.buildStatusRestriction(STATUS,
                Lists.newArrayList(StateChangeStatus.SUCCESSFUL.getStringValue()));
        historyGrid.setCustomRestriction(onlySuccessfulRestriction);
    }

    public void setTreeTabEditable(final ViewDefinitionState view) {
        final boolean treeTabShouldBeEnabled = TechnologyState.DRAFT.equals(getTechnologyState(view))
                && technologyIsAlreadySaved(view);
        for (String componentReference : Sets.newHashSet(OUT_PRODUCTS_REFERENCE, IN_PRODUCTS_REFERENCE)) {
            ((GridComponent) view.getComponentByReference(componentReference)).setEditable(treeTabShouldBeEnabled);
        }
        view.getComponentByReference(TECHNOLOGY_TREE_REFERENCE).setEnabled(treeTabShouldBeEnabled);
    }

    private boolean technologyIsAlreadySaved(final ViewDefinitionState view) {
        final FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        return form.getEntityId() != null;
    }

    private TechnologyState getTechnologyState(final ViewDefinitionState view) {
        final FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        final Entity technology = form.getEntity();
        TechnologyState state = TechnologyState.DRAFT;
        if (technology != null) {
            state = TechnologyState.parseString(technology.getStringField(TechnologyFields.STATE));
        }
        return state;
    }

    public void disableFieldTechnologyFormAndEnabledMaster(final ViewDefinitionState view) {
        FormComponent technology = (FormComponent) view.getComponentByReference(L_FORM);
        FieldComponent master = (FieldComponent) view.getComponentByReference(TechnologyFields.MASTER);
        boolean disabled = false;
        boolean masterDisabled = false;
        if (technology.getEntityId() != null) {
            Entity entity = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                    TechnologiesConstants.MODEL_TECHNOLOGY).get(technology.getEntityId());
            if (entity == null) {
                return;
            }
            String state = entity.getStringField(TechnologyFields.STATE);
            if (!TechnologyState.DRAFT.getStringValue().equals(state)) {
                disabled = true;
            }
            if (TechnologyState.ACCEPTED.getStringValue().equals(state)) {
                masterDisabled = true;
            }

        }

        technology.setFormEnabled(!disabled);
        master.setEnabled(masterDisabled);
        master.requestComponentUpdateState();
    }

}
