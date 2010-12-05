package com.qcadoo.mes.view.components;

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
import com.qcadoo.mes.view.ribbon.Ribbon;

@ViewComponent("window")
public final class WindowComponentPattern extends AbstractContainerPattern {

    private static final String JSP_PATH = "newComponents/window.jsp";

    private static final String JS_PATH = "newComponents/window.js";

    private static final String JS_OBJECT = "QCD.components.containers.Window";

    private boolean header = true;

    private boolean fixedHeight = false;

    private Ribbon ribbon;

    public WindowComponentPattern(final ComponentDefinition componentDefinition) {
        super(componentDefinition);
    }

    @Override
    public ComponentState getComponentStateInstance() {
        return new EmptyContainerState();
    }

    @Override
    protected void initializeComponent() throws JSONException {
        for (ComponentOption option : getOptions()) {
            if ("fixedHeight".equals(option.getType())) {
                fixedHeight = Boolean.parseBoolean(option.getValue());
            } else if ("header".equals(option.getType())) {
                header = Boolean.parseBoolean(option.getValue());
            } else {
                throw new IllegalStateException("Unknown option for window: " + option.getType());
            }
        }
    }

    @Override
    protected JSONObject getJsOptions(final Locale locale) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("fixedHeight", fixedHeight);
        json.put("header", header);
        if (ribbon != null) {
            json.put("ribbon", getJsRibbon(locale));
        }

        return json;
    }

    private JSONObject getJsRibbon(final Locale locale) throws JSONException {
        JSONObject json = ribbon.getAsJson();

        for (int i = 0; i < json.getJSONArray("groups").length(); i++) {
            JSONObject group = json.getJSONArray("groups").getJSONObject(i);
            group.put("label", getTranslationService().translate(getTranslationCodes(group.getString("name")), locale));
            translateRibbonItems(group, group.getString("name") + ".", locale);
        }

        return json;
    }

    private void translateRibbonItems(final JSONObject owner, final String prefix, final Locale locale) throws JSONException {
        if (owner.has("items")) {
            for (int j = 0; j < owner.getJSONArray("items").length(); j++) {
                JSONObject item = owner.getJSONArray("items").getJSONObject(j);
                String label = getTranslationService().translate(getTranslationCodes(prefix + item.getString("name")), locale);
                item.put("label", label);
                translateRibbonItems(item, prefix + item.getString("name"), locale);
            }
        }
    }

    private List<String> getTranslationCodes(final String key) {
        return Arrays.asList(new String[] { getTranslationPath() + ".ribbon." + key, "core.ribbon." + key });
    }

    public void setRibbon(final Ribbon ribbon) {
        // TODO masz ribbon should be a window option
        this.ribbon = ribbon;
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
