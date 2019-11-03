package com.qcadoo.mes.materialFlowResources.dto;

public class ColumnProperties {

    String name;

    boolean checked;

    boolean forAttribute;

    String attributeDataType;

    String attributeValueType;

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

    public String getAttributeDataType() {
        return attributeDataType;
    }

    public void setAttributeDataType(String attributeDataType) {
        this.attributeDataType = attributeDataType;
    }

    public String getAttributeValueType() {
        return attributeValueType;
    }

    public void setAttributeValueType(String attributeValueType) {
        this.attributeValueType = attributeValueType;
    }
}
