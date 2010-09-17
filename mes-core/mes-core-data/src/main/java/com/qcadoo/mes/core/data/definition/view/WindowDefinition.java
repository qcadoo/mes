package com.qcadoo.mes.core.data.definition.view;

import com.qcadoo.mes.core.data.definition.DataDefinition;

public class WindowDefinition extends ContainerDefinition {

    public WindowDefinition(final String name, final DataDefinition dataDefinition) {
        super(name, null, null, null, dataDefinition);
    }

    @Override
    public String getType() {
        return "window";
    }

}
