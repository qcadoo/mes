package com.qcadoo.mes.productionPerShift.domain;

import com.google.common.collect.Lists;
import com.qcadoo.model.api.Entity;
import org.joda.time.DateTime;

import java.util.List;

public class DailyProgressContainer {

    private List<Entity> dailyProgress = Lists.newArrayList();

    private boolean calculationError;

    private boolean partCalculation;

    private DateTime partCalculationToDate;

    public List<Entity> getDailyProgress() {
        return dailyProgress;
    }

    public void setDailyProgress(List<Entity> dailyProgress) {
        this.dailyProgress = dailyProgress;
    }

    public boolean isCalculationError() {
        return calculationError;
    }

    public void setCalculationError(boolean calculationError) {
        this.calculationError = calculationError;
    }

    public boolean isPartCalculation() {
        return partCalculation;
    }

    public void setPartCalculation(boolean partCalculation) {
        this.partCalculation = partCalculation;
    }

    public DateTime getPartCalculationToDate() {
        return partCalculationToDate;
    }

    public void setPartCalculationToDate(DateTime partCalculationToDate) {
        this.partCalculationToDate = partCalculationToDate;
    }
}
