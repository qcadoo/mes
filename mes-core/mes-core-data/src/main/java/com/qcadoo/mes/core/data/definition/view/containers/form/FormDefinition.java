package com.qcadoo.mes.core.data.definition.view.containers.form;

import com.qcadoo.mes.core.data.definition.view.ComponentDefinition;
import com.qcadoo.mes.core.data.definition.view.ContainerDefinition;

public final class FormDefinition extends ContainerDefinition {

    public FormDefinition(final String name, final String dataSource) {
        super(name, dataSource);
    }

    @Override
    public int getType() {
        return ComponentDefinition.TYPE_CONTAINER_FORM;
    }

}
