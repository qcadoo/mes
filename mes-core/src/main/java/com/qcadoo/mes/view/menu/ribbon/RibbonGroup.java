package com.qcadoo.mes.view.menu.ribbon;

import java.util.List;

public final class RibbonGroup {

    private String name;

    private List<RibbonItem> items;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public List<RibbonItem> getItems() {
        return items;
    }

    public void setItems(final List<RibbonItem> items) {
        this.items = items;
    }

}
