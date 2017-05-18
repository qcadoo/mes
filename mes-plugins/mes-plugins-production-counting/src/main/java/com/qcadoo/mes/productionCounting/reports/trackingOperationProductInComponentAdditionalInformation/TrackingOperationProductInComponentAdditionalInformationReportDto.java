package com.qcadoo.mes.productionCounting.reports.trackingOperationProductInComponentAdditionalInformation;

import java.util.Objects;

final public class TrackingOperationProductInComponentAdditionalInformationReportDto {

    private String productionTrackingNumber;

    private String orderNumber;

    private String productNumber;

    private String productName;

    private String additionalInformation;

    public String getProductionTrackingNumber() {
        return productionTrackingNumber;
    }

    public void setProductionTrackingNumber(String productionTrackingNumber) {
        this.productionTrackingNumber = productionTrackingNumber;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getProductNumber() {
        return productNumber;
    }

    public void setProductNumber(String productNumber) {
        this.productNumber = productNumber;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getAdditionalInformation() {
        return additionalInformation;
    }

    public void setAdditionalInformation(String additionalInformation) {
        this.additionalInformation = additionalInformation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        TrackingOperationProductInComponentAdditionalInformationReportDto that = (TrackingOperationProductInComponentAdditionalInformationReportDto) o;
        return Objects.equals(productionTrackingNumber, that.productionTrackingNumber)
                && Objects.equals(orderNumber, that.orderNumber) && Objects.equals(productNumber, that.productNumber)
                && Objects.equals(productName, that.productName)
                && Objects.equals(additionalInformation, that.additionalInformation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productionTrackingNumber, orderNumber, productNumber, productName, additionalInformation);
    }
}
