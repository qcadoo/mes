package com.qcadoo.mes.orders.validators;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.ParameterFieldsO;
import com.qcadoo.mes.orders.constants.ProductionLineScheduleFields;
import com.qcadoo.mes.orders.constants.ProductionLineSchedulePositionFields;
import com.qcadoo.mes.orders.states.constants.OrderStateStringValues;
import com.qcadoo.mes.productionLines.constants.ProductionLineFields;
import com.qcadoo.mes.productionLines.constants.ProductionLinesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologyProductionLineFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.plugin.api.PluginManager;

@Service
public class ProductionLineSchedulePositionValidators {

    private static final String ORDERS_FOR_SUBPRODUCTS_GENERATION = "ordersForSubproductsGeneration";

    public static final String SCHEDULE_ID = "scheduleId";

    public static final String ORDER_ID = "orderId";

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private PluginManager pluginManager;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ParameterService parameterService;

    public boolean onValidate(final DataDefinition dataDefinition, final Entity schedulePosition) {
        boolean isValid = true;
        if (schedulePosition.getId() != null) {
            isValid = checkProductionLineIsCorrect(dataDefinition, schedulePosition);
            isValid = validateDates(dataDefinition, schedulePosition) && isValid;
        }
        return isValid;
    }

    private boolean checkProductionLineIsCorrect(DataDefinition dataDefinition, Entity schedulePosition) {
        Entity productionLine = schedulePosition.getBelongsToField(ProductionLineSchedulePositionFields.PRODUCTION_LINE);
        if (productionLine != null) {
            boolean allowProductionLineChange = schedulePosition.getBelongsToField(ProductionLineSchedulePositionFields.PRODUCTION_LINE_SCHEDULE).getBooleanField(ProductionLineScheduleFields.ALLOW_PRODUCTION_LINE_CHANGE);
            boolean canChangeProdLineForAcceptedOrders = parameterService.getParameter().getBooleanField(ParameterFieldsO.CAN_CHANGE_PROD_LINE_FOR_ACCEPTED_ORDERS);
            List<Entity> productionLines = dataDefinitionService
                    .get(ProductionLinesConstants.PLUGIN_IDENTIFIER, ProductionLinesConstants.MODEL_PRODUCTION_LINE).find()
                    .add(SearchRestrictions.eq(ProductionLineFields.PRODUCTION, true)).list().getEntities();
            List<Entity> orderProductionLines = getProductionLinesFromTechnology(schedulePosition, productionLines,
                    allowProductionLineChange, canChangeProdLineForAcceptedOrders);
            if (!orderProductionLines.isEmpty() && orderProductionLines.stream().noneMatch(pl -> pl.getId().equals(productionLine.getId()))) {
                schedulePosition.addError(dataDefinition.getField(ProductionLineSchedulePositionFields.PRODUCTION_LINE),
                        "orders.error.inappropriateProductionLineForPositionOrder");
                return false;
            }
        }
        return true;
    }

    public List<Entity> getProductionLinesFromTechnology(Entity position, List<Entity> productionLines, boolean allowProductionLineChange, boolean canChangeProdLineForAcceptedOrders) {
        Entity order = position.getBelongsToField(ProductionLineSchedulePositionFields.ORDER);
        String orderState = order.getStringField(OrderFields.STATE);
        Entity orderProductionLine = order.getBelongsToField(OrderFields.PRODUCTION_LINE);
        if (orderProductionLine != null && (!allowProductionLineChange || orderState.equals(OrderStateStringValues.ACCEPTED) && !canChangeProdLineForAcceptedOrders)) {
            return Collections.singletonList(orderProductionLine);
        }
        List<Entity> technologyProductionLines = order
                .getBelongsToField(OrderFields.TECHNOLOGY).getHasManyField(TechnologyFields.PRODUCTION_LINES);
        if (technologyProductionLines.isEmpty()) {
            return productionLines;
        } else {
            return technologyProductionLines.stream().map(e -> e.getBelongsToField(TechnologyProductionLineFields.PRODUCTION_LINE)).collect(Collectors.toList());
        }
    }

