package com.qcadoo.mes.view.components;

import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.view.ComponentDefinition;
import com.qcadoo.mes.view.ComponentOption;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ViewComponent;

@ViewComponent("textarea")
public final class TextAreaComponentPattern extends FieldComponentPattern {

    private static final String JS_OBJECT = "QCD.components.elements.TextInput";

    private static final String JSP_PATH = "newComponents/input.jsp";

    private int rows = 4;

    public TextAreaComponentPattern(final ComponentDefinition componentDefinition) {
        super(componentDefinition);
    }

    @Override
    protected void initializeComponent() throws JSONException {
        super.initializeComponent();
        for (ComponentOption option : getOptions()) {
            if ("rows".equals(option.getType())) {
                rows = Integer.parseInt(option.getValue());
            } else {
                throw new IllegalStateException("Unknown option for textarea: " + option.getType());
            }
        }
    }

    @Override
    protected JSONObject getJsOptions(final Locale locale) throws JSONException {
        JSONObject json = super.getJsOptions(locale);
        json.put("rows", rows);
        return json;
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
