package com.qcadoo.mes.materialFlowResources.storagelocation.imports;

public class ImportedStorageLocationPosition {

    private String storageLocation;

    private String product;

    public ImportedStorageLocationPosition withStorageLocation(String storageLocation) {
        this.storageLocation = storageLocation;
        return this;
    }

    public ImportedStorageLocationPosition withProduct(String product) {
        this.product = product;
        return this;
    }

    public String getProduct() {
        return product;
    }

    public String getStorageLocation() {
        return storageLocation;
    }
}
