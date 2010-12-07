package com.qcadoo.mes.view.components;

import java.util.Locale;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.view.ComponentDefinition;
import com.qcadoo.mes.view.ComponentOption;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ViewComponent;

@ViewComponent("checkbox")
public final class CheckBoxComponentPattern extends FieldComponentPattern {

    private static final String JSP_PATH = "elements/checkbox.jsp";

    private static final String JS_OBJECT = "QCD.components.elements.CheckBox";

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
    protected Map<String, Object> getJspOptions(final Locale locale) {
        Map<String, Object> options = super.getJspOptions(locale);
        options.put("textRepresentationOnDisabled", textRepresentationOnDisabled);
        return options;
    }

    @Override
    protected JSONObject getJsOptions(final Locale locale) throws JSONException {
        JSONObject json = new JSONObject();
        JSONObject jsonTranslations = new JSONObject();
        jsonTranslations.put("true", getTranslationService().translate("commons.true", locale));
        jsonTranslations.put("false", getTranslationService().translate("commons.false", locale));
        json.put("translations", jsonTranslations);
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
