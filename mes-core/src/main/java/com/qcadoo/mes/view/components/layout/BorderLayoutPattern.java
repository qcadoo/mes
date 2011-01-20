package com.qcadoo.mes.view.components.layout;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONException;

import com.qcadoo.mes.view.ComponentDefinition;
import com.qcadoo.mes.view.ComponentOption;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ViewComponent;
import com.qcadoo.mes.view.components.EmptyContainerState;

@ViewComponent("borderLayout")
public class BorderLayoutPattern extends AbstractLayoutPattern {

    private static final String JS_OBJECT = "QCD.components.containers.layout.BorderLayout";

    private static final String JSP_PATH = "containers/layout/borderLayout.jsp";

    private static final String JS_PATH = "/js/crud/qcd/components/containers/layout/borderLayout.js";

    private String label;

    public BorderLayoutPattern(final ComponentDefinition componentDefinition) {
        super(componentDefinition);
    }

    @Override
    protected void initializeComponent() throws JSONException {
        for (ComponentOption option : getOptions()) {
            if ("label".equals(option.getType())) {
                label = option.getValue();
            }
        }
    }

    @Override
    protected Map<String, Object> getJspOptions(final Locale locale) {
        Map<String, Object> options = super.getJspOptions(locale);
        List<String> codes = Arrays.asList(new String[] { getTranslationPath() + "." + label, label });
        options.put("label", label != null ? getTranslationService().translate(codes, locale) : null);
        return options;
    }

    @Override
    protected ComponentState getComponentStateInstance() {
        return new EmptyContainerState();
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
