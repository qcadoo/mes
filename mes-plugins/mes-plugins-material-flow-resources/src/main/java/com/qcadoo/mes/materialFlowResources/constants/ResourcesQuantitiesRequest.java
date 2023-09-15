package com.qcadoo.mes.materialFlowResources.constants;

public class ResourcesQuantitiesRequest {
    private Long storageLocationId;
    private String productNumber;

    public ResourcesQuantitiesRequest() {
    }

    public ResourcesQuantitiesRequest(Long storageLocationId, String productNumber) {
        this.storageLocationId = storageLocationId;
        this.productNumber = productNumber;
    }

    public Long getStorageLocationId() {
        return storageLocationId;
    }

    public void setStorageLocationId(Long storageLocationId) {
        this.storageLocationId = storageLocationId;
    }

    public String getProductNumber() {
        return productNumber;
    }

    public void setProductNumber(String productNumber) {
        this.productNumber = productNumber;
    }
}
