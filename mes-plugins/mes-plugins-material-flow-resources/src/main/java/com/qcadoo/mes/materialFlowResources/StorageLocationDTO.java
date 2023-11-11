package com.qcadoo.mes.materialFlowResources;

import com.qcadoo.mes.basic.controllers.dataProvider.dto.AbstractDTO;

public class StorageLocationDTO implements AbstractDTO {

    private Long id;

    private String number;

    private String product;

    private String location;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(final String number) {
        this.number = number;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(final String product) {
        this.product = product;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(final String location) {
        this.location = location;
    }

}
