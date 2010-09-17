package com.qcadoo.mes.core.data.definition.view.containers.form;

import com.qcadoo.mes.core.data.definition.view.ComponentDefinition;
import com.qcadoo.mes.core.data.definition.view.ContainerDefinition;

public final class FormDefinition extends ContainerDefinition {

    public FormDefinition(final String name, final ContainerDefinition parentContainer, final String fieldPath,
            final String sourceFieldPath) {
        super(name, parentContainer, fieldPath, sourceFieldPath, null);
    }

    @Override
    public int getType() {
        return ComponentDefinition.TYPE_CONTAINER_FORM;
    }

}
