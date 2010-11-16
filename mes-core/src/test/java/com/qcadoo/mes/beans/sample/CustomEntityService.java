/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.beans.sample;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.model.DataDefinition;

public class CustomEntityService {

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

    public boolean isEqualToQwerty(final DataDefinition dataDefinition, final Object object) {
        return String.valueOf(object).equals("qwerty");
    }

    public boolean hasAge18AndNameMrT(final DataDefinition dataDefinition, final Entity entity) {
        if (entity.getField("age").equals(18) && entity.getField("name").equals("Mr T")) {
            return true;
        } else {
            entity.addError(dataDefinition.getField("name"), "xxx");
            return false;
        }
    }

}