package com.qcadoo.mes.view.menu.ribbon;

import java.util.List;

public final class RibbonComboItem extends RibbonItem {

    private List<RibbonActionItem> items;

    public List<RibbonActionItem> getItems() {
        return items;
    }

    public void setItems(final List<RibbonActionItem> items) {
        this.items = items;
    }

}
