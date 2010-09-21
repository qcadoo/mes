package com.qcadoo.mes.core.data.internal.view;

import com.qcadoo.mes.core.data.model.ModelDefinition;

public class WindowDefinitionImpl extends RootContainerDefinitionImpl {

    public WindowDefinitionImpl(final String name, final ModelDefinition dataDefinition) {
        super(name, dataDefinition);
    }

    @Override
    public String getType() {
        return "window";
    }

}
