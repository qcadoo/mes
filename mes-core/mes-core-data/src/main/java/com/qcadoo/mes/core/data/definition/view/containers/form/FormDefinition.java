package com.qcadoo.mes.core.data.definition.view.containers.form;

import java.util.Map;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.view.ContainerDefinition;
import com.qcadoo.mes.core.data.definition.view.ViewEntity;

public final class FormDefinition extends ContainerDefinition<Long> {

    public FormDefinition(final String name, final ContainerDefinition<?> parentContainer, final String fieldPath,
            final String sourceFieldPath) {
        super(name, parentContainer, fieldPath, sourceFieldPath);
    }

    @Override
    public Long getContainerValue(final Entity entity, final Map<String, Entity> selectableEntities,
            final ViewEntity<Object> globalViewEntity, final ViewEntity<Long> viewEntity) {
        return null;
    }

    @Override
    public String getType() {
        return "form";
    }

}
