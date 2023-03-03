package com.qcadoo.mes.orders.hooks;

import com.qcadoo.mes.orders.constants.OperationalTaskFields;
import com.qcadoo.mes.orders.constants.WorkstationChangeoverForOperationalTaskFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.Objects;

@Service
public class WorkstationChangeoverForOperationalTaskHooks {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public boolean validatesWith(final DataDefinition workstationChangeoverForOperationalTaskDD, final Entity workstationChangeoverForOperationalTask) {
        boolean isValid = true;

        isValid = isValid && validateOperationalTask(workstationChangeoverForOperationalTaskDD, workstationChangeoverForOperationalTask);
        isValid = isValid && validateDates(workstationChangeoverForOperationalTaskDD, workstationChangeoverForOperationalTask);

        return isValid;
    }

    private boolean validateOperationalTask(final DataDefinition workstationChangeoverForOperationalTaskDD, final Entity workstationChangeoverForOperationalTask) {
        Entity currentOperationalTask = workstationChangeoverForOperationalTask.getBelongsToField(WorkstationChangeoverForOperationalTaskFields.CURRENT_OPERATIONAL_TASK);
        Entity previousOperationalTask = workstationChangeoverForOperationalTask.getBelongsToField(WorkstationChangeoverForOperationalTaskFields.PREVIOUS_OPERATIONAL_TASK);

        if (Objects.isNull(currentOperationalTask) || Objects.isNull(previousOperationalTask)) {
            workstationChangeoverForOperationalTask.addError(workstationChangeoverForOperationalTaskDD.getField(WorkstationChangeoverForOperationalTaskFields.CURRENT_OPERATIONAL_TASK),
                    "qcadooView.validate.field.error.missing");

            return false;
        }

        return true;
    }

    private boolean validateDates(final DataDefinition workstationChangeoverForOperationalTaskDD, final Entity workstationChangeoverForOperationalTask) {
        Date startDate = workstationChangeoverForOperationalTask.getDateField(WorkstationChangeoverForOperationalTaskFields.START_DATE);
        Date finishDate = workstationChangeoverForOperationalTask.getDateField(WorkstationChangeoverForOperationalTaskFields.FINISH_DATE);
        Entity currentOperationalTask = workstationChangeoverForOperationalTask.getBelongsToField(WorkstationChangeoverForOperationalTaskFields.CURRENT_OPERATIONAL_TASK);
        Entity previousOperationalTask = workstationChangeoverForOperationalTask.getBelongsToField(WorkstationChangeoverForOperationalTaskFields.PREVIOUS_OPERATIONAL_TASK);

        if (Objects.nonNull(startDate) && Objects.nonNull(finishDate) && finishDate.before(startDate)) {
            workstationChangeoverForOperationalTask.addError(workstationChangeoverForOperationalTaskDD.getField(WorkstationChangeoverForOperationalTaskFields.FINISH_DATE),
                    "orders.workstationChangeoverForOperationalTask.finishDate.isBeforeStartDate");

            return false;
        }

        if (Objects.nonNull(currentOperationalTask)) {
            Date currentOperationalTaskStartDate = currentOperationalTask.getDateField(OperationalTaskFields.START_DATE);

            if (Objects.nonNull(finishDate) && Objects.nonNull(currentOperationalTaskStartDate) && finishDate.after(currentOperationalTaskStartDate)) {
                workstationChangeoverForOperationalTask.addError(workstationChangeoverForOperationalTaskDD.getField(WorkstationChangeoverForOperationalTaskFields.FINISH_DATE),
                        "orders.workstationChangeoverForOperationalTask.finishDate.isAfterCurrentStartDate");

                return false;
            }
        }

        if (Objects.nonNull(previousOperationalTask)) {
            Date previousOperationalTaskFinishDate = previousOperationalTask.getDateField(OperationalTaskFields.FINISH_DATE);

            if (Objects.nonNull(startDate) && Objects.nonNull(previousOperationalTaskFinishDate) && startDate.before(previousOperationalTaskFinishDate)) {
                workstationChangeoverForOperationalTask.addError(workstationChangeoverForOperationalTaskDD.getField(WorkstationChangeoverForOperationalTaskFields.START_DATE),
                        "orders.workstationChangeoverForOperationalTask.startDate.isBeforePreviousFinishDate");

                return false;
            }
        }

        return true;
    }

    public void onSave(final DataDefinition workstationChangeoverForOperationalTaskDD, final Entity workstationChangeoverForOperationalTask) {
        setNumber(workstationChangeoverForOperationalTask);
    }

    private void setNumber(final Entity workstationChangeoverForOperationalTask) {
        if (checkIfShouldInsertNumber(workstationChangeoverForOperationalTask)) {
            String number = jdbcTemplate.queryForObject("SELECT generate_workstation_changeover_for_operational_task_number()",
                    Collections.emptyMap(), String.class);

            workstationChangeoverForOperationalTask.setField(WorkstationChangeoverForOperationalTaskFields.NUMBER, number);
        }
    }

    private boolean checkIfShouldInsertNumber(final Entity workstationChangeoverForOperationalTask) {
        if (!Objects.isNull(workstationChangeoverForOperationalTask.getId())) {
            return false;
        }

        return !StringUtils.isNotBlank(workstationChangeoverForOperationalTask.getStringField(WorkstationChangeoverForOperationalTaskFields.NUMBER));
    }

}
