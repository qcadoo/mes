package com.qcadoo.mes.cmmsMachineParts.reports.xls.timeUsage.dto;

import java.util.Date;


public class TimeUsageDTO {

    private String worker;

    private Date startDate;

    private String number;

    private String type;

    private String state;

    private String object;

    private String parts;

    private String description;

    private Integer duration;

    private Date registeredStart;

    private Date registeredEnd;

    public String getWorker() {
        return worker;
    }

    public void setWorker(String worker) {
        this.worker = worker;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public String getParts() {
        return parts;
    }

    public void setParts(String parts) {
        this.parts = parts;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Date getRegisteredStart() {
        return registeredStart;
    }

    public void setRegisteredStart(Date registeredStart) {
        this.registeredStart = registeredStart;
    }

    public Date getRegisteredEnd() {
        return registeredEnd;
    }

    public void setRegisteredEnd(Date registeredEnd) {
        this.registeredEnd = registeredEnd;
    }

}
