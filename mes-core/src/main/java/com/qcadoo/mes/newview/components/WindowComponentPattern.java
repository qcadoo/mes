package com.qcadoo.mes.newview.components;

import com.qcadoo.mes.newview.AbstractContainerPattern;
import com.qcadoo.mes.newview.ComponentPattern;
import com.qcadoo.mes.newview.ComponentState;
import com.qcadoo.mes.view.menu.ribbon.Ribbon;

public class WindowComponentPattern extends AbstractContainerPattern {

    private Ribbon ribbon;

    public WindowComponentPattern(final String name, final String fieldPath, final String sourceFieldPath,
            final ComponentPattern parent) {
        super(name, fieldPath, sourceFieldPath, parent);
    }

    @Override
    public ComponentState getComponentStateInstance() {
        return new FormComponentState();
    }

    public void setRibbon(final Ribbon ribbon) {
        this.ribbon = ribbon;
    }

}
