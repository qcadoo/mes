package com.qcadoo.mes.view.components;

import com.qcadoo.mes.view.ComponentDefinition;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ViewComponent;
import com.qcadoo.mes.view.patterns.AbstractComponentPattern;

@ViewComponent("separatorLine")
public class SeparatorLinePattern extends AbstractComponentPattern {

    private static final String JS_OBJECT = "QCD.components.containers.layout.SeperatorLine";

    private static final String JSP_PATH = "elements/separatorLine.jsp";

    private static final String JS_PATH = "/js/crud/qcd/components/elements/separatorLine.js";

    public SeparatorLinePattern(final ComponentDefinition componentDefinition) {
        super(componentDefinition);
    }

    @Override
    protected ComponentState getComponentStateInstance() {
        return new EmptyContainerState();
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
