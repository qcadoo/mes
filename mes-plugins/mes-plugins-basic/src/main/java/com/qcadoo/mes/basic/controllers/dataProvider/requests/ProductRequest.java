package com.qcadoo.mes.basic.controllers.dataProvider.requests;

public class ProductRequest {

    private String number;

    private String name;

    private String unit;

    private String globalTypeOfMaterial;

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getGlobalTypeOfMaterial() {
        return globalTypeOfMaterial;
    }

    public void setGlobalTypeOfMaterial(String globalTypeOfMaterial) {
        this.globalTypeOfMaterial = globalTypeOfMaterial;
    }
}
