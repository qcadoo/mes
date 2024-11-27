package com.qcadoo.mes.orders.validators;

import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.constants.WorkstationFields;
import com.qcadoo.mes.basic.constants.WorkstationTypeFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.constants.ScheduleFields;
import com.qcadoo.mes.orders.constants.SchedulePositionFields;
import com.qcadoo.mes.productionLines.constants.WorkstationFieldsPL;
import com.qcadoo.mes.technologies.constants.AssignedToOperation;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.plugin.api.PluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SchedulePositionValidators {

    private static final String ORDERS_FOR_SUBPRODUCTS_GENERATION = "ordersForSubproductsGeneration";

    public static final String SCHEDULE_ID = "scheduleId";

    public static final String ORDER_ID = "orderId";

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private PluginManager pluginManager;

    @Autowired
    private DataDefinitionService dataDefinitionService;

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
            Entity schedule = schedulePosition.getBelongsToField(SchedulePositionFields.SCHEDULE);
            Entity order = schedulePosition.getBelongsToField(SchedulePositionFields.ORDER);
            List<Entity> workstations = getWorkstationsFromTOC(schedule, schedulePosition.getBelongsToField(SchedulePositionFields.TECHNOLOGY_OPERATION_COMPONENT), order);

            if (workstations.stream().noneMatch(w -> w.getId().equals(workstation.getId()))) {
                schedulePosition.addError(dataDefinition.getField(SchedulePositionFields.WORKSTATION),
                        "orders.error.inappropriateWorkstationForPositionOperation");
                return false;
            }
        }
        return true;
    }

    public List<Entity> getWorkstationsFromTOC(Entity schedule, Entity technologyOperationComponent, Entity order) {
        List<Entity> workstations;
        if (AssignedToOperation.WORKSTATIONS.getStringValue()
                .equals(technologyOperationComponent.getStringField(TechnologyOperationComponentFields.ASSIGNED_TO_OPERATION))) {
            workstations = technologyOperationComponent.getManyToManyField(TechnologyOperationComponentFields.WORKSTATIONS);
        } else {
            Entity workstationType = technologyOperationComponent
                    .getBelongsToField(TechnologyOperationComponentFields.WORKSTATION_TYPE);
            if (workstationType == null) {
                workstations = Collections.emptyList();
            } else {
                workstations = workstationType.getHasManyField(WorkstationTypeFields.WORKSTATIONS);
            }
        }
        boolean onlyWorkstationsOfLineFromOrder = schedule.getBooleanField(ScheduleFields.ONLY_WORKSTATIONS_OF_LINE_FROM_ORDER);
        if (onlyWorkstationsOfLineFromOrder) {
            Entity productionLine = order.getBelongsToField(OrderFields.PRODUCTION_LINE);
            if (productionLine != null) {
                workstations = workstations.stream().filter(e -> e.getBelongsToField(WorkstationFieldsPL.PRODUCTION_LINE) != null && productionLine.getId().equals(e.getBelongsToField(WorkstationFieldsPL.PRODUCTION_LINE).getId()))
                        .collect(Collectors.toList());
            }
        }
        return workstations.stream().filter(Entity::isActive).collect(Collectors.toList());
    }

    private boolean validateDates(DataDefinition dataDefinition, Entity schedulePosition) {
        boolean isValid;
        Date startTime = schedulePosition.getDateField(SchedulePositionFields.START_TIME);
        Date endTime = schedulePosition.getDateField(SchedulePositionFields.END_TIME);
        if (startTime == null) {
            schedulePosition.addError(dataDefinition.getField(SchedulePositionFields.START_TIME),
                    "qcadooView.validate.field.error.missing");
            isValid = false;
        } else {
            isValid = validateParentsStartDates(dataDefinition, schedulePosition);
            isValid = validateChildrenStartDates(dataDefinition, schedulePosition) && isValid;
        }
        if (endTime == null) {
            schedulePosition.addError(dataDefinition.getField(SchedulePositionFields.END_TIME),
                    "qcadooView.validate.field.error.missing");
            isValid = false;
        } else {
            isValid = validateParentsEndDates(dataDefinition, schedulePosition) && isValid;
            isValid = validateChildrenEndDates(dataDefinition, schedulePosition) && isValid;
        }
        if ((startTime != null) && (endTime != null) && endTime.before(startTime)) {
            schedulePosition.addError(dataDefinition.getField(SchedulePositionFields.END_TIME),
                    "orders.validate.global.error.endTime");
            isValid = false;
        }
        return isValid;
    }

    private boolean validateParentsStartDates(DataDefinition dataDefinition, Entity schedulePosition) {
        Entity parent = getParent(schedulePosition);
        if (parent != null && !Objects.isNull(parent.getDateField(SchedulePositionFields.START_TIME))
                && parent.getDateField(SchedulePositionFields.START_TIME)
                .before(schedulePosition.getDateField(SchedulePositionFields.START_TIME))) {
            schedulePosition.addError(dataDefinition.getField(SchedulePositionFields.START_TIME),
                    "orders.schedulePosition.error.inappropriateStartDateNext");
            return false;
        }
        if (pluginManager.isPluginEnabled(ORDERS_FOR_SUBPRODUCTS_GENERATION)
                && schedulePosition.getBelongsToField(SchedulePositionFields.TECHNOLOGY_OPERATION_COMPONENT)
                .getBelongsToField(TechnologyOperationComponentFields.PARENT) == null
                && schedulePosition.getBelongsToField(SchedulePositionFields.ORDER)
                .getBelongsToField(TechnologyOperationComponentFields.PARENT) != null) {
            Date parentStartTime = getOrdersParentsMinStartTime(schedulePosition);
            if (!Objects.isNull(parentStartTime)
                    && parentStartTime.before(schedulePosition.getDateField(SchedulePositionFields.START_TIME))) {
                schedulePosition.addError(dataDefinition.getField(SchedulePositionFields.START_TIME),
                        "orders.schedulePosition.error.inappropriateStartDateNext");
                return false;
            }
        }
        return true;
    }

    private boolean validateParentsEndDates(DataDefinition dataDefinition, Entity schedulePosition) {
        Entity parent = getParent(schedulePosition);
        if (parent != null && !Objects.isNull(parent.getDateField(SchedulePositionFields.END_TIME))
                && parent.getDateField(SchedulePositionFields.END_TIME)
                .before(schedulePosition.getDateField(SchedulePositionFields.END_TIME))) {
            schedulePosition.addError(dataDefinition.getField(SchedulePositionFields.END_TIME),
                    "orders.schedulePosition.error.inappropriateFinishDateNext");
            return false;
        }
        if (pluginManager.isPluginEnabled(ORDERS_FOR_SUBPRODUCTS_GENERATION)
                && schedulePosition.getBelongsToField(SchedulePositionFields.TECHNOLOGY_OPERATION_COMPONENT)
                .getBelongsToField(TechnologyOperationComponentFields.PARENT) == null
                && schedulePosition.getBelongsToField(SchedulePositionFields.ORDER)
                .getBelongsToField(TechnologyOperationComponentFields.PARENT) != null) {
            Date parentEndTime = getOrdersParentsMinEndTime(schedulePosition);
            if (!Objects.isNull(parentEndTime)
                    && parentEndTime.before(schedulePosition.getDateField(SchedulePositionFields.END_TIME))) {
                schedulePosition.addError(dataDefinition.getField(SchedulePositionFields.END_TIME),
                        "orders.schedulePosition.error.inappropriateFinishDateNext");
                return false;
            }
        }
        return true;
    }

    private Entity getParent(Entity position) {
        if (position.getBelongsToField(SchedulePositionFields.TECHNOLOGY_OPERATION_COMPONENT)
                .getBelongsToField(TechnologyOperationComponentFields.PARENT) != null) {
            return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_SCHEDULE_POSITION).find()
                    .add(SearchRestrictions.belongsTo(SchedulePositionFields.TECHNOLOGY_OPERATION_COMPONENT,
                            position.getBelongsToField(SchedulePositionFields.TECHNOLOGY_OPERATION_COMPONENT)
                                    .getBelongsToField(TechnologyOperationComponentFields.PARENT)))
                    .add(SearchRestrictions.belongsTo(SchedulePositionFields.ORDER,
                            position.getBelongsToField(SchedulePositionFields.ORDER)))
                    .add(SearchRestrictions.belongsTo(SchedulePositionFields.SCHEDULE,
                            position.getBelongsToField(SchedulePositionFields.SCHEDULE)))
                    .uniqueResult();
        }
        return null;
    }

    private boolean validateChildrenStartDates(DataDefinition dataDefinition, Entity schedulePosition) {
        Date childStartTime = getChildrenMaxStartTime(schedulePosition);
        if (!Objects.isNull(childStartTime)
                && childStartTime.after(schedulePosition.getDateField(SchedulePositionFields.START_TIME))) {
            schedulePosition.addError(dataDefinition.getField(SchedulePositionFields.START_TIME),
                    "orders.schedulePosition.error.inappropriateStartDatePrevious");
            return false;
        }
        if (pluginManager.isPluginEnabled(ORDERS_FOR_SUBPRODUCTS_GENERATION)) {
            childStartTime = getOrdersChildrenMaxStartTime(schedulePosition);
            if (!Objects.isNull(childStartTime)
                    && childStartTime.after(schedulePosition.getDateField(SchedulePositionFields.START_TIME))) {
                schedulePosition.addError(dataDefinition.getField(SchedulePositionFields.START_TIME),
                        "orders.schedulePosition.error.inappropriateStartDatePrevious");
                return false;
            }
        }
        return true;
    }

    private boolean validateChildrenEndDates(DataDefinition dataDefinition, Entity schedulePosition) {
        Date childEndTime = getChildrenMaxEndTime(schedulePosition);
        if (!Objects.isNull(childEndTime) && childEndTime.after(schedulePosition.getDateField(SchedulePositionFields.END_TIME))) {
            schedulePosition.addError(dataDefinition.getField(SchedulePositionFields.END_TIME),
                    "orders.schedulePosition.error.inappropriateFinishDatePrevious");
            return false;
        }
        if (pluginManager.isPluginEnabled(ORDERS_FOR_SUBPRODUCTS_GENERATION)) {
            childEndTime = getOrdersChildrenMaxEndTime(schedulePosition);
            if (!Objects.isNull(childEndTime)
                    && childEndTime.after(schedulePosition.getDateField(SchedulePositionFields.END_TIME))) {
                schedulePosition.addError(dataDefinition.getField(SchedulePositionFields.END_TIME),
                        "orders.schedulePosition.error.inappropriateFinishDatePrevious");
                return false;
            }
        }
        return true;
    }

    public Date getChildrenMaxEndTime(Entity position) {
        Entity schedule = position.getBelongsToField(SchedulePositionFields.SCHEDULE);
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put(SCHEDULE_ID, schedule.getId());
        parameters.put(ORDER_ID, position.getBelongsToField(SchedulePositionFields.ORDER).getId());
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

    private Date getChildrenMaxStartTime(Entity position) {
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put(SCHEDULE_ID, position.getBelongsToField(SchedulePositionFields.SCHEDULE).getId());
        parameters.put(ORDER_ID, position.getBelongsToField(SchedulePositionFields.ORDER).getId());
        parameters.put("tocId", position.getBelongsToField(SchedulePositionFields.TECHNOLOGY_OPERATION_COMPONENT).getId());
        String query = "SELECT MAX(sp.starttime) FROM orders_scheduleposition sp "
                + "JOIN technologies_technologyoperationcomponent toc ON sp.technologyoperationcomponent_id = toc.id "
                + "WHERE sp.schedule_id = :scheduleId AND sp.order_id = :orderId AND toc.parent_id = :tocId ";

        return jdbcTemplate.queryForObject(query, parameters, Timestamp.class);
    }

    public Date getOrdersChildrenMaxEndTime(Entity position) {
        Entity schedule = position.getBelongsToField(SchedulePositionFields.SCHEDULE);
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put(SCHEDULE_ID, schedule.getId());
        parameters.put(ORDER_ID, position.getBelongsToField(SchedulePositionFields.ORDER).getId());
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

    private Date getOrdersChildrenMaxStartTime(Entity position) {
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put(SCHEDULE_ID, position.getBelongsToField(SchedulePositionFields.SCHEDULE).getId());
        parameters.put(ORDER_ID, position.getBelongsToField(SchedulePositionFields.ORDER).getId());
        String query = "SELECT MAX(sp.starttime) FROM orders_scheduleposition sp "
                + "JOIN technologies_technologyoperationcomponent toc ON sp.technologyoperationcomponent_id = toc.id "
                + "JOIN orders_order o ON sp.order_id = o.id "
                + "WHERE sp.schedule_id = :scheduleId AND o.parent_id = :orderId AND toc.parent_id IS NULL "
                + "AND sp.starttime IS NOT NULL ";

        return jdbcTemplate.queryForObject(query, parameters, Timestamp.class);
    }

    private Date getOrdersParentsMinEndTime(Entity position) {
        Entity schedule = position.getBelongsToField(SchedulePositionFields.SCHEDULE);
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put(SCHEDULE_ID, schedule.getId());
        parameters.put(ORDER_ID, position.getBelongsToField(SchedulePositionFields.ORDER)
                .getBelongsToField(TechnologyOperationComponentFields.PARENT).getId());
        StringBuilder query = new StringBuilder();
        if (!schedule.getBooleanField(ScheduleFields.ADDITIONAL_TIME_EXTENDS_OPERATION)) {
            query.append("SELECT MIN(sp.endtime + (interval '1 second' * sp.additionaltime)) ");
        } else {
            query.append("SELECT MIN(sp.endtime) ");
        }
        query.append("FROM orders_scheduleposition sp ");
        query.append("WHERE sp.schedule_id = :scheduleId AND sp.order_id = :orderId ");
        query.append("AND sp.endtime IS NOT NULL ");

        return jdbcTemplate.queryForObject(query.toString(), parameters, Timestamp.class);
    }

    private Date getOrdersParentsMinStartTime(Entity position) {
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put(SCHEDULE_ID, position.getBelongsToField(SchedulePositionFields.SCHEDULE).getId());
        parameters.put(ORDER_ID, position.getBelongsToField(SchedulePositionFields.ORDER)
                .getBelongsToField(TechnologyOperationComponentFields.PARENT).getId());
        String query = "SELECT MIN(sp.starttime) " + "FROM orders_scheduleposition sp "
                + "WHERE sp.schedule_id = :scheduleId AND sp.order_id = :orderId " + "AND sp.starttime IS NOT NULL ";

        return jdbcTemplate.queryForObject(query, parameters, Timestamp.class);
    }
}
