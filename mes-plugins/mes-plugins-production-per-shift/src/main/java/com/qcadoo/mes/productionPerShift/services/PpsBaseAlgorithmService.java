package com.qcadoo.mes.productionPerShift.services;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.qcadoo.commons.dateTime.TimeRange;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.shift.Shift;
import com.qcadoo.mes.basicProductionCounting.BasicProductionCountingService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionLines.constants.ProductionLineFields;
import com.qcadoo.mes.productionPerShift.DateTimeRange;
import com.qcadoo.mes.productionPerShift.PpsTimeHelper;
import com.qcadoo.mes.productionPerShift.constants.DailyProgressFields;
import com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftConstants;
import com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftFields;
import com.qcadoo.mes.productionPerShift.constants.ProgressForDayFields;
import com.qcadoo.mes.productionPerShift.domain.DailyProgressContainer;
import com.qcadoo.mes.productionPerShift.domain.DailyProgressKey;
import com.qcadoo.mes.productionPerShift.domain.ProgressForDaysContainer;
import com.qcadoo.mes.productionPerShift.domain.ShiftEfficiencyCalculationHolder;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.validators.ErrorMessage;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

public abstract class PpsBaseAlgorithmService {

    @Autowired
    private BasicProductionCountingService basicProductionCountingService;

    @Autowired
    private DailyProgressService dailyProgressService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private PpsTimeHelper ppsTimeHelper;

    private Map<DailyProgressKey, Entity> dailyProgressesWithTrackingRecords;

    public void generateProgressForDays(ProgressForDaysContainer progressForDaysContainer, Entity productionPerShift) {
        Entity order = productionPerShift.getBelongsToField(ProductionPerShiftFields.ORDER);
        if (progressForDaysContainer.getOrder() != null) {
            order = progressForDaysContainer.getOrder();
        }
        Entity operationComponent = progressForDaysContainer.getOperationComponent();
        Date orderStartDate = order.getDateField(OrderFields.START_DATE);
        if (orderStartDate == null) {
            progressForDaysContainer.addError(new ErrorMessage("productionPerShift.automaticAlgorithm.order.startDateRequired",
                    false));
            throw new IllegalStateException("No start date in order");
        }

        Entity productionLine = order.getBelongsToField(OrderFields.PRODUCTION_LINE);
        if (productionLine == null) {
            progressForDaysContainer.addError(new ErrorMessage(
                    "productionPerShift.automaticAlgorithm.order.productionLineRequired", false));
            throw new IllegalStateException("No production line in order");
        }

        List<Shift> shifts = extractShiftsFormOrder(order);
        if (shifts.isEmpty()) {
            progressForDaysContainer.addError(new ErrorMessage(
                    "productionPerShift.automaticAlgorithm.productionLine.shiftsRequired", false, productionLine
                            .getStringField(ProductionLineFields.NUMBER)));
            throw new IllegalStateException("No shifts assigned to production line");
        }

        BigDecimal plannedQuantity = order.getDecimalField(OrderFields.PLANNED_QUANTITY);
        if (order.getBooleanField(OrderFields.FINAL_PRODUCTION_TRACKING)) {
            plannedQuantity = basicProductionCountingService.getProducedQuantityFromBasicProductionCountings(order);
        }
        calculateRegisteredQuantity(progressForDaysContainer, order, operationComponent, plannedQuantity);

        BigDecimal alreadyPlannedQuantity = BigDecimal.ZERO;
        List<Entity> progressForDays = Lists.newLinkedList();

        DateTime currentDate = new DateTime(orderStartDate);
        currentDate = currentDate.minusDays(1);
        currentDate = currentDate.toLocalDate().toDateTimeAtStartOfDay();
        boolean shouldBeCorrected = progressForDaysContainer.isShouldBeCorrected();
        int realizationDayNumber = 0;
        while (progressForDaysContainer.getPlannedQuantity().compareTo(BigDecimal.ZERO) > 0
                || progressForDaysContainer.getAlreadyRegisteredQuantity().compareTo(BigDecimal.ZERO) > 0) {

            DailyProgressContainer dailyProgressContainer = fillDailyProgressWithShifts(progressForDaysContainer,
                    productionPerShift, order, shifts, currentDate, orderStartDate, shouldBeCorrected, progressForDays.size(),
                    alreadyPlannedQuantity);
            if (dailyProgressContainer.isCalculationError()) {
                progressForDaysContainer.setCalculationError(true);
                return;

            }
            List<Entity> dailyProgress = dailyProgressContainer.getDailyProgress();
            if (!dailyProgress.isEmpty()) {
                progressForDays
                        .add(createComponent(realizationDayNumber, currentDate.toDate(), dailyProgress, shouldBeCorrected));
            }
            currentDate = currentDate.plusDays(1);
            ++realizationDayNumber;
        }

        progressForDaysContainer.setProgressForDays(progressForDays);
    }

