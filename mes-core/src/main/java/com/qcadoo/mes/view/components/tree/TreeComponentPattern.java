package com.qcadoo.mes.view.components.tree;

import com.qcadoo.mes.view.ComponentDefinition;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ViewComponent;
import com.qcadoo.mes.view.patterns.AbstractComponentPattern;

@ViewComponent("tree")
public final class TreeComponentPattern extends AbstractComponentPattern {

    private static final String JSP_PATH = "elements/input.jsp";

    private static final String JS_OBJECT = "QCD.components.elements.TextInput";

    public TreeComponentPattern(final ComponentDefinition componentDefinition) {
        super(componentDefinition);
    }

    @Override
    public ComponentState getComponentStateInstance() {
        return new TreeComponentState();
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