    private boolean validateDates(DataDefinition dataDefinition, Entity schedulePosition) {
        boolean isValid;
        Date startTime = schedulePosition.getDateField(ProductionLineSchedulePositionFields.START_TIME);
        Date endTime = schedulePosition.getDateField(ProductionLineSchedulePositionFields.END_TIME);
        if (startTime == null) {
            schedulePosition.addError(dataDefinition.getField(ProductionLineSchedulePositionFields.START_TIME),
                    "qcadooView.validate.field.error.missing");
            isValid = false;
        } else {
            isValid = validateParentsStartDates(dataDefinition, schedulePosition);
            isValid = validateChildrenStartDates(dataDefinition, schedulePosition) && isValid;
        }
        if (endTime == null) {
            schedulePosition.addError(dataDefinition.getField(ProductionLineSchedulePositionFields.END_TIME),
                    "qcadooView.validate.field.error.missing");
            isValid = false;
        } else {
            isValid = validateParentsEndDates(dataDefinition, schedulePosition) && isValid;
            isValid = validateChildrenEndDates(dataDefinition, schedulePosition) && isValid;
        }
        if ((startTime != null) && (endTime != null) && endTime.before(startTime)) {
            schedulePosition.addError(dataDefinition.getField(ProductionLineSchedulePositionFields.END_TIME),
                    "orders.validate.global.error.endTime");
            isValid = false;
        }
        return isValid;
    }

    private boolean validateParentsStartDates(DataDefinition dataDefinition, Entity schedulePosition) {
        if (pluginManager.isPluginEnabled(ORDERS_FOR_SUBPRODUCTS_GENERATION)
                && schedulePosition.getBelongsToField(ProductionLineSchedulePositionFields.ORDER)
                .getBelongsToField(TechnologyOperationComponentFields.PARENT) != null) {
            Date parentStartTime = getOrdersParentsMinStartTime(schedulePosition);
            if (!Objects.isNull(parentStartTime)
                    && parentStartTime.before(schedulePosition.getDateField(ProductionLineSchedulePositionFields.START_TIME))) {
                schedulePosition.addError(dataDefinition.getField(ProductionLineSchedulePositionFields.START_TIME),
                        "orders.schedulePosition.error.inappropriateStartDateNext");
                return false;
            }
        }
        return true;
    }

    private boolean validateParentsEndDates(DataDefinition dataDefinition, Entity schedulePosition) {
        if (pluginManager.isPluginEnabled(ORDERS_FOR_SUBPRODUCTS_GENERATION)
                && schedulePosition.getBelongsToField(ProductionLineSchedulePositionFields.ORDER)
                .getBelongsToField(TechnologyOperationComponentFields.PARENT) != null) {
            Date parentEndTime = getOrdersParentsMinEndTime(schedulePosition);
            if (!Objects.isNull(parentEndTime)
                    && parentEndTime.before(schedulePosition.getDateField(ProductionLineSchedulePositionFields.END_TIME))) {
                schedulePosition.addError(dataDefinition.getField(ProductionLineSchedulePositionFields.END_TIME),
                        "orders.schedulePosition.error.inappropriateFinishDateNext");
                return false;
            }
        }
        return true;
    }

    private boolean validateChildrenStartDates(DataDefinition dataDefinition, Entity schedulePosition) {
        if (pluginManager.isPluginEnabled(ORDERS_FOR_SUBPRODUCTS_GENERATION)) {
            Date childStartTime = getOrdersChildrenMaxStartTime(schedulePosition);
            if (!Objects.isNull(childStartTime)
                    && childStartTime.after(schedulePosition.getDateField(ProductionLineSchedulePositionFields.START_TIME))) {
                schedulePosition.addError(dataDefinition.getField(ProductionLineSchedulePositionFields.START_TIME),
                        "orders.schedulePosition.error.inappropriateStartDatePrevious");
                return false;
            }
        }
        return true;
    }

