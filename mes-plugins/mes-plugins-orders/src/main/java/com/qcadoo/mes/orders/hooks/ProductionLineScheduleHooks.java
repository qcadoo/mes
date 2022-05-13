package com.qcadoo.mes.orders.hooks;

import java.util.Collections;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.newstates.StateExecutorService;
import com.qcadoo.mes.orders.constants.ProductionLineScheduleFields;
import com.qcadoo.mes.orders.states.ProductionLineScheduleServiceMarker;
import com.qcadoo.mes.orders.states.constants.ScheduleStateStringValues;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class ProductionLineScheduleHooks {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private StateExecutorService stateExecutorService;

    public void onCreate(final DataDefinition scheduleDD, final Entity schedule) {
        setInitialState(schedule);
    }

    public void onCopy(final DataDefinition scheduleDD, final Entity schedule) {
        setInitialState(schedule);
    }

    private void setInitialState(final Entity schedule) {
        stateExecutorService.buildInitial(ProductionLineScheduleServiceMarker.class, schedule, ScheduleStateStringValues.DRAFT);
    }

    public void onSave(final DataDefinition scheduleDD, final Entity schedule) {
        setScheduleNumber(schedule);
    }

    private void setScheduleNumber(final Entity schedule) {
        if (checkIfShouldInsertNumber(schedule)) {
            String number = jdbcTemplate.queryForObject("select generate_prod_line_schedule_number()", Collections.emptyMap(),
                    String.class);
            schedule.setField(ProductionLineScheduleFields.NUMBER, number);
            if (StringUtils.isBlank(schedule.getStringField(ProductionLineScheduleFields.NAME))) {
                schedule.setField(ProductionLineScheduleFields.NAME, number);
            }
        }
    }

    private boolean checkIfShouldInsertNumber(final Entity schedule) {
        if (!Objects.isNull(schedule.getId())) {
            return false;
        }
        return !StringUtils.isNotBlank(schedule.getStringField(ProductionLineScheduleFields.NUMBER));
    }
}
