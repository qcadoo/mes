package com.qcadoo.mes.orders.services;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.constants.AttributeDataType;
import com.qcadoo.mes.basic.constants.AttributeFields;
import com.qcadoo.mes.basic.constants.ProductAttributeValueFields;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.orders.constants.*;
import com.qcadoo.mes.orders.states.constants.OperationalTaskState;
import com.qcadoo.mes.orders.states.constants.OperationalTaskStateStringValues;
import com.qcadoo.mes.technologies.constants.WorkstationChangeoverNormChangeoverType;
import com.qcadoo.mes.technologies.constants.WorkstationChangeoverNormFields;
import com.qcadoo.mes.technologies.services.WorkstationChangeoverNormService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.*;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WorkstationChangeoverService {

    private static final String L_DOT = ".";

    private static final String L_ID = "id";

    private static final String L_COUNT = "count";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private WorkstationChangeoverNormService workstationChangeoverNormService;

    public List<Entity> findWorkstationChangeoversForSchedulePosition(final Date startDate, final Entity workstation,
                                                                      final Entity schedulePosition,
                                                                      final Entity previousSchedulePosition) {
        List<Entity> workstationChangeovers = Lists.newArrayList();

        if (workstationChangeoverNormService.hasWorkstationChangeoverNorms(workstation)) {
            Entity previousOperationalTask = null;
            Entity previousProduct = null;
            if (previousSchedulePosition != null) {
                previousProduct = previousSchedulePosition.getBelongsToField(SchedulePositionFields.ORDER).getBelongsToField(OrderFields.PRODUCT);
            } else {
                previousOperationalTask = getPreviousOperationalTask(workstation, startDate);
                if (previousOperationalTask != null) {
                    previousProduct = previousOperationalTask.getBelongsToField(OperationalTaskFields.ORDER).getBelongsToField(OrderFields.PRODUCT);
                }
            }
            if (previousProduct != null) {
                Entity currentProduct = schedulePosition.getBelongsToField(SchedulePositionFields.ORDER).getBelongsToField(OrderFields.PRODUCT);

                List<Entity> currentProductAttributeValues = getProductAttributeValuesWithDataTypeCalculated(currentProduct);
                List<Entity> previousProductAttributeValues = getProductAttributeValuesWithDataTypeCalculated(previousProduct);

                for (Entity currentProductAttributeValue : currentProductAttributeValues) {
                    createChangeoversForAttribute(workstation, schedulePosition, previousSchedulePosition, workstationChangeovers,
                            previousOperationalTask, previousProductAttributeValues, currentProductAttributeValue);
                }
            }

            updateWorkstationChangeoversForSchedulePositionDates(workstationChangeovers, startDate);
        }

        return workstationChangeovers;
    }

    private void createChangeoversForAttribute(Entity workstation, Entity schedulePosition,
                                               Entity previousSchedulePosition,
                                               List<Entity> workstationChangeovers, Entity previousOperationalTask,
                                               List<Entity> previousProductAttributeValues,
                                               Entity currentProductAttributeValue) {
        Entity attribute = currentProductAttributeValue.getBelongsToField(ProductAttributeValueFields.ATTRIBUTE);
        Entity attributeValue = currentProductAttributeValue.getBelongsToField(ProductAttributeValueFields.ATTRIBUTE_VALUE);

        List<Entity> filteredProductAttributeValues = previousProductAttributeValues.stream().filter(previousProductAttributeValue ->
                filterProductAttributeValuesWithAttribute(previousProductAttributeValue, attribute)).collect(Collectors.toList());
        if (!filteredProductAttributeValues.isEmpty()) {
            List<Entity> workstationChangeoverNorms = workstationChangeoverNormService.findWorkstationChangeoverNorms(workstation, attribute);

            for (Entity workstationChangeoverNorm : workstationChangeoverNorms) {
                String changeoverType = workstationChangeoverNorm.getStringField(WorkstationChangeoverNormFields.CHANGEOVER_TYPE);

                if (WorkstationChangeoverNormChangeoverType.BETWEEN_VALUES.getStringValue().equals(changeoverType)) {
                    Entity fromAttributeValue = workstationChangeoverNorm.getBelongsToField(WorkstationChangeoverNormFields.FROM_ATTRIBUTE_VALUE);
                    Entity toAttributeValue = workstationChangeoverNorm.getBelongsToField(WorkstationChangeoverNormFields.TO_ATTRIBUTE_VALUE);

                    if (attributeValue.getId().equals(toAttributeValue.getId()) && filteredProductAttributeValues.stream().anyMatch(previousProductAttributeValue ->
                            filterProductAttributeValuesWithAttributeValue(previousProductAttributeValue, fromAttributeValue))) {
                        Entity workstationChangeover = createWorkstationChangeoverForSchedulePosition(schedulePosition, previousSchedulePosition, workstationChangeoverNorm, previousOperationalTask);

                        workstationChangeovers.add(workstationChangeover);
                    }
                } else {
                    Entity workstationChangeover = createWorkstationChangeoverForSchedulePosition(schedulePosition, previousSchedulePosition, workstationChangeoverNorm, previousOperationalTask);

                    workstationChangeovers.add(workstationChangeover);
                }
            }
        }
    }

    public Optional<Date> getWorkstationChangeoversMaxFinishDate(final List<Entity> workstationChangeovers) {
        return workstationChangeovers.stream().map(workstationChangeover ->
                        workstationChangeover.getDateField(WorkstationChangeoverForOperationalTaskFields.FINISH_DATE))
                .max(Date::compareTo);
    }

    private Entity createWorkstationChangeoverForSchedulePosition(final Entity currentSchedulePosition,
                                                                  final Entity previousSchedulePosition,
                                                                  final Entity workstationChangeoverNorm,
                                                                  final Entity previousOperationalTask) {
        Entity workstationChangeoverForSchedulePosition = getWorkstationChangeoverForSchedulePositionDD().create();

        workstationChangeoverForSchedulePosition.setField(WorkstationChangeoverForSchedulePositionFields.WORKSTATION_CHANGEOVER_NORM, workstationChangeoverNorm);
        workstationChangeoverForSchedulePosition.setField(WorkstationChangeoverForSchedulePositionFields.CURRENT_SCHEDULE_POSITION, currentSchedulePosition);
        workstationChangeoverForSchedulePosition.setField(WorkstationChangeoverForSchedulePositionFields.PREVIOUS_SCHEDULE_POSITION, previousSchedulePosition);
        workstationChangeoverForSchedulePosition.setField(WorkstationChangeoverForSchedulePositionFields.PREVIOUS_OPERATIONAL_TASK, previousOperationalTask);

        return workstationChangeoverForSchedulePosition;
    }

    public List<Entity> findWorkstationChangeoverForOperationalTasks(final Entity operationalTask) {
        List<Entity> workstationChangeoverForOperationalTasks = Lists.newArrayList();

        Optional<Entity> mayBePreviousOperationalTask = findPreviousOperationalTask(operationalTask);

        if (mayBePreviousOperationalTask.isPresent()) {
            Entity previousOperationalTask = mayBePreviousOperationalTask.get();

            workstationChangeoverForOperationalTasks = findWorkstationChangeoverForOperationalTasks(operationalTask, previousOperationalTask);
        }

        return workstationChangeoverForOperationalTasks;
    }

    public List<Entity> findWorkstationChangeoverForOperationalTasks(final Entity operationalTask,
                                                                     final Entity previousOperationalTask) {
        List<Entity> workstationChangeoverForOperationalTasks = Lists.newArrayList();

        Entity workstation = operationalTask.getBelongsToField(OperationalTaskFields.WORKSTATION);

        if (workstationChangeoverNormService.hasWorkstationChangeoverNorms(workstation)) {
            Entity currentOperationalTaskOrder = operationalTask.getBelongsToField(OperationalTaskFields.ORDER);
            Entity currentOperationalTaskOrderProduct = currentOperationalTaskOrder.getBelongsToField(OrderFields.PRODUCT);

            Entity previousOperationalTaskOrder = previousOperationalTask.getBelongsToField(OperationalTaskFields.ORDER);
            Entity previousOperationalTaskOrderProduct = previousOperationalTaskOrder.getBelongsToField(OrderFields.PRODUCT);

            List<Entity> currentProductAttributeValues = getProductAttributeValuesWithDataTypeCalculated(currentOperationalTaskOrderProduct);
            List<Entity> previousProductAttributeValues = getProductAttributeValuesWithDataTypeCalculated(previousOperationalTaskOrderProduct);

            currentProductAttributeValues.forEach(currentProductAttributeValue -> {
                Entity attribute = currentProductAttributeValue.getBelongsToField(ProductAttributeValueFields.ATTRIBUTE);
                Entity attributeValue = currentProductAttributeValue.getBelongsToField(ProductAttributeValueFields.ATTRIBUTE_VALUE);

                List<Entity> filteredProductAttributeValues = previousProductAttributeValues.stream().filter(previousProductAttributeValue ->
                        filterProductAttributeValuesWithAttribute(previousProductAttributeValue, attribute)).collect(Collectors.toList());

                if (!filteredProductAttributeValues.isEmpty()) {
                    List<Entity> workstationChangeoverNorms = workstationChangeoverNormService.findWorkstationChangeoverNorms(workstation, attribute);

                    workstationChangeoverNorms.forEach(workstationChangeoverNorm -> {
                        String changeoverType = workstationChangeoverNorm.getStringField(WorkstationChangeoverNormFields.CHANGEOVER_TYPE);

                        if (WorkstationChangeoverNormChangeoverType.BETWEEN_VALUES.getStringValue().equals(changeoverType)) {
                            Entity fromAttributeValue = workstationChangeoverNorm.getBelongsToField(WorkstationChangeoverNormFields.FROM_ATTRIBUTE_VALUE);
                            Entity toAttributeValue = workstationChangeoverNorm.getBelongsToField(WorkstationChangeoverNormFields.TO_ATTRIBUTE_VALUE);

                            if (attributeValue.getId().equals(toAttributeValue.getId())) {
                                if (filteredProductAttributeValues.stream().anyMatch(previousProductAttributeValue ->
                                        filterProductAttributeValuesWithAttributeValue(previousProductAttributeValue, fromAttributeValue))) {
                                    Entity workstationChangeoverForOperationalTask = createWorkstationChangeoverForOperationalTask(operationalTask, previousOperationalTask, workstationChangeoverNorm, workstation, attribute, fromAttributeValue, toAttributeValue);

                                    workstationChangeoverForOperationalTasks.add(workstationChangeoverForOperationalTask);
                                }
                            }
                        } else {
                            Optional<Entity> mayBeProductAttributeValue = filteredProductAttributeValues.stream().findFirst();

                            Entity fromAttributeValue = null;

                            if (mayBeProductAttributeValue.isPresent()) {
                                fromAttributeValue = mayBeProductAttributeValue.get().getBelongsToField(ProductAttributeValueFields.ATTRIBUTE_VALUE);
                            }

                            if (fromAttributeValue != null && !attributeValue.getId().equals(fromAttributeValue.getId())) {
                                Entity workstationChangeoverForOperationalTask = createWorkstationChangeoverForOperationalTask(operationalTask, previousOperationalTask, workstationChangeoverNorm, workstation, attribute, fromAttributeValue, attributeValue);

                                workstationChangeoverForOperationalTasks.add(workstationChangeoverForOperationalTask);
                            }
                        }
                    });
                }
            });

            updateWorkstationChangeoverForOperationalTasksDates(workstationChangeoverForOperationalTasks,
                    operationalTask.getDateField(OperationalTaskFields.START_DATE),
                    previousOperationalTask.getDateField(OperationalTaskFields.FINISH_DATE));
        }

        return workstationChangeoverForOperationalTasks;
    }

    private void updateWorkstationChangeoverForOperationalTasksDates(
            final List<Entity> workstationChangeoverForOperationalTasks, Date startDate, Date previousFinishDate) {
        if (startDate.compareTo(previousFinishDate) > 0) {
            int totalSetupDuration = 0;
            for (Entity workstationChangeoverForOperationalTask : workstationChangeoverForOperationalTasks.stream().filter(this::isNotParallel).collect(Collectors.toList())) {
                totalSetupDuration += workstationChangeoverForOperationalTask.getIntegerField(WorkstationChangeoverForOperationalTaskFields.DURATION);
            }

            for (Entity workstationChangeoverForOperationalTask : workstationChangeoverForOperationalTasks.stream().filter(this::isParallel).collect(Collectors.toList())) {
                Integer duration = workstationChangeoverForOperationalTask.getIntegerField(WorkstationChangeoverForOperationalTaskFields.DURATION);
                if (duration > totalSetupDuration) {
                    totalSetupDuration = duration;
                }
            }
            Date setupStartDate = new DateTime(startDate).minusSeconds(totalSetupDuration).toDate();
            if (setupStartDate.compareTo(previousFinishDate) < 0) {
                setupStartDate = previousFinishDate;
            }
            startDate = setupStartDate;
        }
        for (Entity workstationChangeoverForOperationalTask : workstationChangeoverForOperationalTasks.stream().filter(this::isParallel).collect(Collectors.toList())) {
            Integer duration = workstationChangeoverForOperationalTask.getIntegerField(WorkstationChangeoverForOperationalTaskFields.DURATION);
            Date finishDate = new DateTime(startDate).plusSeconds(duration).toDate();

            workstationChangeoverForOperationalTask.setField(WorkstationChangeoverForOperationalTaskFields.START_DATE, startDate);
            workstationChangeoverForOperationalTask.setField(WorkstationChangeoverForOperationalTaskFields.FINISH_DATE, finishDate);
        }

        Date lastStartDate = startDate;

        for (Entity workstationChangeoverForOperationalTask : workstationChangeoverForOperationalTasks.stream().filter(this::isNotParallel).collect(Collectors.toList())) {
            Integer duration = workstationChangeoverForOperationalTask.getIntegerField(WorkstationChangeoverForOperationalTaskFields.DURATION);
            Date finishDate = new DateTime(lastStartDate).plusSeconds(duration).toDate();

            workstationChangeoverForOperationalTask.setField(WorkstationChangeoverForOperationalTaskFields.START_DATE, lastStartDate);
            workstationChangeoverForOperationalTask.setField(WorkstationChangeoverForOperationalTaskFields.FINISH_DATE, finishDate);

            lastStartDate = finishDate;
        }

    }

    private void updateWorkstationChangeoversForSchedulePositionDates(final List<Entity> workstationChangeovers,
                                                                      final Date startDate) {
        Date lastStartDate = startDate;

        for (Entity workstationChangeover : workstationChangeovers.stream().filter(e -> !e.getBelongsToField(WorkstationChangeoverForSchedulePositionFields.WORKSTATION_CHANGEOVER_NORM).getBooleanField(WorkstationChangeoverForOperationalTaskFields.IS_PARALLEL)).collect(Collectors.toList())) {
            Integer duration = workstationChangeover.getBelongsToField(WorkstationChangeoverForSchedulePositionFields.WORKSTATION_CHANGEOVER_NORM).getIntegerField(WorkstationChangeoverNormFields.DURATION);
            Date finishDate = new DateTime(lastStartDate).plusSeconds(duration).toDate();

            workstationChangeover.setField(WorkstationChangeoverForSchedulePositionFields.START_DATE, lastStartDate);
            workstationChangeover.setField(WorkstationChangeoverForSchedulePositionFields.FINISH_DATE, finishDate);

            lastStartDate = finishDate;
        }

        for (Entity workstationChangeover : workstationChangeovers.stream().filter(e -> e.getBelongsToField(WorkstationChangeoverForSchedulePositionFields.WORKSTATION_CHANGEOVER_NORM).getBooleanField(WorkstationChangeoverForOperationalTaskFields.IS_PARALLEL)).collect(Collectors.toList())) {
            Integer duration = workstationChangeover.getBelongsToField(WorkstationChangeoverForSchedulePositionFields.WORKSTATION_CHANGEOVER_NORM).getIntegerField(WorkstationChangeoverNormFields.DURATION);
            Date finishDate = new DateTime(startDate).plusSeconds(duration).toDate();

            workstationChangeover.setField(WorkstationChangeoverForSchedulePositionFields.START_DATE, startDate);
            workstationChangeover.setField(WorkstationChangeoverForSchedulePositionFields.FINISH_DATE, finishDate);
        }
    }

    private boolean isParallel(final Entity workstationChangeoverForOperationalTask) {
        return getWorkstationChangeoverForOperationalTaskIsParallel(workstationChangeoverForOperationalTask);
    }

    private boolean isNotParallel(final Entity workstationChangeoverForOperationalTask) {
        return !getWorkstationChangeoverForOperationalTaskIsParallel(workstationChangeoverForOperationalTask);
    }

    private boolean getWorkstationChangeoverForOperationalTaskIsParallel(
            final Entity workstationChangeoverForOperationalTask) {
        return workstationChangeoverForOperationalTask.getBooleanField(WorkstationChangeoverForOperationalTaskFields.IS_PARALLEL);
    }

    private List<Entity> getProductAttributeValuesWithDataTypeCalculated(final Entity product) {
        return product.getHasManyField(ProductFields.PRODUCT_ATTRIBUTE_VALUES).stream().filter(this::filterProductAttributeValuesWithDataTypeCalculated).collect(Collectors.toList());
    }

    private boolean filterProductAttributeValuesWithDataTypeCalculated(final Entity productAttributeValue) {
        return AttributeDataType.CALCULATED.getStringValue().equals(productAttributeValue.getBelongsToField(ProductAttributeValueFields.ATTRIBUTE).getStringField(AttributeFields.DATA_TYPE));
    }

    private boolean filterProductAttributeValuesWithAttribute(final Entity productAttributeValue,
                                                              final Entity attribute) {
        return productAttributeValue.getBelongsToField(ProductAttributeValueFields.ATTRIBUTE).getId().equals(attribute.getId());
    }

    private boolean filterProductAttributeValuesWithAttributeValue(final Entity productAttributeValue,
                                                                   final Entity attributeValue) {
        return productAttributeValue.getBelongsToField(ProductAttributeValueFields.ATTRIBUTE_VALUE).getId().equals(attributeValue.getId());
    }

    private Entity createWorkstationChangeoverForOperationalTask(final Entity currentOperationalTask,
                                                                 final Entity previousOperationalTask,
                                                                 final Entity workstationChangeoverNorm,
                                                                 final Entity workstation,
                                                                 final Entity attribute,
                                                                 final Entity fromAttributeValue,
                                                                 final Entity toAttributeValue) {
        Entity workstationChangeoverForOperationalTask = getWorkstationChangeoverForOperationalTaskDD().create();

        String name = workstationChangeoverNorm.getStringField(WorkstationChangeoverNormFields.NAME);
        String description = workstationChangeoverNorm.getStringField(WorkstationChangeoverNormFields.DESCRIPTION);
        Integer duration = workstationChangeoverNorm.getIntegerField(WorkstationChangeoverNormFields.DURATION);
        boolean isParallel = workstationChangeoverNorm.getBooleanField(WorkstationChangeoverNormFields.IS_PARALLEL);

        workstationChangeoverForOperationalTask.setField(WorkstationChangeoverForOperationalTaskFields.NAME, name);
        workstationChangeoverForOperationalTask.setField(WorkstationChangeoverForOperationalTaskFields.DESCRIPTION, description);
        workstationChangeoverForOperationalTask.setField(WorkstationChangeoverForOperationalTaskFields.WORKSTATION, workstation);
        workstationChangeoverForOperationalTask.setField(WorkstationChangeoverForOperationalTaskFields.ATTRIBUTE, attribute);
        workstationChangeoverForOperationalTask.setField(WorkstationChangeoverForOperationalTaskFields.FROM_ATTRIBUTE_VALUE, fromAttributeValue);
        workstationChangeoverForOperationalTask.setField(WorkstationChangeoverForOperationalTaskFields.TO_ATTRIBUTE_VALUE, toAttributeValue);
        workstationChangeoverForOperationalTask.setField(WorkstationChangeoverForOperationalTaskFields.WORKSTATION_CHANGEOVER_NORM, workstationChangeoverNorm);
        workstationChangeoverForOperationalTask.setField(WorkstationChangeoverForOperationalTaskFields.CURRENT_OPERATIONAL_TASK, currentOperationalTask);
        workstationChangeoverForOperationalTask.setField(WorkstationChangeoverForOperationalTaskFields.PREVIOUS_OPERATIONAL_TASK, previousOperationalTask);
        workstationChangeoverForOperationalTask.setField(WorkstationChangeoverForOperationalTaskFields.CHANGEOVER_TYPE, WorkstationChangeoverForOperationalTaskChangeoverType.BASED_ON_NORM.getStringValue());
        workstationChangeoverForOperationalTask.setField(WorkstationChangeoverForOperationalTaskFields.DURATION, duration);
        workstationChangeoverForOperationalTask.setField(WorkstationChangeoverForOperationalTaskFields.IS_PARALLEL, isParallel);

        return workstationChangeoverForOperationalTask;
    }

    public Optional<Entity> findPreviousOperationalTask(final Entity operationalTask) {
        if (Objects.nonNull(operationalTask)) {
            Entity workstation = operationalTask.getBelongsToField(OperationalTaskFields.WORKSTATION);
            Date startDate = operationalTask.getDateField(OperationalTaskFields.START_DATE);

            SearchCriteriaBuilder searchCriteriaBuilder = getOperationalTaskDD().find();

            addWorkstationAndDateSearchRestrictions(searchCriteriaBuilder, workstation, startDate);

            return Optional.ofNullable(searchCriteriaBuilder
                    .addOrder(SearchOrders.desc(OperationalTaskFields.FINISH_DATE))
                    .setMaxResults(1).uniqueResult());
        } else {
            return Optional.empty();
        }
    }

    public Optional<Entity> findPreviousOperationalTask(final Entity operationalTask,
                                                        final Entity skipOperationalTask) {
        if (Objects.nonNull(operationalTask)) {
            Entity workstation = operationalTask.getBelongsToField(OperationalTaskFields.WORKSTATION);
            Date startDate = operationalTask.getDateField(OperationalTaskFields.START_DATE);

            SearchCriteriaBuilder searchCriteriaBuilder = getOperationalTaskDD().find();

            addWorkstationAndDateSearchRestrictions(searchCriteriaBuilder, workstation, startDate);

            return Optional.ofNullable(searchCriteriaBuilder.add(SearchRestrictions.idNe(skipOperationalTask.getId()))
                    .addOrder(SearchOrders.desc(OperationalTaskFields.FINISH_DATE))
                    .setMaxResults(1).uniqueResult());
        } else {
            return Optional.empty();
        }
    }

    public List<Entity> getPreviousOperationalTasks(final Entity operationalTask) {
        Entity workstation = operationalTask.getBelongsToField(OperationalTaskFields.WORKSTATION);
        Date startDate = operationalTask.getDateField(OperationalTaskFields.START_DATE);

        SearchCriteriaBuilder searchCriteriaBuilder = getOperationalTaskDD().find();

        addWorkstationAndDateSearchRestrictions(searchCriteriaBuilder, workstation, startDate);

        return searchCriteriaBuilder.addOrder(SearchOrders.desc(OperationalTaskFields.FINISH_DATE))
                .list().getEntities();
    }

    public Entity getPreviousOperationalTask(final Entity workstation, final Date operationalTaskStartDate) {
        SearchCriteriaBuilder searchCriteriaBuilder = getOperationalTaskDD().find();

        addWorkstationAndDateSearchRestrictions(searchCriteriaBuilder, workstation, operationalTaskStartDate);

        return searchCriteriaBuilder.addOrder(SearchOrders.desc(OperationalTaskFields.FINISH_DATE))
                .setMaxResults(1).uniqueResult();
    }

    private void addWorkstationAndDateSearchRestrictions(final SearchCriteriaBuilder searchCriteriaBuilder,
                                                         final Entity workstation, final Date startDate) {
        searchCriteriaBuilder.createAlias(OperationalTaskFields.WORKSTATION, OperationalTaskFields.WORKSTATION, JoinType.LEFT);
        searchCriteriaBuilder.add(SearchRestrictions.eq(OperationalTaskFields.WORKSTATION + L_DOT + L_ID, workstation.getId()));
        searchCriteriaBuilder.add(SearchRestrictions.ne(OperationalTaskFields.STATE, OperationalTaskState.REJECTED.getStringValue()));
        searchCriteriaBuilder.add(SearchRestrictions.le(OperationalTaskFields.FINISH_DATE, startDate));
        searchCriteriaBuilder.add(SearchRestrictions.eq(OperationalTaskFields.TYPE, OperationalTaskType.EXECUTION_OPERATION_IN_ORDER.getStringValue()));
    }

    public boolean hasWorkstationChangeoverForOperationalTasks(final Entity workstationChangeoverNorm) {
        Entity workstationChangeoverForOperationalTask = getWorkstationChangeoverForOperationalTaskDD().find()
                .createAlias(WorkstationChangeoverForOperationalTaskFields.WORKSTATION_CHANGEOVER_NORM, WorkstationChangeoverForOperationalTaskFields.WORKSTATION_CHANGEOVER_NORM, JoinType.LEFT)
                .add(SearchRestrictions.eq(WorkstationChangeoverForOperationalTaskFields.WORKSTATION_CHANGEOVER_NORM + L_DOT + L_ID, workstationChangeoverNorm.getId()))
                .setProjection(SearchProjections.alias(SearchProjections.countDistinct(L_ID), L_COUNT))
                .addOrder(SearchOrders.desc(L_COUNT)).setMaxResults(1).uniqueResult();

        Long countValue = (Long) workstationChangeoverForOperationalTask.getField(L_COUNT);

        return countValue > 0;
    }

    public Optional<Entity> getOperationalTask(final String number) {
        return Optional.ofNullable(getOperationalTaskDD().find().add(SearchRestrictions.eq(OperationalTaskFields.NUMBER, number))
                .add(SearchRestrictions.not(SearchRestrictions.in(OperationalTaskFields.STATE, Lists.newArrayList(OperationalTaskStateStringValues.FINISHED, OperationalTaskStateStringValues.REJECTED))))
                .setMaxResults(1).uniqueResult());
    }

    private DataDefinition getOperationalTaskDD() {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_OPERATIONAL_TASK);
    }

    public DataDefinition getWorkstationChangeoverForOperationalTaskDD() {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_WORKSTATION_CHANGEOVER_FOR_OPERATIONAL_TASK);
    }

    private DataDefinition getWorkstationChangeoverForSchedulePositionDD() {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_WORKSTATION_CHANGEOVER_FOR_SCHEDULE_POSITION);
    }

    public Entity getPreviousSchedulePosition(Entity schedule, Entity workstation, Entity toc, Date startDate) {
        return dataDefinitionService
                .get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_SCHEDULE_POSITION).find()
                .add(SearchRestrictions.belongsTo(SchedulePositionFields.WORKSTATION, workstation))
                .add(SearchRestrictions.belongsTo(SchedulePositionFields.SCHEDULE, schedule))
                .createAlias(SchedulePositionFields.TECHNOLOGY_OPERATION_COMPONENT, SchedulePositionFields.TECHNOLOGY_OPERATION_COMPONENT, JoinType.INNER)
                .add(SearchRestrictions.ne(SchedulePositionFields.TECHNOLOGY_OPERATION_COMPONENT + ".id", toc.getId()))
                .add(SearchRestrictions.le(SchedulePositionFields.END_TIME, startDate))
                .addOrder(SearchOrders.desc(SchedulePositionFields.END_TIME)).setMaxResults(1).uniqueResult();
    }
}
