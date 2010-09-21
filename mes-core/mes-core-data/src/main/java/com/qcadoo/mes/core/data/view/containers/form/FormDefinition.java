package com.qcadoo.mes.core.data.view.containers.form;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.internal.view.ContainerDefinitionImpl;
import com.qcadoo.mes.core.data.view.ContainerComponent;
import com.qcadoo.mes.core.data.view.ViewEntity;

public final class FormDefinition extends ContainerDefinitionImpl<Long> {

    public FormDefinition(final String name, final ContainerComponent<?> parentContainer, final String fieldPath,
            final String sourceFieldPath) {
        super(name, parentContainer, fieldPath, sourceFieldPath);
    }

    @Override
    public String getType() {
        return "form";
    }

    public Entity getFormEntity(final ViewEntity<Object> viewEntity, final String path) {
        // TODO Auto-generated method stub
        return null;
    }

    public Object addValidationResults(final ViewEntity<Object> viewEntity, final String path, final Entity results) {
        // TODO Auto-generated method stub
        return null;
    }

}
