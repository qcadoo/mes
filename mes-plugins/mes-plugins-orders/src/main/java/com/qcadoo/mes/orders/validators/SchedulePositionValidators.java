package com.qcadoo.mes.orders.validators;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.qcadoo.mes.orders.constants.ScheduleFields;
import com.qcadoo.mes.orders.constants.SchedulePositionFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.plugin.api.PluginManager;

@Service
public class SchedulePositionValidators {

    private static final String ORDERS_FOR_SUBPRODUCTS_GENERATION = "ordersForSubproductsGeneration";

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private PluginManager pluginManager;

    public boolean onValidate(final DataDefinition dataDefinition, final Entity schedulePosition) {
        boolean isValid = true;
        if (schedulePosition.getId() != null) {
            isValid = checkWorkstationIsCorrect(dataDefinition, schedulePosition);
            isValid = validateDates(dataDefinition, schedulePosition) && isValid;
        }
        return isValid;
    }

    private boolean checkWorkstationIsCorrect(DataDefinition dataDefinition, Entity schedulePosition) {
        Entity workstation = schedulePosition.getBelongsToField(SchedulePositionFields.WORKSTATION);
        if (workstation != null) {
            Entity technologyOperationComponent = schedulePosition
                    .getBelongsToField(SchedulePositionFields.TECHNOLOGY_OPERATION_COMPONENT);
            List<Entity> workstations = technologyOperationComponent
                    .getManyToManyField(TechnologyOperationComponentFields.WORKSTATIONS);
            if (!workstations.isEmpty() && workstations.stream().noneMatch(w -> w.getId().equals(workstation.getId()))) {
                schedulePosition.addError(dataDefinition.getField(SchedulePositionFields.WORKSTATION),
                        "orders.error.inappropriateWorkstationForPositionOperation");
                return false;
            }
        }
        return true;
    }

    private boolean validateDates(DataDefinition dataDefinition, Entity schedulePosition) {
        boolean isValid = true;
        Date startTime = schedulePosition.getDateField(SchedulePositionFields.START_TIME);
        Date endTime = schedulePosition.getDateField(SchedulePositionFields.END_TIME);
        if (endTime == null) {
            schedulePosition.addError(dataDefinition.getField(SchedulePositionFields.END_TIME),
                    "qcadooView.validate.field.error.missing");
            isValid = false;
        }
        if (startTime == null) {
            schedulePosition.addError(dataDefinition.getField(SchedulePositionFields.START_TIME),
                    "qcadooView.validate.field.error.missing");
            isValid = false;
        } else {
            isValid = validateChildrenDates(dataDefinition, schedulePosition) && isValid;
        }
        if ((startTime != null) && (endTime != null) && endTime.before(startTime)) {
            schedulePosition.addError(dataDefinition.getField(SchedulePositionFields.END_TIME),
                    "orders.validate.global.error.endTime");
            isValid = false;
        }
        return isValid;
    }

    private boolean validateChildrenDates(DataDefinition dataDefinition, Entity schedulePosition) {
        Date childEndTime = getChildrenMaxEndTime(schedulePosition);
        if (!Objects.isNull(childEndTime)
                && childEndTime.after(schedulePosition.getDateField(SchedulePositionFields.START_TIME))) {
            schedulePosition.addError(dataDefinition.getField(SchedulePositionFields.START_TIME),
                    "orders.schedulePosition.message.linkedOperationStartTimeIncorrect");
            return false;
        }
        if (pluginManager.isPluginEnabled(ORDERS_FOR_SUBPRODUCTS_GENERATION)) {
            childEndTime = getOrdersChildrenMaxEndTime(schedulePosition);
            if (!Objects.isNull(childEndTime)
                    && childEndTime.after(schedulePosition.getDateField(SchedulePositionFields.START_TIME))) {
                schedulePosition.addError(dataDefinition.getField(SchedulePositionFields.START_TIME),
                        "orders.schedulePosition.message.linkedOperationStartTimeIncorrect");
                return false;
            }
        }
        return true;
    }

    public Date getChildrenMaxEndTime(Entity position) {
        Entity schedule = position.getBelongsToField(SchedulePositionFields.SCHEDULE);
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("scheduleId", schedule.getId());
        parameters.put("orderId", position.getBelongsToField(SchedulePositionFields.ORDER).getId());
        parameters.put("tocId", position.getBelongsToField(SchedulePositionFields.TECHNOLOGY_OPERATION_COMPONENT).getId());
        StringBuilder query = new StringBuilder();
        if (!schedule.getBooleanField(ScheduleFields.ADDITIONAL_TIME_EXTENDS_OPERATION)) {
            query.append("SELECT MAX(sp.endtime + (interval '1 second' * sp.additionaltime)) ");
        } else {
            query.append("SELECT MAX(sp.endtime) ");
        }
        query.append("FROM orders_scheduleposition sp JOIN technologies_technologyoperationcomponent toc ");
        query.append("ON sp.technologyoperationcomponent_id = toc.id WHERE sp.schedule_id = :scheduleId ");
        query.append("AND sp.order_id = :orderId AND toc.parent_id = :tocId ");

        return jdbcTemplate.queryForObject(query.toString(), parameters, Timestamp.class);
    }

    public Date getOrdersChildrenMaxEndTime(Entity position) {
        Entity schedule = position.getBelongsToField(SchedulePositionFields.SCHEDULE);
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("scheduleId", schedule.getId());
        parameters.put("orderId", position.getBelongsToField(SchedulePositionFields.ORDER).getId());
        StringBuilder query = new StringBuilder();
        if (!schedule.getBooleanField(ScheduleFields.ADDITIONAL_TIME_EXTENDS_OPERATION)) {
            query.append("SELECT MAX(sp.endtime + (interval '1 second' * sp.additionaltime)) ");
        } else {
            query.append("SELECT MAX(sp.endtime) ");
        }
        query.append("FROM orders_scheduleposition sp JOIN technologies_technologyoperationcomponent toc ");
        query.append("ON sp.technologyoperationcomponent_id = toc.id JOIN orders_order o ON sp.order_id = o.id ");
        query.append("WHERE sp.schedule_id = :scheduleId AND o.parent_id = :orderId AND toc.parent_id IS NULL ");
        query.append("AND sp.endtime IS NOT NULL ");

        return jdbcTemplate.queryForObject(query.toString(), parameters, Timestamp.class);
    }
}
