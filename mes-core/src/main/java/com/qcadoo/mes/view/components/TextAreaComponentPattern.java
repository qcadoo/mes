package com.qcadoo.mes.view.components;

import com.qcadoo.mes.view.ComponentPattern;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ViewComponent;
import com.qcadoo.mes.view.patterns.AbstractComponentPattern;

@ViewComponent("textarea")
public class TextAreaComponentPattern extends AbstractComponentPattern {

    public TextAreaComponentPattern(final String name, final String fieldPath, final String sourceFieldPath,
            final ComponentPattern parent) {
        super(name, fieldPath, sourceFieldPath, parent);
    }

    @Override
    public ComponentState getComponentStateInstance() {
        return new TextInputComponentState();
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
