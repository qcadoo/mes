package com.qcadoo.mes.productionCounting.analysis.dto;

import java.math.BigDecimal;
import java.util.Date;

public class FinalProductAnalysisDto {

    private String productionLineNumber;

    private String companyNumber;

    private String assortmentName;

    private String productNumber;

    private String productName;

    private String productUnit;

    private String size;

    private BigDecimal quantity;

    private BigDecimal wastes;

    private BigDecimal doneQuantity;

    private String shiftName;

    private Date timeRangeFrom;

    private Date timeRangeTo;

    private String technologyGeneratorNumber;

    private String orderNumber;

    public String getProductionLineNumber() {
        return productionLineNumber;
    }

    public void setProductionLineNumber(String productionLineNumber) {
        this.productionLineNumber = productionLineNumber;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getAssortmentName() {
        return assortmentName;
    }

    public void setAssortmentName(String assortmentName) {
        this.assortmentName = assortmentName;
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

    public String getProductUnit() {
        return productUnit;
    }

    public void setProductUnit(String productUnit) {
        this.productUnit = productUnit;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getWastes() {
        return wastes;
    }

    public void setWastes(BigDecimal wastes) {
        this.wastes = wastes;
    }

    public BigDecimal getDoneQuantity() {
        return doneQuantity;
    }

    public void setDoneQuantity(BigDecimal doneQuantity) {
        this.doneQuantity = doneQuantity;
    }

    public String getShiftName() {
        return shiftName;
    }

    public void setShiftName(String shiftName) {
        this.shiftName = shiftName;
    }

    public Date getTimeRangeFrom() {
        return timeRangeFrom;
    }

    public void setTimeRangeFrom(Date timeRangeFrom) {
        this.timeRangeFrom = timeRangeFrom;
    }

    public Date getTimeRangeTo() {
        return timeRangeTo;
    }

    public void setTimeRangeTo(Date timeRangeTo) {
        this.timeRangeTo = timeRangeTo;
    }

    public String getTechnologyGeneratorNumber() {
        return technologyGeneratorNumber;
    }

    public void setTechnologyGeneratorNumber(String technologyGeneratorNumber) {
        this.technologyGeneratorNumber = technologyGeneratorNumber;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        FinalProductAnalysisDto that = (FinalProductAnalysisDto) o;

        if (productionLineNumber != null ? !productionLineNumber.equals(that.productionLineNumber)
                : that.productionLineNumber != null)
            return false;
        if (companyNumber != null ? !companyNumber.equals(that.companyNumber) : that.companyNumber != null)
            return false;
        if (assortmentName != null ? !assortmentName.equals(that.assortmentName) : that.assortmentName != null)
            return false;
        if (!productNumber.equals(that.productNumber))
            return false;
        if (!productName.equals(that.productName))
            return false;
        if (!productUnit.equals(that.productUnit))
            return false;
        if (size != null ? !size.equals(that.size) : that.size != null)
            return false;
        if (!quantity.equals(that.quantity))
            return false;
        if (!wastes.equals(that.wastes))
            return false;
        if (!doneQuantity.equals(that.doneQuantity))
            return false;
        if (shiftName != null ? !shiftName.equals(that.shiftName) : that.shiftName != null)
            return false;
        if (timeRangeFrom != null ? !timeRangeFrom.equals(that.timeRangeFrom) : that.timeRangeFrom != null)
            return false;
        if (timeRangeTo != null ? !timeRangeTo.equals(that.timeRangeTo) : that.timeRangeTo != null)
            return false;
        if (technologyGeneratorNumber != null ? !technologyGeneratorNumber.equals(that.technologyGeneratorNumber)
                : that.technologyGeneratorNumber != null)
            return false;
        return orderNumber.equals(that.orderNumber);
    }

    @Override
    public int hashCode() {
        int result = productionLineNumber != null ? productionLineNumber.hashCode() : 0;
        result = 31 * result + (companyNumber != null ? companyNumber.hashCode() : 0);
        result = 31 * result + (assortmentName != null ? assortmentName.hashCode() : 0);
        result = 31 * result + productNumber.hashCode();
        result = 31 * result + productName.hashCode();
        result = 31 * result + productUnit.hashCode();
        result = 31 * result + (size != null ? size.hashCode() : 0);
        result = 31 * result + quantity.hashCode();
        result = 31 * result + wastes.hashCode();
        result = 31 * result + doneQuantity.hashCode();
        result = 31 * result + (shiftName != null ? shiftName.hashCode() : 0);
        result = 31 * result + (timeRangeFrom != null ? timeRangeFrom.hashCode() : 0);
        result = 31 * result + (timeRangeTo != null ? timeRangeTo.hashCode() : 0);
        result = 31 * result + (technologyGeneratorNumber != null ? technologyGeneratorNumber.hashCode() : 0);
        result = 31 * result + orderNumber.hashCode();
        return result;
    }
}
