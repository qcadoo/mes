package com.qcadoo.mes.materialFlowResources;

import com.qcadoo.mes.basic.controllers.dataProvider.dto.AbstractDTO;

public class StorageLocationDTO implements AbstractDTO{
    private Long id;
    private String number;

    private String product;

    private String location;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }


}
