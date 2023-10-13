package com.qcadoo.mes.materialFlowResources.dto;

import java.math.BigDecimal;

public class CheckProductDto {
    private Long locationId;
    private Long productId;
    private Boolean placeStorageLocation;
    private BigDecimal maximumNumberOfPallets;

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Boolean getPlaceStorageLocation() {
        return placeStorageLocation;
    }

    public void setPlaceStorageLocation(Boolean placeStorageLocation) {
        this.placeStorageLocation = placeStorageLocation;
    }

    public BigDecimal getMaximumNumberOfPallets() {
        return maximumNumberOfPallets;
    }

    public void setMaximumNumberOfPallets(BigDecimal maximumNumberOfPallets) {
        this.maximumNumberOfPallets = maximumNumberOfPallets;
    }
}
