package com.qcadoo.mes.basic.controllers.dataProvider.dto;

public class AdditionalCodeDTO implements AbstractDTO {

    private Long id;

    private String code;

    private String productnumber;

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

    public String getProductnumber() {
        return productnumber;
    }

    public void setProductnumber(String productnumber) {
        this.productnumber = productnumber;
    }

}
