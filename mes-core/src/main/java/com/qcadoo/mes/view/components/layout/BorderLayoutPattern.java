package com.qcadoo.mes.view.components.layout;

import com.qcadoo.mes.view.ComponentDefinition;
import com.qcadoo.mes.view.ViewComponent;

@ViewComponent("borderLayout")
public class BorderLayoutPattern extends AbstractLayoutPattern {

    private static final String JS_OBJECT = "QCD.components.containers.layout.BorderLayout";

    private static final String JSP_PATH = "containers/layout/borderLayout.jsp";

    private static final String JS_PATH = "/js/crud/qcd/components/containers/layout/borderLayout.js";

    public BorderLayoutPattern(final ComponentDefinition componentDefinition) {
        super(componentDefinition);
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
