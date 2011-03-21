package com.qcadoo.model.integration;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public class MockHook {

    public void changeName(final DataDefinition dataDefinition, final Entity entity) {
        entity.setField("changeableName", "XXX");
    }

}
