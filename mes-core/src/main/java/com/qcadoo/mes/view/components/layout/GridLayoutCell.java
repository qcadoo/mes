package com.qcadoo.mes.view.components.layout;

import java.util.LinkedList;
import java.util.List;

import com.qcadoo.mes.view.ComponentPattern;

public class GridLayoutCell {

    private List<ComponentPattern> components;

    private int rowspan = 1;

    private int colspan = 1;

    private boolean available = true;

    private boolean rightBorder = false;

    public List<ComponentPattern> getComponents() {
        return components;
    }

    public void addComponent(ComponentPattern component) {
        if (components == null) {
            components = new LinkedList<ComponentPattern>();
        }
        components.add(component);
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

    public boolean isRightBorder() {
        return rightBorder;
    }

    public void setRightBorder(boolean rightBorder) {
        this.rightBorder = rightBorder;
    }

}