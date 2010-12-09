package com.qcadoo.mes.view.components.layout;

import com.qcadoo.mes.view.ComponentDefinition;
import com.qcadoo.mes.view.ComponentPattern;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ViewComponent;
import com.qcadoo.mes.view.components.EmptyContainerState;
import com.qcadoo.mes.view.patterns.AbstractComponentPattern;
import com.qcadoo.mes.view.patterns.AbstractContainerPattern;

@ViewComponent("borderLayout")
public class BorderLayoutPattern extends AbstractContainerPattern {

    private static final String JS_OBJECT = "QCD.components.containers.layout.BorderLayout";

    private static final String JSP_PATH = "containers/layout/borderLayout.jsp";

    private static final String JS_PATH = "/js/crud/qcd/components/containers/layout/borderLayout.js";

    public BorderLayoutPattern(final ComponentDefinition componentDefinition) {
        super(componentDefinition);
    }

    public final void addFieldEntityIdChangeListener(final String field, final ComponentPattern listener) {
        AbstractComponentPattern parent = (AbstractComponentPattern) this.getParent();
        parent.addFieldEntityIdChangeListener(field, listener);
    }

    public final void addScopeEntityIdChangeListener(final String field, final ComponentPattern listener) {
        AbstractComponentPattern parent = (AbstractComponentPattern) this.getParent();
        parent.addScopeEntityIdChangeListener(field, listener);
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
