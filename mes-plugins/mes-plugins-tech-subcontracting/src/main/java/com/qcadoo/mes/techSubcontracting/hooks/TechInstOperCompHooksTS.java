package com.qcadoo.mes.techSubcontracting.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.technologies.constants.TechnologyInstanceOperCompFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class TechInstOperCompHooksTS {

    private static final String L_IS_SUBCONTRACTING = "isSubcontracting";

    public void copySubstractingFieldFromLowerInstance(final DataDefinition dataDefinition, final Entity entity) {
        Entity technologyOperationComponent = entity
                .getBelongsToField(TechnologyInstanceOperCompFields.TECHNOLOGY_OPERATION_COMPONENT);
        entity.setField(L_IS_SUBCONTRACTING, technologyOperationComponent.getBooleanField(L_IS_SUBCONTRACTING));
    }
}
