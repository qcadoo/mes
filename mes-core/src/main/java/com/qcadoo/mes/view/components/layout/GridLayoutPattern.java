package com.qcadoo.mes.view.components.layout;

import java.util.Locale;
import java.util.Map;

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

    private GridLayoutCell[][] cells;

    public GridLayoutPattern(final ComponentDefinition componentDefinition) {
        super(componentDefinition);

        cells = new GridLayoutCell[10][];
        for (int row = 0; row < cells.length; row++) {
            cells[row] = new GridLayoutCell[3];
            for (int col = 0; col < cells[row].length; col++) {
                cells[row][col] = new GridLayoutCell();
            }
        }

        cells[2][2].setRowspan(2);
        cells[3][2].setAvailable(false);

        cells[1][0].setColspan(3);
        cells[1][1].setAvailable(false);
        cells[1][2].setAvailable(false);

        cells[5][0].setColspan(2);
        cells[5][0].setRowspan(3);
        cells[5][1].setAvailable(false);
        cells[6][0].setAvailable(false);
        cells[6][1].setAvailable(false);
        cells[7][0].setAvailable(false);
        cells[7][1].setAvailable(false);
    }

    @Override
    public final Map<String, Object> prepareView(final Locale locale) {
        Map<String, Object> model = super.prepareView(locale);
        model.put("cells", cells);
        return model;
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
