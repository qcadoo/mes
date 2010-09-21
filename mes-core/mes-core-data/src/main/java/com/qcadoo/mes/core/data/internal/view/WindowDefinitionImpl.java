package com.qcadoo.mes.core.data.internal.view;

import com.qcadoo.mes.core.data.model.DataDefinition;

public class WindowDefinitionImpl extends RootContainerDefinitionImpl {

    public WindowDefinitionImpl(final String name, final DataDefinition dataDefinition) {
        super(name, dataDefinition);
    }

    @Override
    public String getType() {
        return "window";
    }

}
