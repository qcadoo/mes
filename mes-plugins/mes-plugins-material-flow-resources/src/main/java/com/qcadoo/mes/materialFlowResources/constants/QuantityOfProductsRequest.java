package com.qcadoo.mes.materialFlowResources.constants;

import java.util.List;

public class QuantityOfProductsRequest {
    private Long materialFlowLocationId;
    private List<String> productsNumberList;

    public QuantityOfProductsRequest() {
    }

    public QuantityOfProductsRequest(Long materialFlowLocationId, List<String> productsNumberList) {
        this.materialFlowLocationId = materialFlowLocationId;
        this.productsNumberList = productsNumberList;
    }

    public Long getMaterialFlowLocationId() {
        return materialFlowLocationId;
    }

    public void setMaterialFlowLocationId(Long materialFlowLocationId) {
        this.materialFlowLocationId = materialFlowLocationId;
    }

    public List<String> getProductsNumberList() {
        return productsNumberList;
    }

    public void setProductsNumberList(List<String> productsNumberList) {
        this.productsNumberList = productsNumberList;
    }
}
