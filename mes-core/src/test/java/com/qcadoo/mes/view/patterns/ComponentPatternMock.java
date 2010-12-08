package com.qcadoo.mes.view.patterns;

import com.qcadoo.mes.view.ComponentDefinition;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.states.ComponentStateMock;

public class ComponentPatternMock extends AbstractComponentPattern {

    private final ComponentState componentState;

    public ComponentPatternMock(final ComponentDefinition componentDefinition) {
        super(componentDefinition);
        componentState = new ComponentStateMock();
    }

    public ComponentPatternMock(final ComponentDefinition componentDefinition, final ComponentState componentState) {
        super(componentDefinition);
        this.componentState = componentState;
    }

    @Override
    public ComponentState getComponentStateInstance() {
        return componentState;
    }

    @Override
    public String getJspFilePath() {
        return JSP_PATH;
    }

    @Override
    public String getJsFilePath() {
        return JS_PATH;
    }

    @Override
    public String getJsObjectName() {
        return JS_OBJECT;
    }
}
