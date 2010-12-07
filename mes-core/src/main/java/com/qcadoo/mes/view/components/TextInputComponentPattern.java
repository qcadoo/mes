package com.qcadoo.mes.view.components;

import java.util.Locale;
import java.util.Map;

import org.json.JSONException;

import com.qcadoo.mes.view.ComponentDefinition;
import com.qcadoo.mes.view.ComponentOption;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ViewComponent;

@ViewComponent("input")
public final class TextInputComponentPattern extends FieldComponentPattern {

    private static final String JSP_PATH = "elements/input.jsp";

    private static final String JS_OBJECT = "QCD.components.elements.TextInput";

    private boolean textRepresentationOnDisabled;

    public TextInputComponentPattern(final ComponentDefinition componentDefinition) {
        super(componentDefinition);
    }

    @Override
    protected void initializeComponent() throws JSONException {
        for (ComponentOption option : getOptions()) {
            if ("textRepresentationOnDisabled".equals(option.getType())) {
                textRepresentationOnDisabled = Boolean.parseBoolean(option.getValue());
            } else {
                throw new IllegalStateException("Unknown option for input: " + option.getType());
            }
        }
    }

    @Override
    protected Map<String, Object> getJspOptions(final Locale locale) {
        Map<String, Object> options = super.getJspOptions(locale);
        options.put("textRepresentationOnDisabled", textRepresentationOnDisabled);
        return options;
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
