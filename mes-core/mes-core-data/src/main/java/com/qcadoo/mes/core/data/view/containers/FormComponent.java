package com.qcadoo.mes.core.data.view.containers;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.view.AbstractContainerComponent;
import com.qcadoo.mes.core.data.view.ContainerComponent;
import com.qcadoo.mes.core.data.view.ViewValue;

public final class FormComponent extends AbstractContainerComponent {

    public FormComponent(final String name, final ContainerComponent parentContainer, final String fieldPath,
            final String sourceFieldPath) {
        super(name, parentContainer, fieldPath, sourceFieldPath);
    }

    @Override
    public String getType() {
        return "form";
    }

    public Entity getFormEntity(final ViewValue<Object> viewEntity, final String path) {
        // TODO Auto-generated method stub
        return null;
    }

    public Object addValidationResults(final ViewValue<Object> viewEntity, final String path, final Entity results) {
        // TODO Auto-generated method stub
        return null;
    }

}
