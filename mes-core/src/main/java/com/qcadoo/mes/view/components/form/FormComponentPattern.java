package com.qcadoo.mes.view.components.form;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.view.ComponentDefinition;
import com.qcadoo.mes.view.ComponentOption;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ViewComponent;
import com.qcadoo.mes.view.patterns.AbstractContainerPattern;

@ViewComponent("form")
public final class FormComponentPattern extends AbstractContainerPattern {

    private static final String JSP_PATH = "containers/form.jsp";

    private static final String JS_OBJECT = "QCD.components.containers.Form";

    private boolean header;

    private String expression = "#id";

    public FormComponentPattern(final ComponentDefinition componentDefinition) {
        super(componentDefinition);
    }

    @Override
    protected void initializeComponent() throws JSONException {
        System.out.println(" -- INITIALIZE FORM");
        System.out.println(getDataDefinition());
        System.out.println(getChildren());
        if (getDataDefinition() != null) {
            System.out.println(getDataDefinition().getName());
        }
        for (ComponentOption option : getOptions()) {
            if ("expression".equals(option.getType())) {
                expression = option.getValue();
            } else if ("header".equals(option.getType())) {
                header = Boolean.parseBoolean(option.getValue());
            } else {
                throw new IllegalStateException("Unknown option for form: " + option.getType());
            }
        }
        System.out.println(getFieldEntityIdChangeListeners());
        System.out.println(getScopeEntityIdChangeListeners());
    }

    @Override
    protected JSONObject getJsOptions(final Locale locale) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("header", header);

        JSONObject translations = new JSONObject();

        addTranslation(translations, "confirmCancelMessage", locale);
        addTranslation(translations, "confirmDeleteMessage", locale);
        addTranslation(translations, "entityWithoutIdentifier", locale);

        translations.put("loading", getTranslationService().translate("commons.loading", locale));

        json.put("translations", translations);

        return json;
    }

    private void addTranslation(final JSONObject translation, final String key, final Locale locale) throws JSONException {
        List<String> codes = Arrays.asList(new String[] { getTranslationPath() + "." + key, "core.form." + key });
        translation.put(key, getTranslationService().translate(codes, locale));
    }

    @Override
    public ComponentState getComponentStateInstance() {
        return new FormComponentState(expression);
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
