package com.qcadoo.mes.core.data.definition.view;

import com.qcadoo.mes.core.data.definition.DataDefinition;

public class WindowDefinition extends RootContainerDefinition {

    public WindowDefinition(final String name, final DataDefinition dataDefinition) {
        super(name, dataDefinition);
    }

    @Override
    public String getType() {
        return "window";
    }

}
