package com.qcadoo.mes.timeNormsForOperations.listeners;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.technologies.constants.TechnologyState;
import com.qcadoo.mes.timeNormsForOperations.NormService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class TechnologyListenersTN {

    @Autowired
    private NormService normService;

    public void checkOperationOutputQuantities(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");

        Entity technology = form.getEntity();

        if (!TechnologyState.DRAFT.getStringValue().equals(technology.getStringField("state"))) {
            return; // validation will take care of this.
        }

        // FIXME mici, why would I need this? Without it operationComponents are null
        technology = technology.getDataDefinition().get(technology.getId());

        List<String> messages = normService.checkOperationOutputQuantities(technology);

        for (String message : messages) {
            form.addMessage(message.toString(), MessageType.INFO, false);
        }
    }

}
