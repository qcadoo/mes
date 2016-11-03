package com.qcadoo.mes.productionPerShift.services;

import com.qcadoo.mes.basic.shift.Shift;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionPerShift.DateTimeRange;
import com.qcadoo.mes.productionPerShift.domain.ProgressForDaysContainer;
import com.qcadoo.mes.productionPerShift.domain.ShiftEfficiencyCalculationHolder;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.validators.ErrorMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class PpsTechNormAlgorithmService extends PpsBaseAlgorithmService{

    @Autowired
    private NumberService numberService;

    @Override
    protected ShiftEfficiencyCalculationHolder calculateShiftEfficiency(ProgressForDaysContainer progressForDaysContainer,
            Entity productionPerShift, Shift shift, Entity order, DateTimeRange range, BigDecimal shiftEfficiency,
            int progressForDayQuantity) {
        ShiftEfficiencyCalculationHolder calculationHolder = new ShiftEfficiencyCalculationHolder();
        BigDecimal scaledNorm = getStandardPerformanceNorm(progressForDaysContainer, order);
        Long minuets = range.durationInMins();
        BigDecimal efficiencyForRange = calculateEfficiencyForRange(scaledNorm, minuets);
        shiftEfficiency = shiftEfficiency.add(efficiencyForRange, numberService.getMathContext());
        calculationHolder.setShiftEfficiency(shiftEfficiency);
        if (shiftEfficiency.compareTo(progressForDaysContainer.getPlannedQuantity()) > 0) {
            calculateEfficiencyTime(calculationHolder, progressForDaysContainer.getPlannedQuantity(), scaledNorm);
        } else {
            calculateEfficiencyTime(calculationHolder, shiftEfficiency, scaledNorm);
        }
        return calculationHolder;
    }

    protected void calculateEfficiencyTime(ShiftEfficiencyCalculationHolder calculationHolder, BigDecimal shiftEfficiency, BigDecimal scaledNorm) {
        int time = shiftEfficiency.divide(scaledNorm, numberService.getMathContext()).setScale(0, RoundingMode.HALF_UP).intValue();
        calculationHolder.addEfficiencyTime(time);
    }

    protected BigDecimal calculateEfficiencyForRange(BigDecimal scaledNorm, long minuets) {
        BigDecimal value = BigDecimal.ZERO;
        value = scaledNorm.multiply(new BigDecimal(minuets), numberService.getMathContext());
        return value.setScale(0, RoundingMode.HALF_UP);
    }

    protected BigDecimal getStandardPerformanceNorm(ProgressForDaysContainer progressForDaysContainer, Entity order) {
        BigDecimal norm = order.getBelongsToField(OrderFields.TECHNOLOGY).getDecimalField("standardPerformanceTechnology");
        if (norm == null) {
            progressForDaysContainer.addError(new ErrorMessage("productionPerShift.automaticAlgorithm.technology.standardPerformanceTechnologyRequired", false));
            throw new IllegalStateException("No standard performance norm in technology");
        }
        return norm;

    }

}
