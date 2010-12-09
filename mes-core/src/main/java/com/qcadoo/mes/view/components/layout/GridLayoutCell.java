package com.qcadoo.mes.view.components.layout;

import com.qcadoo.mes.view.ComponentPattern;

public class GridLayoutCell {

    private ComponentPattern component;

    private int rowspan = 1;

    private int colspan = 1;

    private boolean available = true;

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

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

}