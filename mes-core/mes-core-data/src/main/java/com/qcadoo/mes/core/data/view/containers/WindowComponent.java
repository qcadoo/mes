package com.qcadoo.mes.core.data.view.containers;

import com.qcadoo.mes.core.data.model.DataDefinition;
import com.qcadoo.mes.core.data.view.AbstractRootComponent;

public class WindowComponent extends AbstractRootComponent {

    public WindowComponent(final String name, final DataDefinition dataDefinition) {
        super(name, dataDefinition);
    }

    @Override
    public String getType() {
        return "window";
    }

}
