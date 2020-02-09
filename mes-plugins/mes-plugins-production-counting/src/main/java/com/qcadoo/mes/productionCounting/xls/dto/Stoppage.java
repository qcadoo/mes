package com.qcadoo.mes.productionCounting.xls.dto;

import java.util.Date;

public class Stoppage {

    private String orderNumber;

    private String productionTrackingNumber;

    private String productionTrackingState;

    private int duration;

    private Date dateFrom;

    private Date dateTo;

    private String reason;

    private String description;

    private String division;

    private String productionLine;

    private String workstation;

    private String worker;

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getProductionTrackingNumber() {
        return productionTrackingNumber;
    }

    public void setProductionTrackingNumber(String productionTrackingNumber) {
        this.productionTrackingNumber = productionTrackingNumber;
    }

    public String getProductionTrackingState() {
        return productionTrackingState;
    }

    public void setProductionTrackingState(String productionTrackingState) {
        this.productionTrackingState = productionTrackingState;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public Date getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(Date dateFrom) {
        this.dateFrom = dateFrom;
    }

    public Date getDateTo() {
        return dateTo;
    }

    public void setDateTo(Date dateTo) {
        this.dateTo = dateTo;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDivision() {
        return division;
    }

    public void setDivision(String division) {
        this.division = division;
    }

    public String getProductionLine() {
        return productionLine;
    }

    public void setProductionLine(String productionLine) {
        this.productionLine = productionLine;
    }

    public String getWorkstation() {
        return workstation;
    }

    public void setWorkstation(String workstation) {
        this.workstation = workstation;
    }

    public String getWorker() {
        return worker;
    }

    public void setWorker(String worker) {
        this.worker = worker;
    }
}
