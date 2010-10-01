package com.qcadoo.mes.view.menu.ribbon;

public abstract class RibbonItem {

    public static enum Type {
        BIG_BUTTON, SMALL_BUTTON, CHECKBOX, COMBOBOX
    }

    private String name;

    private Type type;

    private String icon;

    public final String getName() {
        return name;
    }

    public final void setName(final String name) {
        this.name = name;
    }

    public final Type getType() {
        return type;
    }

    public final void setType(final Type type) {
        this.type = type;
    }

    public final String getIcon() {
        return icon;
    }

    public final void setIcon(final String icon) {
        this.icon = icon;
    }

}
