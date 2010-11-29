package com.qcadoo.mes.newview.components;

import java.util.Map;

import com.qcadoo.mes.newview.AbstractComponentPattern;
import com.qcadoo.mes.newview.AbstractContainerPattern;
import com.qcadoo.mes.newview.ComponentPattern;
import com.qcadoo.mes.newview.ComponentState;

public class TestComponentPattern extends AbstractContainerPattern {

    public TestComponentPattern(String name, String fieldPath, String sourceFieldPath, AbstractComponentPattern parent) {
        super(name, fieldPath, sourceFieldPath, parent);
    }

    @Override
    public ComponentState getComponentStateInstance() {
        return new TestComponentState();
    }

    public Map<String, ComponentPattern> getFieldEntityIdChangeListeners() {
        return getFieldEntityIdChangeListenersMap();
    }
}
