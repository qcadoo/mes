package com.qcadoo.mes.masterOrders;

public class SubOrderErrorHolder {

    private String number;

    private String error;
    private String productNumber;

    public SubOrderErrorHolder(String number, String error) {
        this.number = number;
        this.error = error;
    }

    public SubOrderErrorHolder(String number, String error, String productNumber) {
        this.number = number;
        this.error = error;
        this.productNumber = productNumber;
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

    public String getProductNumber() {
        return productNumber;
    }

    public void setProductNumber(String productNumber) {
        this.productNumber = productNumber;
    }
}
