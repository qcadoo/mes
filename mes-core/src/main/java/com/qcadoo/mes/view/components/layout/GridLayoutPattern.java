package com.qcadoo.mes.view.components.layout;

import com.qcadoo.mes.view.ComponentDefinition;
import com.qcadoo.mes.view.ComponentPattern;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ViewComponent;
import com.qcadoo.mes.view.components.EmptyContainerState;
import com.qcadoo.mes.view.patterns.AbstractComponentPattern;
import com.qcadoo.mes.view.patterns.AbstractContainerPattern;

@ViewComponent("gridLayout")
public class GridLayoutPattern extends AbstractContainerPattern {

    private static final String JS_OBJECT = "QCD.components.containers.layout.GridLayout";

    private static final String JSP_PATH = "containers/layout/gridLayout.jsp";

    private static final String JS_PATH = "/js/crud/qcd/components/containers/layout/gridLayout.js";

    private GridLayoutCell[][] cells = new GridLayoutCell[44][];

    public GridLayoutPattern(final ComponentDefinition componentDefinition) {
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

class GridLayoutCell {

    private ComponentPattern component;

    private int rowspan = 1;

    private int colspan = 1;

    private boolean isAvalible = true;

    public ComponentPattern getComponent() {
        return component;
    }

    public void setComponent(ComponentPattern component) {
        this.component = component;
    }

    public int getRowspan() {
        return rowspan;
    }

    public void setRowspan(int rowspan) {
        this.rowspan = rowspan;
    }

    public int getColspan() {
        return colspan;
    }

    public void setColspan(int colspan) {
        this.colspan = colspan;
    }

    public boolean isAvalible() {
        return isAvalible;
    }

    public void setAvalible(boolean isAvalible) {
        this.isAvalible = isAvalible;
    }

}
