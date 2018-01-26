package com.qcadoo.mes.productionLines.helper;

import org.joda.time.DateTime;
import org.joda.time.Minutes;

public class WorkstationTypeComponentQuantity {

    private Integer quantity;

    private final DateTime from;

    private final DateTime to;

    private boolean toInfinity;

    public Integer getDurationInMinuts(){
        return Minutes.minutesBetween(from, to).getMinutes();
    }

    public WorkstationTypeComponentQuantity(Integer quantity, DateTime from, DateTime to) {
        this.quantity = quantity;
        this.from = from;
        this.to = to;
        this.toInfinity= false;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public DateTime getFrom() {
        return from;
    }

    public DateTime getTo() {
        return to;
    }

    public boolean isToInfinity() {
        return toInfinity;
    }

    public void setToInfinity(boolean toInfinity) {
        this.toInfinity = toInfinity;
    }
}
