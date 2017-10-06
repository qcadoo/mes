package com.qcadoo.mes.basic.activityStream.model;

import java.util.Date;

public class ActivityDto {

    private Long id;

    private String message;

    private String type;

    private boolean viewed;

    private Date date;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isViewed() {
        return viewed;
    }

    public void setViewed(boolean viewed) {
        this.viewed = viewed;
    }

    public Date getDate() {
        return (date == null) ? null : new Date(date.getTime());
    }

    public void setDate(final Date date) {
        this.date = (date == null) ? null : new Date(date.getTime());
    }

}
