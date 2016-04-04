package com.qcadoo.mes.technologies.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.technologies.constants.OperationProductOutComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class OPOCHooks {

    public void onSave(final DataDefinition dataDefinition, final Entity opoc) {
        Boolean set = (Boolean) opoc.getField(OperationProductOutComponentFields.SET);
        Entity toc = opoc.getBelongsToField(OperationProductOutComponentFields.OPERATION_COMPONENT);
        if (set == null || (set && toc.getBelongsToField(TechnologyOperationComponentFields.PARENT) != null)) {

            opoc.setField(OperationProductOutComponentFields.SET, false);
        }
    }

    public void onCreate(final DataDefinition dataDefinition, final Entity opoc) {
        Boolean set = (Boolean) opoc.getField(OperationProductOutComponentFields.SET);
        if (set == null) {
            opoc.setField(OperationProductOutComponentFields.SET, false);
        }
    }
}
