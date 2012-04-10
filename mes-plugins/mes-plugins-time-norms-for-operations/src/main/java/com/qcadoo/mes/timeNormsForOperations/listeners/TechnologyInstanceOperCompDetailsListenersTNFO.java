package com.qcadoo.mes.timeNormsForOperations.listeners;

import static com.qcadoo.mes.technologies.constants.TechnologyInstanceOperCompFields.TECHNOLOGY_OPERATION_COMPONENT;
import static com.qcadoo.mes.timeNormsForOperations.constants.TimeNormsConstants.FIELDS_TECHNOLOGY;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class TechnologyInstanceOperCompDetailsListenersTNFO {

    @Autowired
    private TechnologyOperCompDetailsListenersTNFO technologyOperCompDetailsListenersTNFO;

    public void copyTimeNormsFromTechnology(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        Entity technologyInstanceOperationComponent = ((FormComponent) view.getComponentByReference("form")).getEntity();

        // be sure that entity isn't in detached state
        technologyInstanceOperationComponent = technologyInstanceOperationComponent.getDataDefinition().get(
                technologyInstanceOperationComponent.getId());

        technologyOperCompDetailsListenersTNFO.applyTimeNormsFromGivenSource(view,
                technologyInstanceOperationComponent.getBelongsToField(TECHNOLOGY_OPERATION_COMPONENT), FIELDS_TECHNOLOGY);
    }
}
