package com.qcadoo.mes.view.menu.ribbon;


public abstract class RibbonItem {

    public static enum Type {
        BIG_BUTTON, SMALL_BUTTON, CHECKBOX, COMBOBOX
    }

    private String name;

    private Type type;

    private String icon;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public void setType(final Type type) {
        this.type = type;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(final String icon) {
        this.icon = icon;
    }

}
