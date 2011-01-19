package com.qcadoo.mes.view.components;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.qcadoo.mes.view.ComponentDefinition;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ViewComponent;
import com.qcadoo.mes.view.patterns.AbstractComponentPattern;

@ViewComponent("label")
public class LabelComponentPattern extends AbstractComponentPattern {

    private static final String JS_OBJECT = "QCD.components.elements.Label";

    private static final String JSP_PATH = "elements/label.jsp";

    private static final String JS_PATH = "/js/crud/qcd/components/elements/label.js";

    public LabelComponentPattern(final ComponentDefinition componentDefinition) {
        super(componentDefinition);
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
    protected ComponentState getComponentStateInstance() {
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
