package com.qcadoo.mes.productionCounting.hooks;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.springframework.stereotype.Service;

@Service
public class StorageLocationHooksPC {

    public boolean onDelete(final DataDefinition dataDefinition, final Entity entity) {
        if (!entity.getHasManyField("trackingOperationProductOutComponents").isEmpty()) {
            entity.addGlobalError("qcadooView.errorPage.error.dataIntegrityViolationException.objectInUse.explanation");
            return false;
        }
        return true;
    }
}
