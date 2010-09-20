package com.qcadoo.mes.core.data.definition.view.containers.form;

import com.qcadoo.mes.core.data.definition.view.ContainerDefinition;

public final class FormDefinition extends ContainerDefinition<Long> {

    public FormDefinition(final String name, final ContainerDefinition<?> parentContainer, final String fieldPath,
            final String sourceFieldPath) {
        super(name, parentContainer, fieldPath, sourceFieldPath);
    }

    @Override
    public String getType() {
        return "form";
    }

}
