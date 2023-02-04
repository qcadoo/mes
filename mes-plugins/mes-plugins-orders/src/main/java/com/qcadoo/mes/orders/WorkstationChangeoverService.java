package com.qcadoo.mes.orders;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.constants.AttributeDataType;
import com.qcadoo.mes.basic.constants.AttributeFields;
import com.qcadoo.mes.basic.constants.ProductAttributeValueFields;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.orders.constants.*;
import com.qcadoo.mes.orders.states.constants.OperationalTaskStateStringValues;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.WorkstationChangeoverNormChangeoverType;
import com.qcadoo.mes.technologies.constants.WorkstationChangeoverNormFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchProjections;
import com.qcadoo.model.api.search.SearchRestrictions;
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

    public List<Entity> findWorkstationChangeoverForOperationalTasks(final Entity operationalTask) {
        List<Entity> workstationChangeoverForOperationalTasks = Lists.newArrayList();

        Optional<Entity> mayBePreviousOperationalTask = findPreviousOperationalTask(operationalTask);

        if (mayBePreviousOperationalTask.isPresent()) {
            Entity previousOperationalTask = mayBePreviousOperationalTask.get();

            workstationChangeoverForOperationalTasks = findWorkstationChangeoverForOperationalTasks(operationalTask, previousOperationalTask);
        }

        return workstationChangeoverForOperationalTasks;
    }

    public List<Entity> findWorkstationChangeoverForOperationalTasks(final Entity operationalTask, final Entity previousOperationalTask) {
        List<Entity> workstationChangeoverForOperationalTasks = Lists.newArrayList();

        Entity workstation = operationalTask.getBelongsToField(OperationalTaskFields.WORKSTATION);

        if (hasWorkstationChangeoverNorms(workstation)) {
            Date startDate = operationalTask.getDateField(OperationalTaskFields.START_DATE);

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
                    List<Entity> workstationChangeoverNorms = findWorkstationChangeoverNorms(workstation, attribute);

                    workstationChangeoverNorms.forEach(workstationChangeoverNorm -> {
                        String changeoverType = workstationChangeoverNorm.getStringField(WorkstationChangeoverNormFields.CHANGEOVER_TYPE);

                        if (WorkstationChangeoverNormChangeoverType.BETWEEN_VALUES.getStringValue().equals(changeoverType)) {
                            Entity fromAttributeValue = workstationChangeoverNorm.getBelongsToField(WorkstationChangeoverNormFields.FROM_ATTRIBUTE_VALUE);
                            Entity toAttributeValue = workstationChangeoverNorm.getBelongsToField(WorkstationChangeoverNormFields.TO_ATTRIBUTE_VALUE);

                            if (attributeValue.getId().equals(toAttributeValue.getId())) {
                                if (filteredProductAttributeValues.stream().anyMatch(previousProductAttributeValue ->
                                        filterProductAttributeValuesWithAttributeValue(previousProductAttributeValue, fromAttributeValue))) {
                                    Entity workstationChangeoverForOperationalTask = createWorkstationChangeoverForOperationalTask(operationalTask, previousOperationalTask, workstationChangeoverNorm, workstation, attribute);

                                    workstationChangeoverForOperationalTasks.add(workstationChangeoverForOperationalTask);
                                }
                            }
                        } else {
                            Entity workstationChangeoverForOperationalTask = createWorkstationChangeoverForOperationalTask(operationalTask, previousOperationalTask, workstationChangeoverNorm, workstation, attribute);

                            workstationChangeoverForOperationalTasks.add(workstationChangeoverForOperationalTask);
                        }
                    });
                }
            });

            updateWorkstationChangeoverForOperationalTasksDates(workstationChangeoverForOperationalTasks, startDate);
        }

        return workstationChangeoverForOperationalTasks;
    }

    private void updateWorkstationChangeoverForOperationalTasksDates(final List<Entity> workstationChangeoverForOperationalTasks, final Date startDate) {
        Date lastStartDate = startDate;

        for (Entity workstationChangeoverForOperationalTask : workstationChangeoverForOperationalTasks.stream().filter(this::isNotParallel).collect(Collectors.toList())) {
            Integer duration = workstationChangeoverForOperationalTask.getIntegerField(WorkstationChangeoverForOperationalTaskFields.DURATION);
            Date finishDate = new DateTime(lastStartDate).plusSeconds(duration).toDate();

            workstationChangeoverForOperationalTask.setField(WorkstationChangeoverForOperationalTaskFields.START_DATE, lastStartDate);
            workstationChangeoverForOperationalTask.setField(WorkstationChangeoverForOperationalTaskFields.FINISH_DATE, finishDate);

            lastStartDate = finishDate;
        }

        for (Entity workstationChangeoverForOperationalTask : workstationChangeoverForOperationalTasks.stream().filter(this::isParallel).collect(Collectors.toList())) {
            Integer duration = workstationChangeoverForOperationalTask.getIntegerField(WorkstationChangeoverForOperationalTaskFields.DURATION);
            Date finishDate = new DateTime(startDate).plusSeconds(duration).toDate();

            workstationChangeoverForOperationalTask.setField(WorkstationChangeoverForOperationalTaskFields.START_DATE, startDate);
            workstationChangeoverForOperationalTask.setField(WorkstationChangeoverForOperationalTaskFields.FINISH_DATE, finishDate);
        }
    }

    private boolean isParallel(final Entity workstationChangeoverForOperationalTask) {
        return getWorkstationChangeoverForOperationalTaskIsParallel(workstationChangeoverForOperationalTask);
    }

    private boolean isNotParallel(final Entity workstationChangeoverForOperationalTask) {
        return !getWorkstationChangeoverForOperationalTaskIsParallel(workstationChangeoverForOperationalTask);
    }

    private boolean getWorkstationChangeoverForOperationalTaskIsParallel(final Entity workstationChangeoverForOperationalTask) {
        return workstationChangeoverForOperationalTask.getBooleanField(WorkstationChangeoverForOperationalTaskFields.IS_PARALLEL);
    }

    private List<Entity> getProductAttributeValuesWithDataTypeCalculated(final Entity product) {
        return product.getHasManyField(ProductFields.PRODUCT_ATTRIBUTE_VALUES).stream().filter(this::filterProductAttributeValuesWithDataTypeCalculated).collect(Collectors.toList());
    }

    private boolean filterProductAttributeValuesWithDataTypeCalculated(final Entity productAttributeValue) {
        return AttributeDataType.CALCULATED.getStringValue().equals(productAttributeValue.getBelongsToField(ProductAttributeValueFields.ATTRIBUTE).getStringField(AttributeFields.DATA_TYPE));
    }

    private boolean filterProductAttributeValuesWithAttribute(final Entity productAttributeValue, final Entity attribute) {
        return productAttributeValue.getBelongsToField(ProductAttributeValueFields.ATTRIBUTE).getId().equals(attribute.getId());
    }

    private boolean filterProductAttributeValuesWithAttributeValue(final Entity productAttributeValue, final Entity attributeValue) {
        return productAttributeValue.getBelongsToField(ProductAttributeValueFields.ATTRIBUTE_VALUE).getId().equals(attributeValue.getId());
    }

    private Entity createWorkstationChangeoverForOperationalTask(final Entity currentOperationalTask, final Entity previousOperationalTask,
                                                                 final Entity workstationChangeoverNorm,
                                                                 final Entity workstation, final Entity attribute) {
        Entity workstationChangeoverForOperationalTask = getWorkstationChangeoverForOperationalTaskDD().create();

        String name = workstationChangeoverNorm.getStringField(WorkstationChangeoverNormFields.NAME);
        String description = workstationChangeoverNorm.getStringField(WorkstationChangeoverNormFields.DESCRIPTION);
        Entity fromAttributeValue = workstationChangeoverNorm.getBelongsToField(WorkstationChangeoverNormFields.FROM_ATTRIBUTE_VALUE);
        Entity toAttributeValue = workstationChangeoverNorm.getBelongsToField(WorkstationChangeoverNormFields.TO_ATTRIBUTE_VALUE);
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

    public boolean hasWorkstationChangeoverNorms(final Entity workstation) {
        Entity workstationChangeoverNorm = getWorkstationChangeoverNormDD().find()
                .createAlias(WorkstationChangeoverNormFields.WORKSTATION, WorkstationChangeoverNormFields.WORKSTATION, JoinType.LEFT)
                .add(SearchRestrictions.eq(WorkstationChangeoverNormFields.WORKSTATION + L_DOT + L_ID, workstation.getId()))
                .setProjection(SearchProjections.alias(SearchProjections.countDistinct(L_ID), L_COUNT))
                .addOrder(SearchOrders.desc(L_COUNT)).setMaxResults(1).uniqueResult();

        Long countValue = (Long) workstationChangeoverNorm.getField(L_COUNT);

        return countValue > 0;
    }

    public List<Entity> findWorkstationChangeoverNorms(final Entity workstation, final Entity attribute) {
        return getWorkstationChangeoverNormDD().find()
                .createAlias(WorkstationChangeoverNormFields.WORKSTATION, WorkstationChangeoverNormFields.WORKSTATION, JoinType.LEFT)
                .createAlias(WorkstationChangeoverNormFields.ATTRIBUTE, WorkstationChangeoverNormFields.ATTRIBUTE, JoinType.LEFT)
                .add(SearchRestrictions.eq(WorkstationChangeoverNormFields.WORKSTATION + L_DOT + L_ID, workstation.getId()))
                .add(SearchRestrictions.eq(WorkstationChangeoverNormFields.ATTRIBUTE + L_DOT + L_ID, attribute.getId()))
                .list().getEntities();
    }

    public Optional<Entity> getOperationalTask(final String number) {
        return Optional.ofNullable(getOperationalTaskDD().find().add(SearchRestrictions.eq(OperationalTaskFields.NUMBER, number))
                .add(SearchRestrictions.not(SearchRestrictions.in(OperationalTaskFields.STATE, Lists.newArrayList(OperationalTaskStateStringValues.FINISHED, OperationalTaskStateStringValues.REJECTED))))
                .setMaxResults(1).uniqueResult());
    }

    public Optional<Entity> findPreviousOperationalTask(final Entity operationalTask) {
        if (Objects.nonNull(operationalTask)) {
            Entity workstation = operationalTask.getBelongsToField(OperationalTaskFields.WORKSTATION);
            Date startDate = operationalTask.getDateField(OperationalTaskFields.START_DATE);

            return Optional.ofNullable(getOperationalTaskDD().find()
                    .createAlias(OperationalTaskFields.WORKSTATION, OperationalTaskFields.WORKSTATION, JoinType.LEFT)
                    .add(SearchRestrictions.eq(OperationalTaskFields.WORKSTATION + L_DOT + L_ID, workstation.getId()))
                    .add(SearchRestrictions.lt(OperationalTaskFields.FINISH_DATE, startDate))
                    .addOrder(SearchOrders.desc(OperationalTaskFields.FINISH_DATE)).setMaxResults(1).uniqueResult());
        } else {
            return Optional.empty();
        }
    }

    private DataDefinition getOperationalTaskDD() {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_OPERATIONAL_TASK);
    }

    private DataDefinition getWorkstationChangeoverForOperationalTaskDD() {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_WORKSTATION_CHANGEOVER_FOR_OPERATIONAL_TASK);
    }

    private DataDefinition getWorkstationChangeoverNormDD() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_WORKSTATION_CHANGEOVER_NORM);
    }

}
