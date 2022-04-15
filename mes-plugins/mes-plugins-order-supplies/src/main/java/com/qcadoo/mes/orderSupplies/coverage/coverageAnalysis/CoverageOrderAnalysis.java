package com.qcadoo.mes.orderSupplies.coverage.coverageAnalysis;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

public class CoverageOrderAnalysis {

    private Long coverageId;
    private Long orderId;
    private Long orderedProductId;
    private BigDecimal plannedQuantity;
    private String coverageDegree;
    private Date coveredFromTheDay;
    private Date deliveryTime;
    private Date componentsProductionDate;

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getOrderedProductId() {
        return orderedProductId;
    }

    public void setOrderedProductId(Long orderedProductId) {
        this.orderedProductId = orderedProductId;
    }

    public BigDecimal getPlannedQuantity() {
        return plannedQuantity;
    }

    public void setPlannedQuantity(BigDecimal plannedQuantity) {
        this.plannedQuantity = plannedQuantity;
    }

    public String getCoverageDegree() {
        return coverageDegree;
    }

    public void setCoverageDegree(String coverageDegree) {
        this.coverageDegree = coverageDegree;
    }

    public Date getCoveredFromTheDay() {
        return coveredFromTheDay;
    }

    public void setCoveredFromTheDay(Date coveredFromTheDay) {
        this.coveredFromTheDay = coveredFromTheDay;
    }

    public Date getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(Date deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    public Date getComponentsProductionDate() {
        return componentsProductionDate;
    }

    public void setComponentsProductionDate(Date componentsProductionDate) {
        this.componentsProductionDate = componentsProductionDate;
    }

    public Long getCoverageId() {
        return coverageId;
    }

    public void setCoverageId(Long coverageId) {
        this.coverageId = coverageId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CoverageOrderAnalysis that = (CoverageOrderAnalysis) o;
        return Objects.equals(orderId, that.orderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId);
    }
}
