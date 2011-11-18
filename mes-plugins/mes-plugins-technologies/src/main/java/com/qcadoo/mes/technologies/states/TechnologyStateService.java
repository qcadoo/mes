package com.qcadoo.mes.technologies.states;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyState;
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
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TechnologyStateBeforeChangeNotifierService beforeChangeNotifier;

    public void changeTechnologyState(final ViewDefinitionState view, final ComponentState component, final String[] args) {
        final String targetState = getTargetStateFromArgs(args);
        final FormComponent form = (FormComponent) view.getComponentByReference("form");
        final Entity technology = form.getEntity();

        setTechnologyState(view, component, technology, targetState);
    }

    public void changeSelectedTechnologyState(final ViewDefinitionState view, final ComponentState component, final String[] args) {
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
        final ComponentState stateFieldComponent = view.getComponentByReference("state");

        TechnologyState oldState = TechnologyState.valueOf(technology.getStringField("state").toUpperCase());
        TechnologyState newState = oldState.changeState(targetState);

        if (oldState == newState || !beforeChangeNotifier.fireListeners(component, technology, newState)) {
            return;
        }

        if (!sourceComponentIsForm) {
            technology.setField("state", newState.getStringValue());
            Entity savedTechnology = technologyDataDefinition.save(technology);
            
            List<ErrorMessage> errorMessages = Lists.newArrayList();
            errorMessages.addAll(savedTechnology.getErrors().values());
            errorMessages.addAll(savedTechnology.getGlobalErrors());
            
            for (ErrorMessage message : errorMessages) {
                view.getComponentByReference("grid").addMessage(message.getMessage(), MessageType.INFO);
            }
            return;
        }

        stateFieldComponent.setFieldValue(newState.getStringValue());
        component.performEvent(view, "save", new String[0]);
        Entity savedTechnology = technologyDataDefinition.get(technology.getId());
        stateFieldComponent.setFieldValue(savedTechnology.getStringField("state"));

    }

    private DataDefinition getTechnologyDataDefinition() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY);
    }

    private List<Entity> getTechnologiesFromGridComponent(final GridComponent grid) {
        Set<Long> ids = grid.getSelectedEntitiesIds();
        List<Entity> technologies = Lists.newArrayList();
        if (ids.size() > 0) {
            technologies.addAll(getTechnologyDataDefinition().find().add(SearchRestrictions.in("id", ids)).list().getEntities());
        }
        return technologies;
    }

    private String getTargetStateFromArgs(final String[] args) {
        return args != null && args.length > 0 && !"null".equals(args[0]) ? args[0] : "";
    }

}
