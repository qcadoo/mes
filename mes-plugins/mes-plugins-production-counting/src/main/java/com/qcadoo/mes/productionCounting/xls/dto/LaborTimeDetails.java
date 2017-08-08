package com.qcadoo.mes.productionCounting.xls.dto;

public class LaborTimeDetails {

    private String orderNumber;

    private String operationNumber;

    private String staffNumber;

    private String staffName;

    private String staffSurname;

    private Integer laborTime;

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

    public Integer getLaborTime() {
        return laborTime;
    }

    public void setLaborTime(Integer laborTime) {
        this.laborTime = laborTime;
    }
}
