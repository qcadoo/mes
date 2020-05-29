package com.qcadoo.mes.masterOrders;

public class SubOrderErrorHolder {

    private String number;

    private String error;

    public SubOrderErrorHolder(String number, String error) {
        this.number = number;
        this.error = error;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
