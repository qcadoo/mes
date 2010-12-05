package com.qcadoo.mes.view.components;

import com.qcadoo.mes.view.ComponentDefinition;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ViewComponent;

@ViewComponent("checkbox")
public final class CheckBoxComponentPattern extends FieldComponentPattern {

    private static final String JS_OBJECT = "QCD.components.elements.TextInput";

    private static final String JSP_PATH = "newComponents/input.jsp";

    public CheckBoxComponentPattern(final ComponentDefinition componentDefinition) {
        super(componentDefinition);
    }

    @Override
    public ComponentState getComponentStateInstance() {
        return new FieldComponentState();
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
