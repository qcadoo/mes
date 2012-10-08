package com.qcadoo.mes.avgLaborCostCalcForOrder;

import static com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftFields.SHIFT;
import static com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftFields.START_DATE;
import static com.qcadoo.mes.assignmentToShift.constants.StaffAssignmentToShiftFields.WORKER;
import static com.qcadoo.mes.avgLaborCostCalcForOrder.constants.AvgLaborCostCalcForOrderFields.AVERAGE_LABOR_HOURLY_COST;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftConstants;
import com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftFields;
import com.qcadoo.mes.assignmentToShift.constants.StaffAssignmentToShiftFields;
import com.qcadoo.mes.assignmentToShift.constants.StaffAssignmentToShiftState;
import com.qcadoo.mes.assignmentToShift.states.constants.AssignmentToShiftState;
import com.qcadoo.mes.avgLaborCostCalcForOrder.constants.AssignmentWorkerToShiftFields;
import com.qcadoo.mes.avgLaborCostCalcForOrder.constants.AvgLaborCostCalcForOrderConstants;
import com.qcadoo.mes.avgLaborCostCalcForOrder.constants.AvgLaborCostCalcForOrderFields;
import com.qcadoo.mes.basic.ShiftsService;
import com.qcadoo.mes.basic.ShiftsServiceImpl.ShiftHour;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class AverageCostService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private ShiftsService shiftsService;

    public Entity generateAssignmentWorkerToShiftAndAverageCost(final Entity entity, final Date start, final Date finish,
            final Entity productionLine) {
        Entity avgLaborCostCalcForOrder = entity.getDataDefinition().get(entity.getId());
        List<DateTime> days = getDaysBetweenGivenDates(start, finish);
        List<Entity> shifts = getAllShifts();
        Map<Entity, BigDecimal> workersWithHoursWorked = generateMapWorkersWithHoursWorked(days, shifts, productionLine);
        BigDecimal averageCost = countAverageCost(workersWithHoursWorked);

        avgLaborCostCalcForOrder.setField(AVERAGE_LABOR_HOURLY_COST, averageCost);
        avgLaborCostCalcForOrder.setField(AvgLaborCostCalcForOrderFields.ASSIGNMENT_WORKER_TO_SHIFTS,
                createAssignmentWorkerToShift(workersWithHoursWorked));

        return avgLaborCostCalcForOrder.getDataDefinition().save(avgLaborCostCalcForOrder);
    }

    private Map<Entity, BigDecimal> generateMapWorkersWithHoursWorked(final List<DateTime> days, final List<Entity> shifts,
            final Entity productionLine) {
        Map<Entity, BigDecimal> workersWithHours = new HashMap<Entity, BigDecimal>();
        for (DateTime day : days) {
            for (Entity shift : shifts) {
                Entity assignmentToShift = getAssignmentToShift(shift, day.toDate());
                if (assignmentToShift == null) {
                    continue;
                }
                List<Entity> staffs = getStaffAssignmentToShiftDependOnAssignmentToShiftState(assignmentToShift, productionLine);
                for (Entity staff : staffs) {
                    if (workersWithHours.containsKey(staff)) {
                        BigDecimal countHours = workersWithHours.get(staff).add(getWorkedHoursOfWorker(shift, day));
                        workersWithHours.put(staff, countHours);
                    } else {
                        workersWithHours.put(staff, getWorkedHoursOfWorker(shift, day));
                    }
                }
            }
        }
        return workersWithHours;
    }

    private BigDecimal countAverageCost(final Map<Entity, BigDecimal> workersWithHoursWorked) {
        BigDecimal averageCost = BigDecimal.ZERO;
        BigDecimal countHours = BigDecimal.ZERO;
        for (Entry<Entity, BigDecimal> workerWithHours : workersWithHoursWorked.entrySet()) {
            BigDecimal quantityOfHours = workerWithHours.getValue();
            BigDecimal costOfWorkerHours = workerWithHours.getKey().getBelongsToField(StaffAssignmentToShiftFields.WORKER)
                    .getDecimalField("laborHourlyCost").multiply(quantityOfHours);
            averageCost = averageCost.add(costOfWorkerHours);
            countHours = countHours.add(quantityOfHours);
        }
        return numberService.setScale(averageCost.divide(countHours, numberService.getMathContext()));
    }

    private BigDecimal getWorkedHoursOfWorker(final Entity shift, final DateTime dateOfDay) {
        BigDecimal hours = BigDecimal.ZERO;
        List<ShiftHour> workedHours = shiftsService.getHoursForShift(shift, dateOfDay.toDate(), dateOfDay.plusDays(1).toDate());
        for (ShiftHour shiftHour : workedHours) {
            DateTime dateFrom = new DateTime(shiftHour.getDateFrom());
            DateTime dateTo = new DateTime(shiftHour.getDateTo());
            Period p = new Period(dateFrom, dateTo);
            hours = hours.add(new BigDecimal(p.getHours()));
        }
        return hours;
    }

    private Entity getAssignmentToShift(final Entity shift, final Date date) {
        boolean shiftWorks = shiftsService.checkIfShiftWorkAtDate(date, shift);
        if (shiftWorks) {
            return dataDefinitionService
                    .get(AssignmentToShiftConstants.PLUGIN_IDENTIFIER, AssignmentToShiftConstants.MODEL_ASSIGNMENT_TO_SHIFT)
                    .find().add(SearchRestrictions.belongsTo(SHIFT, shift)).add(SearchRestrictions.le(START_DATE, date))
                    .addOrder(SearchOrders.desc(START_DATE)).setMaxResults(1).uniqueResult();
        } else {
            return null;
        }
    }

    private List<Entity> getStaffAssignmentToShiftDependOnAssignmentToShiftState(final Entity assignmentToShift,
            final Entity productionLine) {
        List<Entity> staffAssignmentToShifts = new ArrayList<Entity>();
        String state = assignmentToShift.getStringField(AssignmentToShiftFields.STATE);
        SearchCriteriaBuilder searchCriteriaBuilder = assignmentToShift.getHasManyField("staffAssignmentToShifts").find()
                .add(SearchRestrictions.eq("occupationTypeEnum", "01workOnLine"))
                .add(SearchRestrictions.belongsTo(StaffAssignmentToShiftFields.PRODUCTION_LINE, productionLine));
        if (state.equals(AssignmentToShiftState.CORRECTED.getStringValue())) {
            staffAssignmentToShifts = searchCriteriaBuilder
                    .add(SearchRestrictions.eq("state", StaffAssignmentToShiftState.CORRECTED)).list().getEntities();
        } else if (state.equals(AssignmentToShiftState.ACCEPTED.getStringValue())
                || state.equals(AssignmentToShiftState.DURING_CORRECTION.getStringValue())) {
            staffAssignmentToShifts = searchCriteriaBuilder
                    .add(SearchRestrictions.eq("state", StaffAssignmentToShiftState.ACCEPTED.getStringValue())).list()
                    .getEntities();
        }
        return staffAssignmentToShifts;
    }

    private List<Entity> createAssignmentWorkerToShift(final Map<Entity, BigDecimal> workersWithHoursWorked) {
        Map<Long, Entity> workers = new HashMap<Long, Entity>();
        for (Entry<Entity, BigDecimal> workerWithHours : workersWithHoursWorked.entrySet()) {
            Entity worker = workerWithHours.getKey().getBelongsToField(WORKER);
            Long workerId = worker.getId();
            Entity assignmentWorkerToShift = dataDefinitionService.get(AvgLaborCostCalcForOrderConstants.PLUGIN_IDENTIFIER,
                    AvgLaborCostCalcForOrderConstants.MODEL_ASSIGNMENT_WORKER_TO_SHIFT).create();
            if (workers.containsKey(workerId)) {
                assignmentWorkerToShift = workers.get(workerId);
                BigDecimal countHours = assignmentWorkerToShift.getDecimalField(AssignmentWorkerToShiftFields.WORKED_HOURS).add(
                        workerWithHours.getValue());
                assignmentWorkerToShift.setField(AssignmentWorkerToShiftFields.WORKED_HOURS, countHours);
            } else {
                assignmentWorkerToShift.setField(AssignmentWorkerToShiftFields.ASSIGNMENT_TO_SHIFT, workerWithHours.getKey()
                        .getBelongsToField("assignmentToShift"));
                assignmentWorkerToShift.setField(AssignmentWorkerToShiftFields.WORKER, worker);
                assignmentWorkerToShift.setField(AssignmentWorkerToShiftFields.WORKED_HOURS, workerWithHours.getValue());
            }
            workers.put(worker.getId(), assignmentWorkerToShift);
        }
        return Lists.newArrayList(workers.values());
    }

    private List<DateTime> getDaysBetweenGivenDates(final Date start, final Date finish) {
        List<DateTime> days = new LinkedList<DateTime>();
        DateTime startDate = new DateTime(start);
        DateTime finishDate = new DateTime(finish);

        DateTime nextDay = startDate;
        int numberOfDays = Days.daysBetween(startDate.toDateMidnight(), finishDate.toDateMidnight()).getDays();
        days.add(nextDay);

        int oneDay = 1;
        while (numberOfDays != 0) {
            nextDay = nextDay.plusDays(oneDay).toDateTime();
            days.add(nextDay);
            numberOfDays--;
        }
        return days;
    }

    private List<Entity> getAllShifts() {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_SHIFT).find().list()
                .getEntities();
    }
}
