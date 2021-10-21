package com.qcadoo.mes.basic.listeners;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.constants.ShiftTimetableExceptionFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;

@Service
public class ShiftTimetableExceptionListeners {

    private static final String L_REPRODUCTION_TO_DATE = "reproductionToDate";

    private static final String L_REPRODUCTION_TYPE = "reproductionType";

    public final void goToGenerateTimetableExceptions(final ViewDefinitionState view, final ComponentState state,
            final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        if (Objects.nonNull(form.getEntityId())) {
            Map<String, Object> parameters = Maps.newHashMap();

            parameters.put("form.id", form.getEntityId());

            String url = "../page/basic/generateShiftTimetableExceptionDetails.html";
            view.openModal(url, parameters);
        }
    }

    public final void generateTimetableExceptions(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity generateTimetableExceptionEntity = form.getPersistedEntityWithIncludedFormValues();
        Entity timetableExceptionEntity = generateTimetableExceptionEntity.getDataDefinition().get(form.getEntityId());

        if (isTimetableExceptionValid(form, generateTimetableExceptionEntity)) {
            return;
        }

        Date reproductionToDate = generateTimetableExceptionEntity.getDateField(L_REPRODUCTION_TO_DATE);
        String reproductionType = generateTimetableExceptionEntity.getStringField(L_REPRODUCTION_TYPE);

        LocalDateTime reproductionToLocalDateTime = convertToLocalDateTime(reproductionToDate);

        LocalDateTime fromLocalDateTime = convertToLocalDateTime(generateTimetableExceptionEntity
                .getDateField(ShiftTimetableExceptionFields.FROM_DATE));
        LocalDateTime toLocalDateTime = convertToLocalDateTime(generateTimetableExceptionEntity
                .getDateField(ShiftTimetableExceptionFields.TO_DATE));

        long unitsQuantity = unitsQuantity(reproductionType, fromLocalDateTime.with(LocalTime.MIN), reproductionToLocalDateTime);

        for (long i = 1; i <= unitsQuantity; i++) {
            LocalDateTime from = getNextDate(reproductionType, fromLocalDateTime, i);
            LocalDateTime to = getNextDate(reproductionType, toLocalDateTime, i);
            generateTimetableException(timetableExceptionEntity, from, to);
        }

        if(unitsQuantity > 0) {
            view.addMessage("basic.generateShiftTimetableException.success.timetableExceptionsGenerated", ComponentState.MessageType.SUCCESS);
        } else {
            view.addMessage("basic.generateShiftTimetableException.info.timetableExceptionsGenerated", ComponentState.MessageType.INFO);
        }

    }

    private boolean isTimetableExceptionValid(FormComponent form, Entity generateTimetableExceptionEntity) {
        boolean isValid = true;
        if (Strings.isNullOrEmpty(generateTimetableExceptionEntity.getStringField(L_REPRODUCTION_TYPE))) {
            generateTimetableExceptionEntity.addError(
                    generateTimetableExceptionEntity.getDataDefinition().getField(L_REPRODUCTION_TYPE),
                    "qcadooView.validate.field.error.missing");
            isValid = false;
        }

        if (Objects.isNull(generateTimetableExceptionEntity.getDateField(L_REPRODUCTION_TO_DATE))) {
            generateTimetableExceptionEntity.addError(
                    generateTimetableExceptionEntity.getDataDefinition().getField(L_REPRODUCTION_TO_DATE),
                    "qcadooView.validate.field.error.missing");
            isValid = false;
        }

        if(isValid) {
            Date reproductionToDate = generateTimetableExceptionEntity.getDateField(L_REPRODUCTION_TO_DATE);
            LocalDateTime reproductionToLocalDateTime = convertToLocalDateTime(reproductionToDate);

            LocalDateTime fromLocalDateTime = convertToLocalDateTime(generateTimetableExceptionEntity.getDateField(ShiftTimetableExceptionFields.FROM_DATE));

            if (reproductionToLocalDateTime.isBefore(fromLocalDateTime)) {
                generateTimetableExceptionEntity.addError(generateTimetableExceptionEntity.getDataDefinition().getField(L_REPRODUCTION_TO_DATE),
                        "basic.generateShiftTimetableException.error.reproductionDateBeforeFromDate");
                isValid = false;
            }
        }

        if (!isValid) {
            form.setEntity(generateTimetableExceptionEntity);
            form.addMessage("qcadooView.message.saveFailedMessage", ComponentState.MessageType.FAILURE);
            return true;
        }
        return false;
    }

    private void generateTimetableException(Entity timetableExceptionEntity, LocalDateTime from, LocalDateTime to) {
        Entity newTimetableExceptionEntity = timetableExceptionEntity.getDataDefinition().copy(timetableExceptionEntity.getId())
                .get(0);

        newTimetableExceptionEntity.setField(ShiftTimetableExceptionFields.FROM_DATE,
                Date.from(from.atZone(ZoneId.systemDefault()).toInstant()));

        newTimetableExceptionEntity.setField(ShiftTimetableExceptionFields.TO_DATE,
                Date.from(to.atZone(ZoneId.systemDefault()).toInstant()));

        newTimetableExceptionEntity.setField(ShiftTimetableExceptionFields.PRODUCTION_LINES,
                timetableExceptionEntity.getHasManyField(ShiftTimetableExceptionFields.PRODUCTION_LINES));
        newTimetableExceptionEntity.setField(ShiftTimetableExceptionFields.SHIFTS,
                timetableExceptionEntity.getHasManyField(ShiftTimetableExceptionFields.SHIFTS));

        newTimetableExceptionEntity.getDataDefinition().save(newTimetableExceptionEntity);
    }

    private long unitsQuantity(String reproductionType, LocalDateTime from, LocalDateTime to) {
        long unitsQuantity = 0;
        switch (reproductionType) {
            case "01everyDay":
                unitsQuantity = ChronoUnit.DAYS.between(from, to);
                break;
            case "02everyWeek":
                unitsQuantity = ChronoUnit.WEEKS.between(from, to);
                break;
            case "03everyMonth":
                unitsQuantity = ChronoUnit.MONTHS.between(from, to);
                break;
            case "04everyYear":
                unitsQuantity = ChronoUnit.YEARS.between(from, to);
                break;

        }
        return unitsQuantity;
    }

    private LocalDateTime getNextDate(String reproductionType, LocalDateTime baseDate, long multiplier) {
        switch (reproductionType) {
            case "01everyDay":
                baseDate = baseDate.plusDays(multiplier);
                break;
            case "02everyWeek":
                baseDate = baseDate.plusWeeks(multiplier);
                break;
            case "03everyMonth":
                baseDate = baseDate.plusMonths(multiplier);
                break;
            case "04everyYear":
                baseDate = baseDate.plusYears(multiplier);
                break;

        }
        return baseDate;
    }

    private LocalDateTime convertToLocalDateTime(Date dateToConvert) {
        return dateToConvert.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}
