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

@ViewComponent("staticPage")
public final class StaticPageComponentPattern extends AbstractComponentPattern {

    private static final String JS_OBJECT = "QCD.components.elements.StaticComponent";

    private static final String JSP_PATH = "newComponents/staticPage.jsp";

    private String page;

    public StaticPageComponentPattern(final ComponentDefinition componentDefinition) {
        super(componentDefinition);
    }

    @Override
    protected void initializeComponent() throws JSONException {
        for (ComponentOption option : getOptions()) {
            if ("page".equals(option.getType())) {
                page = option.getValue();
            } else {
                throw new IllegalStateException("Unknown option for staticPage: " + option.getType());
            }
        }
    }

    @Override
    protected Map<String, Object> getJspOptions(final Locale locale) {
        Map<String, Object> options = new HashMap<String, Object>();
        options.put("page", page);
        return options;
    }

    @Override
    public ComponentState getComponentStateInstance() {
        return new EmptyComponentState();
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
