package com.qcadoo.mes.view.patterns;

import com.qcadoo.mes.view.ComponentDefinition;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ContainerState;
import com.qcadoo.mes.view.states.ContainerStateMock;

public class ContainerPatternMock extends AbstractContainerPattern {

    private final ContainerState containerState;

    public ContainerPatternMock(final ComponentDefinition componentDefinition) {
        super(componentDefinition);
        this.containerState = new ContainerStateMock();
    }

    public ContainerPatternMock(final ComponentDefinition componentDefinition, final ContainerState containerState) {
        super(componentDefinition);
        this.containerState = containerState;
    }

    @Override
    public ComponentState getComponentStateInstance() {
        return containerState;
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
