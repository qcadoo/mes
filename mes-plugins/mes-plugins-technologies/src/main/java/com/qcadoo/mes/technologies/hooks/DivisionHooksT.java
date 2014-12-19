package com.qcadoo.mes.technologies.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class DivisionHooksT {

    public boolean onDelete(final DataDefinition dataDefinition, final Entity division) {
        if (division.getHasManyField("operations").isEmpty()
                && division.getHasManyField("technologyOperationComponents").isEmpty()) {
            return true;
        }
        division.addGlobalError("basic.division.onDelete.error");
        return false;
    }

}
