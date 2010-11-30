package com.qcadoo.mes.newview.components;

import java.util.Map;

import com.qcadoo.mes.newview.AbstractComponentPattern;
import com.qcadoo.mes.newview.AbstractContainerPattern;
import com.qcadoo.mes.newview.ComponentPattern;
import com.qcadoo.mes.newview.ComponentState;

public class ComponentPatternMock extends AbstractContainerPattern {

    public ComponentPatternMock(String name, String fieldPath, String sourceFieldPath, AbstractComponentPattern parent) {
        super(name, fieldPath, sourceFieldPath, parent);
    }

    @Override
    public ComponentState getComponentStateInstance() {
        return new ComponentStateMock(null);
    }

    public Map<String, ComponentPattern> getFieldEntityIdChangeListeners() {
        return getFieldEntityIdChangeListenersMap();
    }

    @Override
    public String getJspPath() {
        return JSP_PATH;
    }

    @Override
    public String getJavaScriptFilePath() {
        return JS_PATH;
    }
}
