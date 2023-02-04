package com.qcadoo.mes.productionCounting.xls.dto;

import java.math.BigDecimal;

public class ProductionCost {

    private Long orderId;

    private String orderNumber;

    private String operationNumber;

    private Integer plannedStaffTime;

    private Integer realStaffTime;

    private Integer plannedMachineTime;

    private Integer realMachineTime;

    private BigDecimal plannedStaffCosts;

    private BigDecimal realStaffCosts;

    private BigDecimal staffCostsDeviation;

    private BigDecimal plannedMachineCosts;

    private BigDecimal realMachineCosts;

    private BigDecimal machineCostsDeviation;

    private BigDecimal plannedPieceworkCosts;

    private BigDecimal realPieceworkCosts;

    private BigDecimal plannedCostsSum;

    private BigDecimal realCostsSum;

    private BigDecimal sumCostsDeviation;
    private boolean pieceworkProduction;

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getOperationNumber() {
        return operationNumber;
    }

    public void setOperationNumber(String operationNumber) {
        this.operationNumber = operationNumber;
    }

    public Integer getPlannedStaffTime() {
        return plannedStaffTime;
    }

    public void setPlannedStaffTime(Integer plannedStaffTime) {
        this.plannedStaffTime = plannedStaffTime;
    }

    public Integer getRealStaffTime() {
        return realStaffTime;
    }

    public void setRealStaffTime(Integer realStaffTime) {
        this.realStaffTime = realStaffTime;
    }

    public Integer getPlannedMachineTime() {
        return plannedMachineTime;
    }

    public void setPlannedMachineTime(Integer plannedMachineTime) {
        this.plannedMachineTime = plannedMachineTime;
    }

    public Integer getRealMachineTime() {
        return realMachineTime;
    }

    public void setRealMachineTime(Integer realMachineTime) {
        this.realMachineTime = realMachineTime;
    }

    public BigDecimal getPlannedStaffCosts() {
        return plannedStaffCosts;
    }

    public void setPlannedStaffCosts(BigDecimal plannedStaffCosts) {
        this.plannedStaffCosts = plannedStaffCosts;
    }

    public BigDecimal getRealStaffCosts() {
        return realStaffCosts;
    }

    public void setRealStaffCosts(BigDecimal realStaffCosts) {
        this.realStaffCosts = realStaffCosts;
    }

    public BigDecimal getPlannedMachineCosts() {
        return plannedMachineCosts;
    }

    public void setPlannedMachineCosts(BigDecimal plannedMachineCosts) {
        this.plannedMachineCosts = plannedMachineCosts;
    }

    public BigDecimal getRealMachineCosts() {
        return realMachineCosts;
    }

    public void setRealMachineCosts(BigDecimal realMachineCosts) {
        this.realMachineCosts = realMachineCosts;
    }

    public BigDecimal getMachineCostsDeviation() {
        return machineCostsDeviation;
    }

    public void setMachineCostsDeviation(BigDecimal machineCostsDeviation) {
        this.machineCostsDeviation = machineCostsDeviation;
    }

    public BigDecimal getPlannedPieceworkCosts() {
        return plannedPieceworkCosts;
    }

    public void setPlannedPieceworkCosts(BigDecimal plannedPieceworkCosts) {
        this.plannedPieceworkCosts = plannedPieceworkCosts;
    }

    public BigDecimal getRealPieceworkCosts() {
        return realPieceworkCosts;
    }

    public void setRealPieceworkCosts(BigDecimal realPieceworkCosts) {
        this.realPieceworkCosts = realPieceworkCosts;
    }

    public BigDecimal getPlannedCostsSum() {
        return plannedCostsSum;
    }

    public void setPlannedCostsSum(BigDecimal plannedCostsSum) {
        this.plannedCostsSum = plannedCostsSum;
    }

    public BigDecimal getRealCostsSum() {
        return realCostsSum;
    }

    public void setRealCostsSum(BigDecimal realCostsSum) {
        this.realCostsSum = realCostsSum;
    }

    public BigDecimal getSumCostsDeviation() {
        return sumCostsDeviation;
    }

    public void setSumCostsDeviation(BigDecimal sumCostsDeviation) {
        this.sumCostsDeviation = sumCostsDeviation;
    }

    public BigDecimal getStaffCostsDeviation() {
        return staffCostsDeviation;
    }

    public void setStaffCostsDeviation(BigDecimal staffCostsDeviation) {
        this.staffCostsDeviation = staffCostsDeviation;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public boolean isPieceworkProduction() {
        return pieceworkProduction;
    }

    public void setPieceworkProduction(boolean pieceworkProduction) {
        this.pieceworkProduction = pieceworkProduction;
    }
}