    private boolean validateChildrenEndDates(DataDefinition dataDefinition, Entity schedulePosition) {
        if (pluginManager.isPluginEnabled(ORDERS_FOR_SUBPRODUCTS_GENERATION)) {
            Date childEndTime = getOrdersChildrenMaxEndTime(schedulePosition);
            if (!Objects.isNull(childEndTime)
                    && childEndTime.after(schedulePosition.getDateField(ProductionLineSchedulePositionFields.END_TIME))) {
                schedulePosition.addError(dataDefinition.getField(ProductionLineSchedulePositionFields.END_TIME),
                        "orders.schedulePosition.error.inappropriateFinishDatePrevious");
                return false;
            }
        }
        return true;
    }

    public Date getOrdersChildrenMaxEndTime(Entity position) {
        Entity schedule = position.getBelongsToField(ProductionLineSchedulePositionFields.PRODUCTION_LINE_SCHEDULE);
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put(SCHEDULE_ID, schedule.getId());
        parameters.put(ORDER_ID, position.getBelongsToField(ProductionLineSchedulePositionFields.ORDER).getId());
        String query = "SELECT MAX(sp.endtime + (interval '1 second' * sp.additionaltime)) " +
                "FROM orders_productionlinescheduleposition sp JOIN orders_order o ON sp.order_id = o.id " +
                "WHERE sp.productionlineschedule_id = :scheduleId AND o.parent_id = :orderId AND sp.endtime IS NOT NULL ";

        return jdbcTemplate.queryForObject(query, parameters, Timestamp.class);
    }

    private Date getOrdersChildrenMaxStartTime(Entity position) {
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put(SCHEDULE_ID, position.getBelongsToField(ProductionLineSchedulePositionFields.PRODUCTION_LINE_SCHEDULE).getId());
        parameters.put(ORDER_ID, position.getBelongsToField(ProductionLineSchedulePositionFields.ORDER).getId());
        String query = "SELECT MAX(sp.starttime) FROM orders_productionlinescheduleposition sp "
                + "JOIN orders_order o ON sp.order_id = o.id "
                + "WHERE sp.productionlineschedule_id = :scheduleId AND o.parent_id = :orderId "
                + "AND sp.starttime IS NOT NULL ";

        return jdbcTemplate.queryForObject(query, parameters, Timestamp.class);
    }

    private Date getOrdersParentsMinEndTime(Entity position) {
        Entity schedule = position.getBelongsToField(ProductionLineSchedulePositionFields.PRODUCTION_LINE_SCHEDULE);
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put(SCHEDULE_ID, schedule.getId());
        parameters.put(ORDER_ID, position.getBelongsToField(ProductionLineSchedulePositionFields.ORDER)
                .getBelongsToField(TechnologyOperationComponentFields.PARENT).getId());
        String query = "SELECT MAX(sp.endtime + (interval '1 second' * sp.additionaltime)) " +
                "FROM orders_productionlinescheduleposition sp " +
                "WHERE sp.productionlineschedule_id = :scheduleId AND sp.order_id = :orderId " +
                "AND sp.endtime IS NOT NULL ";

        return jdbcTemplate.queryForObject(query, parameters, Timestamp.class);
    }

    private Date getOrdersParentsMinStartTime(Entity position) {
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put(SCHEDULE_ID, position.getBelongsToField(ProductionLineSchedulePositionFields.PRODUCTION_LINE_SCHEDULE).getId());
        parameters.put(ORDER_ID, position.getBelongsToField(ProductionLineSchedulePositionFields.ORDER)
                .getBelongsToField(TechnologyOperationComponentFields.PARENT).getId());
        String query = "SELECT MIN(sp.starttime) FROM orders_productionlinescheduleposition sp "
                + "WHERE sp.productionlineschedule_id = :scheduleId AND sp.order_id = :orderId "
                + "AND sp.starttime IS NOT NULL ";

        return jdbcTemplate.queryForObject(query, parameters, Timestamp.class);
    }
}