    private DailyProgressContainer fillDailyProgressWithShifts(ProgressForDaysContainer progressForDaysContainer,
            Entity productionPerShift, Entity order, List<Shift> shifts, DateTime dateOfDay, Date orderStartDate,
            boolean shouldBeCorrected, int progressForDayQuantity, BigDecimal alreadyPlannedQuantity) {
        DailyProgressContainer dailyProgressContainer = new DailyProgressContainer();
        List<Entity> dailyProgressWithShifts = Lists.newLinkedList();

        for (Shift shift : shifts) {
            Entity dailyProgress = null;
            if (dailyProgressesWithTrackingRecords != null) {
                DailyProgressKey key = new DailyProgressKey(shift.getId(), dateOfDay);
                dailyProgress = dailyProgressesWithTrackingRecords.get(key);
            }
            if (dailyProgress != null) {
                BigDecimal producedQuantity = dailyProgress.getDecimalField(DailyProgressFields.QUANTITY);
                progressForDaysContainer.setAlreadyRegisteredQuantity(progressForDaysContainer.getAlreadyRegisteredQuantity()
                        .subtract(producedQuantity, numberService.getMathContext()));
                if (shouldBeCorrected) {
                    dailyProgress = dailyProgress.copy();
                    dailyProgress.setId(null);
                }
                dailyProgressWithShifts.add(dailyProgress);
            } else if (progressForDaysContainer.getPlannedQuantity().compareTo(BigDecimal.ZERO) > 0) {
                dailyProgress = dataDefinitionService.get(ProductionPerShiftConstants.PLUGIN_IDENTIFIER,
                        ProductionPerShiftConstants.MODEL_DAILY_PROGRESS).create();

                dailyProgress.setField(DailyProgressFields.SHIFT, shift.getEntity());

                DateTime orderStartDateDT = new DateTime(orderStartDate, DateTimeZone.getDefault());
                BigDecimal shiftEfficiency = BigDecimal.ZERO;
                int time = 0;
                for (DateTimeRange range : getShiftWorkDateTimes(shift, dateOfDay, orderStartDate)) {
                    if (orderStartDate.after(dateOfDay.toDate())) {
                        range = range.trimBefore(orderStartDateDT);
                    }
                    if (range != null) {
                        ShiftEfficiencyCalculationHolder calculationHolder = calculateShiftEfficiency(progressForDaysContainer,
                                productionPerShift, shift, order, range, shiftEfficiency, progressForDayQuantity);
                        shiftEfficiency = calculationHolder.getShiftEfficiency();
                        time = time + calculationHolder.getEfficiencyTime();
                    }
                }

                if (shiftEfficiency.compareTo(progressForDaysContainer.getPlannedQuantity()) > 0) {
                    shiftEfficiency = progressForDaysContainer.getPlannedQuantity();
                    progressForDaysContainer.setPlannedQuantity(BigDecimal.ZERO);
                } else {
                    alreadyPlannedQuantity = alreadyPlannedQuantity.add(shiftEfficiency, numberService.getMathContext());
                    progressForDaysContainer.setPlannedQuantity(progressForDaysContainer.getPlannedQuantity().subtract(
                            shiftEfficiency, numberService.getMathContext()));
                }

                if (shiftEfficiency.compareTo(BigDecimal.ZERO) != 0) {
                    dailyProgress.setField(DailyProgressFields.QUANTITY, numberService.setScale(shiftEfficiency));
                    dailyProgress.setField(DailyProgressFields.EFFICIENCY_TIME, time);
                    dailyProgressWithShifts.add(dailyProgress);
                }

            }
        }

        dailyProgressContainer.setDailyProgress(dailyProgressWithShifts);

        return dailyProgressContainer;
    }

