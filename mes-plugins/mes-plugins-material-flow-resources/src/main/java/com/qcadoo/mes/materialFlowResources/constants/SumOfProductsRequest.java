package com.qcadoo.mes.materialFlowResources.constants;

import java.util.List;

public class SumOfProductsRequest {
    private String productNumber;
    List<String> locationNumber;

    public List<String> getLocationNumber() {
        return locationNumber;
    }

    public void setLocationNumber(List<String> locationNumber) {
        this.locationNumber = locationNumber;
    }

    public String getProductNumber() {
        return productNumber;
    }

    public void setProductNumber(String productNumber) {
        this.productNumber = productNumber;
    }
}
