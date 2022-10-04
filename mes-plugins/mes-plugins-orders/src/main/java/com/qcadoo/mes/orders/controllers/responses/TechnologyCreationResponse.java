package com.qcadoo.mes.orders.controllers.responses;

import com.google.common.collect.Lists;
import com.qcadoo.mes.orders.controllers.dto.OperationalTaskHolder;
import com.qcadoo.mes.orders.controllers.dto.OrderHolder;

import java.util.List;

public class TechnologyCreationResponse {
    private StatusCode code;
    private String number;
    private String message;
    private String additionalInformation;
    public enum StatusCode {
        OK, ERROR;
    }
    public TechnologyCreationResponse(StatusCode code) {
        this.code = code;
    }
    public TechnologyCreationResponse(String message) {
        this.code = code;
        this.message = message;
    }

    public StatusCode getCode() {
        return code;
    }

    public void setCode(StatusCode code) {
        this.code = code;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAdditionalInformation() {
        return additionalInformation;
    }

    public void setAdditionalInformation(String additionalInformation) {
        this.additionalInformation = additionalInformation;
    }
}
