package com.qcadoo.mes.productionLines.controller.dataProvider;

import java.util.Date;

public class ShiftWorkingDateResponse {

    private Date start;

    private Date finish;

    private final StatusCode code;

    private String message;

    public enum StatusCode {
        OK, ERROR;
    }

    public ShiftWorkingDateResponse(StatusCode code) {
        this.code = code;
    }

    public ShiftWorkingDateResponse(Date start, Date finish) {
        this.start = start;
        this.finish = finish;
        this.code = StatusCode.OK;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getFinish() {
        return finish;
    }

    public void setFinish(Date finish) {
        this.finish = finish;
    }

    public StatusCode getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
