package com.qcadoo.mes.view.menu.ribbon;

import java.util.List;

public final class RibbonActionItem extends RibbonItem {

    private List<String> clickAction;

    public List<String> getAction() {
        return clickAction;
    }

    public void setAction(final List<String> clickAction) {
        this.clickAction = clickAction;
    }

}
