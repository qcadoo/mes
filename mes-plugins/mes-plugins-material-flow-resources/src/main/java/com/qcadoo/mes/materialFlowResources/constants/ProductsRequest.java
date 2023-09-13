package com.qcadoo.mes.materialFlowResources.constants;

import java.util.List;

public class ProductsRequest {
    private String productNumber;
    private List<String> locationNumber;

    public String getProductNumber() {
        return productNumber;
    }

    public void setProductNumber(String productNumber) {
        this.productNumber = productNumber;
    }

    public List<String> getLocationNumber() {
        return locationNumber;
    }

    public void setLocationNumber(List<String> locationNumber) {
        this.locationNumber = locationNumber;
    }
}
