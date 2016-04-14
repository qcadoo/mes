package com.qcadoo.mes.productionLines.helper;

import org.joda.time.DateTime;
import org.joda.time.Minutes;

public class WorkstationTypeComponentQuantity {

    private final Integer quantity;

    private final DateTime from;

    private final DateTime to;

    private Integer min;

    public Integer getDurationInMinuts(){
        return Minutes.minutesBetween(from, to).getMinutes();
    }

    public WorkstationTypeComponentQuantity(Integer quantity, DateTime from, DateTime to) {
        this.quantity = quantity;
        this.from = from;
        this.to = to;
        min =  Minutes.minutesBetween(from, to).getMinutes();
    }

    public Integer getQuantity() {
        return quantity;
    }

    public DateTime getFrom() {
        return from;
    }

    public DateTime getTo() {
        return to;
    }

    public Integer getMin() {
        return Minutes.minutesBetween(from, to).getMinutes();
    }

    public void setMin(Integer min) {
        this.min = min;
    }
}
