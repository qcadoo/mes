package com.qcadoo.mes.productionPerShift.domain;

import java.math.BigDecimal;

public class ShiftEfficiencyCalculationHolder {

    private BigDecimal shiftEfficiency;

    private Integer efficiencyTime;

    public void addEfficiencyTime(int time) {
        if (efficiencyTime == null) {
            efficiencyTime = time;
        } else {
            efficiencyTime = efficiencyTime + time;
        }
    }

    public BigDecimal getShiftEfficiency() {
        return shiftEfficiency;
    }

    public void setShiftEfficiency(BigDecimal shiftEfficiency) {
        this.shiftEfficiency = shiftEfficiency;
    }

    public Integer getEfficiencyTime() {
        return efficiencyTime;
    }

    public void setEfficiencyTime(Integer efficiencyTime) {
        this.efficiencyTime = efficiencyTime;
    }
}
