package com.qcadoo.model;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.FieldDefinition;

public class CustomHook {

    public void hook(final DataDefinition dataDefinition, final Entity entity) {

    }

    public void updateHook(final DataDefinition dataDefinition, final Entity entity) {

    }

    public void createHook(final DataDefinition dataDefinition, final Entity entity) {

    }

    public void copyHook(final DataDefinition dataDefinition, final Entity entity) {

    }

    public static void staticHook(final DataDefinition dataDefinition, final Entity entity) {

    }

    public void validate(final DataDefinition dataDefinition, final Entity entity) {

    }

    public void validateField(final DataDefinition dataDefinition, final FieldDefinition fieldDefinition, final Entity entity,
            final Object oldValue, final Object newValue) {

    }

    public static void staticValidateField(final DataDefinition dataDefinition, final FieldDefinition fieldDefinition,
            final Entity entity, final Object oldValue, final Object newValue) {

    }

}
