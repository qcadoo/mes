package com.qcadoo.mes.cmmsMachineParts.hooks;

import com.qcadoo.mes.cmmsMachineParts.constants.SourceCostFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.springframework.stereotype.Service;

@Service public class SourceCodeHooks {

    public void onSave(final DataDefinition sourceCodeDD, final Entity sourceCode) {
        if (sourceCode.isActive()) {
            return;
        } else {
            sourceCode.setField(SourceCostFields.DEFAULT_COST, false);
        }
    }
}