    private List<DateTimeRange> getShiftWorkDateTimes(final Shift shift, DateTime dateOfDay, final Date orderStartDate) {
        DateTime dateOfDayDT = dateOfDay;
        List<TimeRange> shiftWorkTime = Lists.newArrayList();
        List<DateTimeRange> shiftWorkDateTime = Lists.newArrayList();
        if (shift.worksAt(dateOfDay.dayOfWeek().get())) {
            shiftWorkTime = shift.findWorkTimeAt(dateOfDay.toLocalDate());
        }
        for (TimeRange range : shiftWorkTime) {
            shiftWorkDateTime.add(new DateTimeRange(dateOfDayDT, range));
        }

        shiftWorkDateTime = ppsTimeHelper.manageExceptions(shiftWorkDateTime, shift.getEntity(), dateOfDay.toDate());

        return shiftWorkDateTime;
    }

    private Entity createComponent(final int dayNumber, Date realizationDate, final List<Entity> dailyProgress,
            boolean shouldBeCorrected) {
        Entity progressForDay = dataDefinitionService.get(ProductionPerShiftConstants.PLUGIN_IDENTIFIER,
                ProductionPerShiftConstants.MODEL_PROGRESS_FOR_DAY).create();

        progressForDay.setField(ProgressForDayFields.DAY, dayNumber);
        progressForDay.setField(ProgressForDayFields.DATE_OF_DAY, DateUtils.toDateString(realizationDate));
        progressForDay.setField(ProgressForDayFields.ACTUAL_DATE_OF_DAY, DateUtils.toDateString(realizationDate));
        progressForDay.setField(ProgressForDayFields.DAILY_PROGRESS, dailyProgress);
        progressForDay.setField(ProgressForDayFields.CORRECTED, shouldBeCorrected);

        return progressForDay;
    }

    private List<Shift> extractShiftsFormOrder(final Entity order) {
        Entity productionLine = order.getBelongsToField(OrderFields.PRODUCTION_LINE);
        List<Entity> entityShifts = productionLine.getManyToManyField(ProductionLineFields.SHIFTS);
        Collections.sort(entityShifts, (p1, p2) -> p1.getId().compareTo(p2.getId()));

        return FluentIterable.from(entityShifts).transform(new Function<Entity, Shift>() {

            @Override
            public Shift apply(final Entity shiftEntity) {
                return new Shift(shiftEntity);
            }
        }).toList();
    }

    private BigDecimal calculateRegisteredQuantity(final ProgressForDaysContainer progressForDaysContainer, final Entity order,
            final Entity operationComponent, BigDecimal plannedQuantity) {
        BigDecimal alreadyRegisteredQuantity = progressForDaysContainer.getAlreadyRegisteredQuantity();
        if (operationComponent != null) {
            dailyProgressesWithTrackingRecords = dailyProgressService.getDailyProgressesWithTrackingRecords(order,
                    operationComponent);
            for (Entity trackingRecord : dailyProgressesWithTrackingRecords.values()) {
                alreadyRegisteredQuantity = alreadyRegisteredQuantity.add(trackingRecord
                        .getDecimalField(DailyProgressFields.QUANTITY));
            }
            progressForDaysContainer.setAlreadyRegisteredQuantity(alreadyRegisteredQuantity);
        } else {
            dailyProgressesWithTrackingRecords = null;
        }
        progressForDaysContainer.setPlannedQuantity(plannedQuantity.subtract(alreadyRegisteredQuantity,
                numberService.getMathContext()));
        return progressForDaysContainer.getPlannedQuantity();
    }

    protected abstract ShiftEfficiencyCalculationHolder calculateShiftEfficiency(
            ProgressForDaysContainer progressForDaysContainer, Entity productionPerShift, Shift shift, Entity order,
            DateTimeRange range, BigDecimal shiftEfficiency, int progressForDayQuantity);
}
