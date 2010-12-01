package com.qcadoo.mes.view.patterns;

import java.util.Map;

import com.qcadoo.mes.view.ComponentPattern;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.states.components.ComponentStateMock;

public class ComponentPatternMock extends AbstractContainerPattern {

    public ComponentPatternMock(final String name, final String fieldPath, final String sourceFieldPath,
            final AbstractComponentPattern parent) {
        super(name, fieldPath, sourceFieldPath, parent);
    }

    @Override
    public ComponentState getComponentStateInstance() {
        return new ComponentStateMock(null);
    }

    @Override
    public Map<String, ComponentPattern> getFieldEntityIdChangeListeners() {
        return super.getFieldEntityIdChangeListeners();
    }

    @Override
    public Map<String, ComponentPattern> getScopeEntityIdChangeListeners() {
        return super.getScopeEntityIdChangeListeners();
    }

    @Override
    public String getJspFilePath() {
        return JSP_PATH;
    }

    @Override
    public String getJavaScriptFilePath() {
        return JS_PATH;
    }

    @Override
    public String getJavaScriptObjectName() {
        return JS_OBJECT;
    }
}
