package com.qcadoo.mes.productionCounting.xls.dto;

import java.math.BigDecimal;
import java.util.Date;

public class LaborTimeDetails {

    private String divisionNumber;

    private String productionLineNumber;

    private String orderNumber;

    private String orderState;

    private Date plannedDateFrom;

    private Date effectiveDateFrom;

    private Date plannedDateTo;

    private Date effectiveDateTo;

    private String productNumber;

    private String orderName;

    private BigDecimal plannedQuantity;

    private BigDecimal amountOfProductProduced;

    private String staffNumber;

    private String staffName;

    private String staffSurname;

    private String operationNumber;

    private Date timeRangeFrom;

    private Date timeRangeTo;

    private String shiftName;

    private Date createDate;

    private Integer laborTime;

    private Integer plannedLaborTime;

    private Integer laborTimeDeviation;

    private Integer machineTime;

    private Integer plannedMachineTime;

    private Integer machineTimeDeviation;

    public String getDivisionNumber() {
        return divisionNumber;
    }

    public void setDivisionNumber(String divisionNumber) {
        this.divisionNumber = divisionNumber;
    }

    public String getProductionLineNumber() {
        return productionLineNumber;
    }

    public void setProductionLineNumber(String productionLineNumber) {
        this.productionLineNumber = productionLineNumber;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getOrderState() {
        return orderState;
    }

    public void setOrderState(String orderState) {
        this.orderState = orderState;
    }

    public Date getPlannedDateFrom() {
        return plannedDateFrom;
    }

    public void setPlannedDateFrom(Date plannedDateFrom) {
        this.plannedDateFrom = plannedDateFrom;
    }

    public Date getEffectiveDateFrom() {
        return effectiveDateFrom;
    }

    public void setEffectiveDateFrom(Date effectiveDateFrom) {
        this.effectiveDateFrom = effectiveDateFrom;
    }

    public Date getPlannedDateTo() {
        return plannedDateTo;
    }

    public void setPlannedDateTo(Date plannedDateTo) {
        this.plannedDateTo = plannedDateTo;
    }

    public Date getEffectiveDateTo() {
        return effectiveDateTo;
    }

    public void setEffectiveDateTo(Date effectiveDateTo) {
        this.effectiveDateTo = effectiveDateTo;
    }

    public String getProductNumber() {
        return productNumber;
    }

    public void setProductNumber(String productNumber) {
        this.productNumber = productNumber;
    }

    public String getOrderName() {
        return orderName;
    }

    public void setOrderName(String orderName) {
        this.orderName = orderName;
    }

    public BigDecimal getPlannedQuantity() {
        return plannedQuantity;
    }

    public void setPlannedQuantity(BigDecimal plannedQuantity) {
        this.plannedQuantity = plannedQuantity;
    }

    public BigDecimal getAmountOfProductProduced() {
        return amountOfProductProduced;
    }

    public void setAmountOfProductProduced(BigDecimal amountOfProductProduced) {
        this.amountOfProductProduced = amountOfProductProduced;
    }

    public String getStaffNumber() {
        return staffNumber;
    }

    public void setStaffNumber(String staffNumber) {
        this.staffNumber = staffNumber;
    }

    public String getStaffName() {
        return staffName;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }

    public String getStaffSurname() {
        return staffSurname;
    }

    public void setStaffSurname(String staffSurname) {
        this.staffSurname = staffSurname;
    }

    public String getOperationNumber() {
        return operationNumber;
    }

    public void setOperationNumber(String operationNumber) {
        this.operationNumber = operationNumber;
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

    public String getShiftName() {
        return shiftName;
    }

    public void setShiftName(String shiftName) {
        this.shiftName = shiftName;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Integer getLaborTime() {
        return laborTime;
    }

    public void setLaborTime(Integer laborTime) {
        this.laborTime = laborTime;
    }

    public Integer getPlannedLaborTime() {
        return plannedLaborTime;
    }

    public void setPlannedLaborTime(Integer plannedLaborTime) {
        this.plannedLaborTime = plannedLaborTime;
    }

    public Integer getLaborTimeDeviation() {
        return laborTimeDeviation;
    }

    public void setLaborTimeDeviation(Integer laborTimeDeviation) {
        this.laborTimeDeviation = laborTimeDeviation;
    }

    public Integer getMachineTime() {
        return machineTime;
    }

    public void setMachineTime(Integer machineTime) {
        this.machineTime = machineTime;
    }

    public Integer getPlannedMachineTime() {
        return plannedMachineTime;
    }

    public void setPlannedMachineTime(Integer plannedMachineTime) {
        this.plannedMachineTime = plannedMachineTime;
    }

    public Integer getMachineTimeDeviation() {
        return machineTimeDeviation;
    }

    public void setMachineTimeDeviation(Integer machineTimeDeviation) {
        this.machineTimeDeviation = machineTimeDeviation;
    }
}
