package com.qcadoo.mes.basic.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.FactoryFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class FactoryModelHooks {

    public boolean onDelete(final DataDefinition dataDefinition, final Entity factory) {
        boolean canDelete = factory.getHasManyField(FactoryFields.DIVISIONS).isEmpty();
        if (!canDelete) {
            factory.addGlobalError("basic.factory.delete.hasDivisions");
        }
        return canDelete;
    }
}
