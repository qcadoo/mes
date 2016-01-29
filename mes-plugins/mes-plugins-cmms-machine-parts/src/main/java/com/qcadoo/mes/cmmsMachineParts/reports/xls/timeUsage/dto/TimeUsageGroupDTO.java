package com.qcadoo.mes.cmmsMachineParts.reports.xls.timeUsage.dto;

import java.util.Date;
import java.util.List;

public class TimeUsageGroupDTO {

    private List<TimeUsageDTO> timeUsages;

    private String worker;

    private Date date;

    public TimeUsageGroupDTO(Date date, String worker, List<TimeUsageDTO> timeUsages) {
        this.date = date;
        this.worker = worker;
        this.timeUsages = timeUsages;
    }

    public List<TimeUsageDTO> getTimeUsages() {
        return timeUsages;
    }

    public void setTimeUsages(List<TimeUsageDTO> timeUsages) {
        this.timeUsages = timeUsages;
    }

    public String getWorker() {
        return worker;
    }

    public void setWorker(String worker) {
        this.worker = worker;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Long getDurationSum() {
        return timeUsages.stream().mapToLong(u -> u.getDuration()).sum();
    }

    public Long getRegisteredTimeSum() {
        return timeUsages.stream().mapToLong(u -> u.getRegisteredTime()).sum();
    }

}
