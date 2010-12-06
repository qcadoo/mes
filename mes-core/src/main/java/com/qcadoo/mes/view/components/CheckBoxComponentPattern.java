package com.qcadoo.mes.view.components;

import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.view.ComponentDefinition;
import com.qcadoo.mes.view.ComponentOption;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ViewComponent;

@ViewComponent("checkbox")
public final class CheckBoxComponentPattern extends FieldComponentPattern {

    private static final String JS_OBJECT = "QCD.components.elements.TextInput";

    private static final String JSP_PATH = "newComponents/input.jsp";

    private boolean textRepresentationOnDisabled;

    public CheckBoxComponentPattern(final ComponentDefinition componentDefinition) {
        super(componentDefinition);
    }

    @Override
    public ComponentState getComponentStateInstance() {
        return new FieldComponentState();
    }

    @Override
    protected void initializeComponent() throws JSONException {
        for (ComponentOption option : getOptions()) {
            if ("textRepresentationOnDisabled".equals(option.getType())) {
                textRepresentationOnDisabled = Boolean.parseBoolean(option.getValue());
            } else {
                throw new IllegalStateException("Unknown option for checkbox: " + option.getType());
            }
        }
    }

    @Override
    protected JSONObject getJsOptions(final Locale locale) throws JSONException {
        JSONObject json = super.getJsOptions(locale);
        json.put("textRepresentationOnDisabled", textRepresentationOnDisabled);
        return json;
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
