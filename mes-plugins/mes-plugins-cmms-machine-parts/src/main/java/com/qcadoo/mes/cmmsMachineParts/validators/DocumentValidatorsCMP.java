package com.qcadoo.mes.cmmsMachineParts.validators;

import com.qcadoo.mes.cmmsMachineParts.constants.DocumentFieldsCMP;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.springframework.stereotype.Service;

@Service public class DocumentValidatorsCMP {

    public boolean checkSelectedEvents(final DataDefinition documentDD, final Entity document) {
        boolean valid = true;
        if (document.getBelongsToField(DocumentFieldsCMP.MAINTENANCE_EVENT) != null
                && document.getBelongsToField(DocumentFieldsCMP.PLANNED_EVENT) != null) {
            document.addError(documentDD.getField(DocumentFieldsCMP.PLANNED_EVENT), "materialFlowResources.document.error.canSelectOnlyOneEvent");
            valid = false;
        }
        return valid;
    }

}
