package com.qcadoo.mes.materialFlowResources.dto;

public class ColumnProperties {

    String name;

    boolean checked;

    boolean forAttribute;

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public boolean isForAttribute() {
        return forAttribute;
    }

    public void setForAttribute(boolean forAttribute) {
        this.forAttribute = forAttribute;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
