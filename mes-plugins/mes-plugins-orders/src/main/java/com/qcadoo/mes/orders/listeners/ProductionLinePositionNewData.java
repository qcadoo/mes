package com.qcadoo.mes.orders.listeners;

import java.util.Date;

public class ProductionLinePositionNewData {

    private Date startDate;

    private Date finishDate;

    public ProductionLinePositionNewData(Date newStartDate, Date newFinishDate) {
        startDate = newStartDate;
        finishDate = newFinishDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getFinishDate() {
        return finishDate;
    }

    public void setFinishDate(Date finishDate) {
        this.finishDate = finishDate;
    }
}
