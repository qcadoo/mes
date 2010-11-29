package com.qcadoo.mes.newview.components;

import com.qcadoo.mes.newview.AbstractComponentPattern;
import com.qcadoo.mes.newview.ComponentState;

public class TextInputComponentPattern extends AbstractComponentPattern {

    public TextInputComponentPattern(final String name, final String fieldPath, final String sourceFieldPath,
            final AbstractComponentPattern parent) {
        super(name, fieldPath, sourceFieldPath, parent);
    }

    public ComponentState getComponentStateInstance() {
        return new TextInputComponentState();
    }
}
