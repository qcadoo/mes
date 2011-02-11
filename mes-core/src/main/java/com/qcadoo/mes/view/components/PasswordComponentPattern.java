package com.qcadoo.mes.view.components;

import com.qcadoo.mes.view.ComponentDefinition;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ViewComponent;

@ViewComponent("password")
public final class PasswordComponentPattern extends FieldComponentPattern {

    private static final String JSP_PATH = "elements/password.jsp";

    private static final String JS_OBJECT = "QCD.components.elements.PasswordInput";

    public PasswordComponentPattern(final ComponentDefinition componentDefinition) {
        super(componentDefinition);
    }

    @Override
    public ComponentState getComponentStateInstance() {
        return new PasswordComponentState(this);
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
