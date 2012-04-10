package com.qcadoo.mes.timeNormsForOperations.hooks;

import static com.qcadoo.mes.timeNormsForOperations.constants.TimeNormsConstants.FIELDS_TECHNOLOGY;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class TechnologyInstanceOperCompHooksTNFO {

    public void copyTimeNormsFromTechnology(final DataDefinition dataDefinition, final Entity entity) {
        Entity technologyOperationComponent = entity.getBelongsToField("technologyOperationComponent");
        for (String reference : FIELDS_TECHNOLOGY) {
            entity.setField(reference, technologyOperationComponent.getField(reference));
        }
    }

}
