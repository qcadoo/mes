/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.3
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
package com.qcadoo.mes.technologies.states;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyState;
import com.qcadoo.mes.technologies.logging.TechnologyLoggingService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.validators.ErrorMessage;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class TechnologyStateService {

    @Autowired
    private TechnologyLoggingService technologyLoggingService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TechnologyStateBeforeChangeNotifierService beforeChangeNotifier;

    private static final String STATE_FIELD = "state";

    public final void changeTechnologyState(final ViewDefinitionState view, final ComponentState component, final String[] args) {
        final String targetState = getTargetStateFromArgs(args);
        final FormComponent form = (FormComponent) view.getComponentByReference("form");
        final Entity technology = form.getEntity();

        setTechnologyState(view, component, technology, targetState);
    }

    public final void changeSelectedTechnologyState(final ViewDefinitionState view, final ComponentState component,
            final String[] args) {
        final String targetState = getTargetStateFromArgs(args);
        final GridComponent grid = (GridComponent) component;
        final List<Entity> technologies = getTechnologiesFromGridComponent(grid);

        if (technologies.isEmpty()) {
            return;
        }
        for (Entity technology : technologies) {
            setTechnologyState(view, component, technology, targetState);
        }
    }

    @Transactional
    private void setTechnologyState(final ViewDefinitionState view, final ComponentState component, final Entity technology,
            final String targetState) {
        if (technology == null) {
            return;
        }

        final boolean sourceComponentIsForm = component instanceof FormComponent;
        final DataDefinition technologyDataDefinition = technology.getDataDefinition();
        final ComponentState stateFieldComponent = view.getComponentByReference(STATE_FIELD);

        TechnologyState oldState = TechnologyStateUtils.getStateFromField(technology.getStringField(STATE_FIELD));
        TechnologyState newState = oldState.changeState(targetState);

        if (newState.equals(oldState) || !beforeChangeNotifier.fireListeners(component, technology, newState)) {
            return;
        }

        if (!sourceComponentIsForm) {
            technology.setField(STATE_FIELD, newState.getStringValue());
            Entity savedTechnology = technologyDataDefinition.save(technology);

            List<ErrorMessage> errorMessages = Lists.newArrayList();
            errorMessages.addAll(savedTechnology.getErrors().values());
            errorMessages.addAll(savedTechnology.getGlobalErrors());

            for (ErrorMessage message : errorMessages) {
                view.getComponentByReference("grid").addMessage(message.getMessage(), MessageType.FAILURE, message.getVars());
            }
            return;
        }

        if (newState.equals(TechnologyState.DECLINED) || newState.equals(TechnologyState.OUTDATED)) {
            view.getComponentByReference("master").setFieldValue("0");
        }

        stateFieldComponent.setFieldValue(newState.getStringValue());
        component.performEvent(view, "save", new String[0]);
        Entity savedTechnology = technologyDataDefinition.get(technology.getId());
        stateFieldComponent.setFieldValue(savedTechnology.getStringField(STATE_FIELD));
        technologyLoggingService.logStateChange(technology, oldState, newState);
    }

    private DataDefinition getTechnologyDataDefinition() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY);
    }

    private List<Entity> getTechnologiesFromGridComponent(final GridComponent grid) {
        Set<Long> ids = grid.getSelectedEntitiesIds();
        List<Entity> technologies = Lists.newArrayList();
        if (!ids.isEmpty()) {
            technologies.addAll(getTechnologyDataDefinition().find().add(SearchRestrictions.in("id", ids)).list().getEntities());
        }
        return technologies;
    }

    private String getTargetStateFromArgs(final String[] args) {
        if (args != null && args.length > 0 && !"null".equals(args[0])) {
            return args[0];
        }
        return "";
    }

}
