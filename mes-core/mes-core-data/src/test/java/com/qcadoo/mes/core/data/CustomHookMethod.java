package com.qcadoo.mes.core.data;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.model.DataDefinition;

public class CustomHookMethod {

    public void onUpdate(final DataDefinition dataDefinition, final Entity entity) {
        entity.setField("name", "update");
    }

    public void onSave(final DataDefinition dataDefinition, final Entity entity) {
        entity.setField("age", 11);
    }

    public void onCreate(final DataDefinition dataDefinition, final Entity entity) {
        entity.setField("name", "create");
    }

    public void onDelete(final DataDefinition dataDefinition, final Entity entity) {
        entity.setField("name", "delete");
    }

}