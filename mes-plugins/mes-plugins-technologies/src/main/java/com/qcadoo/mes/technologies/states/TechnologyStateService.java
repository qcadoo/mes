package com.qcadoo.mes.technologies.states;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Sets;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyState;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class TechnologyStateService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    private Set<TechnologyStateService.BeforeChangeStateListener> beforeChangeStateListeners = Sets.newLinkedHashSet();

    public void changeTechnologyState(final ViewDefinitionState view, final ComponentState component, final String[] args) {
        final String targetState = getTargetStateFromArgs(args);
        final FormComponent form = (FormComponent) view.getComponentByReference("form");
        final Entity technology = form.getEntity();

        setTechnologyState(view, component, technology, targetState);
    }

    public void changeSelectedTechnologyState(final ViewDefinitionState view, final ComponentState component, final String[] args) {
        final String targetState = getTargetStateFromArgs(args);
        final GridComponent grid = (GridComponent) component;
        final Entity technology = getTechnologyFromGridComponent(grid);

        setTechnologyState(view, component, technology, targetState);
    }

    @Transactional
    private void setTechnologyState(final ViewDefinitionState view, final ComponentState component, final Entity technology,
            final String targetState) {
        if (technology == null) {
            return;
        }

        final boolean sourceComponentIsForm = component instanceof FormComponent;
        final DataDefinition technologyDataDefinition = technology.getDataDefinition();

        TechnologyState oldState = TechnologyState.valueOf(technology.getStringField("state").toUpperCase());
        TechnologyState newState = oldState.changeState(targetState);

        for (BeforeChangeStateListener listener : beforeChangeStateListeners) {
            if (!listener.canChange(component, technology, newState)) {
                return;
            }
        }

        if (!sourceComponentIsForm) {
            technology.setField("state", newState.getStringValue());
            technologyDataDefinition.save(technology);
            return;
        }

        getStateFieldComponent(view).setFieldValue(newState.getStringValue());
        component.performEvent(view, "save", new String[0]);
        Entity savedTechnology = technologyDataDefinition.get(technology.getId());
        getStateFieldComponent(view).setFieldValue(savedTechnology.getStringField("state"));
        
    }

    private FieldComponent getStateFieldComponent(final ViewDefinitionState view) {
        return (FieldComponent) view.getComponentByReference("state");
    }

    private DataDefinition getTechnologyDataDefinition() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY);
    }

    private Entity getTechnologyFromGridComponent(final GridComponent grid) {
        Set<Long> ids = grid.getSelectedEntitiesIds();
        if (ids.size() > 0) {
            return getTechnologyDataDefinition().get((Long) ids.toArray()[0]);
        }
        return null;
    }

    private String getTargetStateFromArgs(final String[] args) {
        return args != null && args.length > 0 && !"null".equals(args[0]) ? args[0] : "";
    }

    public void registerBeforeChangeStateListener(final BeforeChangeStateListener listener) {
        beforeChangeStateListeners.add(listener);
    }

    public void unregisterBeforeChangeStateListener(final BeforeChangeStateListener listener) {
        beforeChangeStateListeners.remove(listener);
    }

    public static interface BeforeChangeStateListener {

        boolean canChange(final ComponentState gridOrForm, final Entity order, final TechnologyState state);
    }
}
