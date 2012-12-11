package com.qcadoo.mes.materialFlow.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class LocationModelHooks {

    public boolean clearExternalIdOnCopy(final DataDefinition dataDefinition, final Entity entity) {
        if (entity == null) {
            return true;
        }

        entity.setField("externalNumber", null);

        return true;
    }

}
