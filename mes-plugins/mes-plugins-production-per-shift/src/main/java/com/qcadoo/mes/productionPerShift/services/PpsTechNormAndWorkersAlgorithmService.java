package com.qcadoo.mes.productionPerShift.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.shift.Shift;
import com.qcadoo.mes.basic.util.DateTimeRange;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionLines.constants.ProductionLineFields;
import com.qcadoo.mes.productionPerShift.domain.ProgressForDaysContainer;
import com.qcadoo.mes.productionPerShift.domain.ShiftEfficiencyCalculationHolder;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.validators.ErrorMessage;

@Service
public class PpsTechNormAndWorkersAlgorithmService extends PpsBaseAlgorithmService {

    @Autowired
    private NumberService numberService;

    @Autowired
    private WorkersOnLineService workersOnLineService;

    @Autowired
    private TechnologyService technologyService;

    @Override
    protected ShiftEfficiencyCalculationHolder calculateShiftEfficiency(ProgressForDaysContainer progressForDaysContainer,
                                                                        Entity productionPerShift, Shift shift, Entity technology, Entity productionLine, DateTimeRange range, BigDecimal shiftEfficiency,
                                                                        int progressForDayQuantity, boolean allowIncompleteUnits) {
        ShiftEfficiencyCalculationHolder calculationHolder = new ShiftEfficiencyCalculationHolder();
        int workersOnLine = workersOnLineService.getWorkersOnLine(productionLine, shift.getEntity(), range.getFrom());
        if (workersOnLine == 0) {
            progressForDaysContainer.addError(new ErrorMessage("productionPerShift.automaticAlgorithm.noAssignmentForShift",
                    false, productionLine.getStringField(ProductionLineFields.NUMBER)));
            throw new IllegalStateException("No assignment for shift");
        }
        BigDecimal scaledNorm = getStandardPerformanceNorm(progressForDaysContainer, technology, productionLine);
        long minutes = range.durationInMins();
        BigDecimal efficiencyForRange = calculateEfficiencyForRange(scaledNorm, workersOnLine, minutes, allowIncompleteUnits);
        shiftEfficiency = shiftEfficiency.add(efficiencyForRange, numberService.getMathContext());
        calculationHolder.setShiftEfficiency(shiftEfficiency);
        if (shiftEfficiency.compareTo(progressForDaysContainer.getPlannedQuantity()) > 0) {
            calculateEfficiencyTime(calculationHolder, progressForDaysContainer.getPlannedQuantity(), workersOnLine, scaledNorm);
        } else {
            calculateEfficiencyTime(calculationHolder, shiftEfficiency, workersOnLine, scaledNorm);
        }
        return calculationHolder;
    }

    protected void calculateEfficiencyTime(ShiftEfficiencyCalculationHolder calculationHolder, BigDecimal shiftEfficiency,
                                           int workersOnLine, BigDecimal scaledNorm) {
        BigDecimal ratio = scaledNorm.multiply(new BigDecimal(workersOnLine), numberService.getMathContext());
        int time = shiftEfficiency.divide(ratio, numberService.getMathContext()).setScale(0, RoundingMode.HALF_UP).intValue();
        calculationHolder.addEfficiencyTime(time);
    }

    protected BigDecimal calculateEfficiencyForRange(BigDecimal scaledNorm, int workersOnLine, long minuets, boolean allowIncompleteUnits) {
        BigDecimal value = scaledNorm.multiply(new BigDecimal(workersOnLine), numberService.getMathContext());
        value = value.multiply(new BigDecimal(minuets), numberService.getMathContext());
        if (allowIncompleteUnits) {
            return value;
        } else {
            return value.setScale(0, RoundingMode.HALF_UP);
        }
    }

    protected BigDecimal getStandardPerformanceNorm(ProgressForDaysContainer progressForDaysContainer, Entity technology, Entity productionLine) {
        Optional<BigDecimal> norm = Optional.empty();
        if (productionLine != null) {
            technology = technology.getDataDefinition()
                    .get(technology.getId());
            norm = technologyService.getStandardPerformance(technology, productionLine);
        }
        if (!norm.isPresent()) {
            progressForDaysContainer.addError(new ErrorMessage(
                    "productionPerShift.automaticAlgorithm.technology.standardPerformanceTechnologyRequired", false));
            throw new IllegalStateException("No standard performance norm in technology");
        }
        return norm.get();
    }

}
