package com.qcadoo.mes.basic.controllers.dataProvider.dto;

public class ProductDTO implements AbstractDTO {

    private Long id;

    private String code;

    private String number;

    private String name;

    private String ean;

    private String globaltypeofmaterial;

    private String category;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

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

    public String getEan() {
        return ean;
    }

    public void setEan(String ean) {
        this.ean = ean;
    }

    public String getGlobaltypeofmaterial() {
        return globaltypeofmaterial;
    }

    public void setGlobaltypeofmaterial(String globaltypeofmaterial) {
        this.globaltypeofmaterial = globaltypeofmaterial;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

}
