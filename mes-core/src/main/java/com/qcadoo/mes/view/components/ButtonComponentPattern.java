package com.qcadoo.mes.view.components;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.json.JSONException;

import com.qcadoo.mes.view.ComponentDefinition;
import com.qcadoo.mes.view.ComponentOption;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ViewComponent;
import com.qcadoo.mes.view.patterns.AbstractComponentPattern;

@ViewComponent("button")
public final class ButtonComponentPattern extends AbstractComponentPattern {

    private static final String JS_OBJECT = "QCD.components.elements.LinkButton";

    private static final String JSP_PATH = "newComponents/button.jsp";

    private String url;

    public ButtonComponentPattern(final ComponentDefinition componentDefinition) {
        super(componentDefinition);
    }

    @Override
    protected void initializeComponent() throws JSONException {
        super.initializeComponent();
        for (ComponentOption option : getOptions()) {
            if ("url".equals(option.getType())) {
                url = option.getValue();
            } else {
                throw new IllegalStateException("Unknown option for button: " + option.getType());
            }
        }
    }

    @Override
    public ComponentState getComponentStateInstance() {
        return new ButtonComponentState(url);
    }

    @Override
    protected Map<String, Object> getJspOptions(final Locale locale) {
        Map<String, Object> options = new HashMap<String, Object>();
        Map<String, Object> translations = new HashMap<String, Object>();
        translations.put("label", getTranslationService().translate(getTranslationPath() + ".label", locale));
        options.put("translations", translations);
        return options;
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
