package com.qcadoo.mes.cmmsMachineParts.reports.xls.sourceCost.dto;


public class WorkerCostsDTO {

    private String sourceCost;

    private String worker;

    private String event;

    private String type;

    private Integer workTime;

    private Integer workerTimeSum;

    private Integer costSourceTimeSum;

    public String getSourceCost() {
        return sourceCost;
    }

    public void setSourceCost(String sourceCost) {
        this.sourceCost = sourceCost;
    }

    public String getWorker() {
        return worker;
    }

    public void setWorker(String worker) {
        this.worker = worker;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getWorkTime() {
        return workTime;
    }

    public void setWorkTime(Integer workTime) {
        this.workTime = workTime;
    }

    public Integer getWorkerTimeSum() {
        return workerTimeSum;
    }

    public void setWorkerTimeSum(Integer workerTimeSum) {
        this.workerTimeSum = workerTimeSum;
    }

    public Integer getCostSourceTimeSum() {
        return costSourceTimeSum;
    }

    public void setCostSourceTimeSum(Integer costSourceTimeSum) {
        this.costSourceTimeSum = costSourceTimeSum;
    }

}
