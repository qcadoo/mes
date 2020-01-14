package com.qcadoo.mes.materialFlowResources;

import com.qcadoo.mes.basic.controllers.dataProvider.dto.AbstractDTO;

public class BatchDTO implements AbstractDTO{

    private Long id;

    private String number;

    private String product;

    private String supplier;

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

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }
}
