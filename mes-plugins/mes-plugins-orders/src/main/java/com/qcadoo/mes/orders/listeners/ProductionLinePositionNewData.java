package com.qcadoo.mes.orders.listeners;

import java.util.Date;

import com.qcadoo.model.api.Entity;

public class ProductionLinePositionNewData {

    private Date startDate;

    private Date finishDate;

    private Entity changeover;

    public ProductionLinePositionNewData(Date newStartDate, Date newFinishDate, Entity newChangeover) {
        startDate = newStartDate;
        finishDate = newFinishDate;
        changeover = newChangeover;
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

    public Entity getChangeover() {
        return changeover;
    }

    public void setChangeover(Entity changeover) {
        this.changeover = changeover;
    }
}
