package com.qcadoo.mes.orders.hooks;

import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.criteriaModifiers.AttributeValueCriteriaModifiers;
import com.qcadoo.mes.orders.services.WorkstationChangeoverService;
import com.qcadoo.mes.orders.constants.OperationalTaskFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.WorkstationChangeoverForOperationalTaskChangeoverType;
import com.qcadoo.mes.orders.constants.WorkstationChangeoverForOperationalTaskFields;
import com.qcadoo.mes.orders.criteriaModifiers.OperationalTaskCriteriaModifiers;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.*;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

@Service
public class WorkstationChangeoverForOperationalTaskDetailsHooks {

    private static final String L_ACTIONS = "actions";

    private static final String L_COPY = "copy";

    private static final String L_CURRENT_OPERATIONAL_TASK_NUMBER = "currentOperationalTaskNumber";

    @Autowired
    private WorkstationChangeoverService workstationChangeoverService;

    public void onBeforeRender(final ViewDefinitionState view) {
        setFieldsAndLookups(view);
        setFieldsRequired(view);
        setLookupsEnabledAndFilterValueHolders(view);
        setRibbonState(view);
    }

    private void setFieldsAndLookups(final ViewDefinitionState view) {
        FormComponent workstationChangeoverForOperationalTaskForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        FieldComponent currentOperationalTaskNumberField = (FieldComponent) view.getComponentByReference(L_CURRENT_OPERATIONAL_TASK_NUMBER);
        LookupComponent currentOperationalTaskLookup = (LookupComponent) view.getComponentByReference(WorkstationChangeoverForOperationalTaskFields.CURRENT_OPERATIONAL_TASK);
        LookupComponent previousOperationalTaskLookup = (LookupComponent) view.getComponentByReference(WorkstationChangeoverForOperationalTaskFields.PREVIOUS_OPERATIONAL_TASK);
        LookupComponent workstationLookup = (LookupComponent) view.getComponentByReference(WorkstationChangeoverForOperationalTaskFields.WORKSTATION);
        FieldComponent changeoverTypeField = (FieldComponent) view.getComponentByReference(WorkstationChangeoverForOperationalTaskFields.CHANGEOVER_TYPE);

        Long workstationChangeoverForOperationalTaskId = workstationChangeoverForOperationalTaskForm.getEntityId();

        String currentOperationalTaskNumber = (String) currentOperationalTaskNumberField.getFieldValue();
        Entity currentOperationalTask = currentOperationalTaskLookup.getEntity();
        Entity previousOperationalTask = previousOperationalTaskLookup.getEntity();
        Entity workstation = workstationLookup.getEntity();
        String changeoverType = (String) changeoverTypeField.getFieldValue();
        Date startDate;

        if (Objects.isNull(workstationChangeoverForOperationalTaskId)) {
            changeoverType = WorkstationChangeoverForOperationalTaskChangeoverType.OWN.getStringValue();
        }

        boolean isOwn = WorkstationChangeoverForOperationalTaskChangeoverType.OWN.getStringValue().equals(changeoverType);

        if (isOwn) {
            if (StringUtils.isNotEmpty(currentOperationalTaskNumber)) {
                Optional<Entity> mayBeOperationalTask = workstationChangeoverService.getOperationalTask(currentOperationalTaskNumber);

                if (mayBeOperationalTask.isPresent()) {
                    currentOperationalTask = mayBeOperationalTask.get();

                    workstation = currentOperationalTask.getBelongsToField(OperationalTaskFields.WORKSTATION);
                    startDate = currentOperationalTask.getDateField(OperationalTaskFields.START_DATE);

                    setWorkstation(view, workstation);
                    setFinishDate(view, startDate);
                }
            }

            Optional<Entity> mayBePreviousOperationalTask = workstationChangeoverService.findPreviousOperationalTask(currentOperationalTask);

            previousOperationalTask = mayBePreviousOperationalTask.orElse(null);

            if (Objects.isNull(workstation)) {
                currentOperationalTask = null;
                previousOperationalTask = null;
            }
        }

        setCurrentOperationalTask(view, currentOperationalTask, view.isViewAfterRedirect());
        setPreviousOperationalTask(view, previousOperationalTask, true);

        setChangeOverType(changeoverTypeField, changeoverType);
    }

    public void setCurrentOperationalTask(final ViewDefinitionState view, final Entity currentOperationalTask, final boolean shouldClear) {
        LookupComponent currentOperationalTaskLookup = (LookupComponent) view.getComponentByReference(WorkstationChangeoverForOperationalTaskFields.CURRENT_OPERATIONAL_TASK);
        FieldComponent currentOperationalTaskNameField = (FieldComponent) view.getComponentByReference(WorkstationChangeoverForOperationalTaskFields.CURRENT_OPERATIONAL_TASK_NAME);
        LookupComponent currentOperationalTaskOrderLookup = (LookupComponent) view.getComponentByReference(WorkstationChangeoverForOperationalTaskFields.CURRENT_OPERATIONAL_TASK_ORDER);
        LookupComponent currentOperationalTaskOrderProductLookup = (LookupComponent) view.getComponentByReference(WorkstationChangeoverForOperationalTaskFields.CURRENT_OPERATIONAL_TASK_ORDER_PRODUCT);
        FieldComponent currentOperationalTaskStartDateField = (FieldComponent) view.getComponentByReference(WorkstationChangeoverForOperationalTaskFields.CURRENT_OPERATIONAL_TASK_START_DATE);
        FieldComponent currentOperationalTaskFinishDateField = (FieldComponent) view.getComponentByReference(WorkstationChangeoverForOperationalTaskFields.CURRENT_OPERATIONAL_TASK_FINISH_DATE);

        Long operationalTaskId = null;
        String name = null;
        Long orderId = null;
        Long productId = null;
        Date startDate = null;
        Date finishDate = null;

        if (Objects.nonNull(currentOperationalTask)) {
            Entity order = currentOperationalTask.getBelongsToField(OperationalTaskFields.ORDER);
            Entity product = order.getBelongsToField(OrderFields.PRODUCT);

            operationalTaskId = currentOperationalTask.getId();
            name = currentOperationalTask.getStringField(OperationalTaskFields.NAME);
            orderId = order.getId();
            productId = product.getId();
            startDate = currentOperationalTask.getDateField(OperationalTaskFields.START_DATE);
            finishDate = currentOperationalTask.getDateField(OperationalTaskFields.FINISH_DATE);
        }

        if (shouldClear) {
            currentOperationalTaskLookup.setFieldValue(operationalTaskId);
            currentOperationalTaskLookup.requestComponentUpdateState();
        }
        currentOperationalTaskNameField.setFieldValue(name);
        currentOperationalTaskNameField.requestComponentUpdateState();
        currentOperationalTaskOrderLookup.setFieldValue(orderId);
        currentOperationalTaskOrderLookup.requestComponentUpdateState();
        currentOperationalTaskOrderProductLookup.setFieldValue(productId);
        currentOperationalTaskOrderProductLookup.requestComponentUpdateState();
        currentOperationalTaskStartDateField.setFieldValue(DateUtils.toDateTimeString(startDate));
        currentOperationalTaskStartDateField.requestComponentUpdateState();
        currentOperationalTaskFinishDateField.setFieldValue(DateUtils.toDateTimeString(finishDate));
        currentOperationalTaskFinishDateField.requestComponentUpdateState();
    }

    public void setPreviousOperationalTask(final ViewDefinitionState view, final Entity previousOperationalTask, final boolean shouldClear) {
        LookupComponent previousOperationalTaskLookup = (LookupComponent) view.getComponentByReference(WorkstationChangeoverForOperationalTaskFields.PREVIOUS_OPERATIONAL_TASK);
        FieldComponent previousOperationalTaskNameField = (FieldComponent) view.getComponentByReference(WorkstationChangeoverForOperationalTaskFields.PREVIOUS_OPERATIONAL_TASK_NAME);
        LookupComponent previousOperationalTaskOrderLookup = (LookupComponent) view.getComponentByReference(WorkstationChangeoverForOperationalTaskFields.PREVIOUS_OPERATIONAL_TASK_ORDER);
        LookupComponent previousOperationalTaskOrderProductLookup = (LookupComponent) view.getComponentByReference(WorkstationChangeoverForOperationalTaskFields.PREVIOUS_OPERATIONAL_TASK_ORDER_PRODUCT);
        FieldComponent previousOperationalTaskStartDateField = (FieldComponent) view.getComponentByReference(WorkstationChangeoverForOperationalTaskFields.PREVIOUS_OPERATIONAL_TASK_START_DATE);
        FieldComponent previousOperationalTaskFinishDateField = (FieldComponent) view.getComponentByReference(WorkstationChangeoverForOperationalTaskFields.PREVIOUS_OPERATIONAL_TASK_FINISH_DATE);

        Long operationalTaskId = null;
        String name = null;
        Long orderId = null;
        Long productId = null;
        Date startDate = null;
        Date finishDate = null;

        if (Objects.nonNull(previousOperationalTask)) {
            Entity order = previousOperationalTask.getBelongsToField(OperationalTaskFields.ORDER);
            Entity product = order.getBelongsToField(OrderFields.PRODUCT);

            operationalTaskId = previousOperationalTask.getId();
            name = previousOperationalTask.getStringField(OperationalTaskFields.NAME);
            orderId = order.getId();
            productId = product.getId();
            startDate = previousOperationalTask.getDateField(OperationalTaskFields.START_DATE);
            finishDate = previousOperationalTask.getDateField(OperationalTaskFields.FINISH_DATE);
        }

        if (shouldClear) {
            previousOperationalTaskLookup.setFieldValue(operationalTaskId);
            previousOperationalTaskLookup.requestComponentUpdateState();
        }
        previousOperationalTaskNameField.setFieldValue(name);
        previousOperationalTaskNameField.requestComponentUpdateState();
        previousOperationalTaskOrderLookup.setFieldValue(orderId);
        previousOperationalTaskOrderLookup.requestComponentUpdateState();
        previousOperationalTaskOrderProductLookup.setFieldValue(productId);
        previousOperationalTaskOrderProductLookup.requestComponentUpdateState();
        previousOperationalTaskStartDateField.setFieldValue(DateUtils.toDateTimeString(startDate));
        previousOperationalTaskStartDateField.requestComponentUpdateState();
        previousOperationalTaskFinishDateField.setFieldValue(DateUtils.toDateTimeString(finishDate));
        previousOperationalTaskFinishDateField.requestComponentUpdateState();
    }

    private void setWorkstation(final ViewDefinitionState view, final Entity workstation) {
        LookupComponent workstationLookup = (LookupComponent) view.getComponentByReference(WorkstationChangeoverForOperationalTaskFields.WORKSTATION);

        Long workstationId = null;

        if (Objects.nonNull(workstation)) {
            workstationId = workstation.getId();
        }

        workstationLookup.setFieldValue(workstationId);
        workstationLookup.requestComponentUpdateState();
    }

    private void setFinishDate(final ViewDefinitionState view, final Date finishDate) {
        FieldComponent finishDateField = (FieldComponent) view.getComponentByReference(WorkstationChangeoverForOperationalTaskFields.FINISH_DATE);

        finishDateField.setFieldValue(DateUtils.toDateTimeString(finishDate));
        finishDateField.requestComponentUpdateState();
    }

    private void setChangeOverType(final FieldComponent changeoverTypeField, final String changeoverType) {
        changeoverTypeField.setFieldValue(changeoverType);
        changeoverTypeField.requestComponentUpdateState();
    }

    private void setFieldsRequired(final ViewDefinitionState view) {
        FieldComponent numberField = (FieldComponent) view.getComponentByReference(WorkstationChangeoverForOperationalTaskFields.NUMBER);
        LookupComponent currentOperationalTaskLookup = (LookupComponent) view.getComponentByReference(WorkstationChangeoverForOperationalTaskFields.CURRENT_OPERATIONAL_TASK);
        FieldComponent changeoverTypeField = (FieldComponent) view.getComponentByReference(WorkstationChangeoverForOperationalTaskFields.CHANGEOVER_TYPE);

        String changeoverType = (String) changeoverTypeField.getFieldValue();

        boolean isOwn = WorkstationChangeoverForOperationalTaskChangeoverType.OWN.getStringValue().equals(changeoverType);

        numberField.setRequired(true);
        currentOperationalTaskLookup.setRequired(isOwn);
    }

    private void setLookupsEnabledAndFilterValueHolders(final ViewDefinitionState view) {
        LookupComponent workstationLookup = (LookupComponent) view.getComponentByReference(WorkstationChangeoverForOperationalTaskFields.WORKSTATION);
        FieldComponent currentOperationalTaskNumberField = (FieldComponent) view.getComponentByReference(L_CURRENT_OPERATIONAL_TASK_NUMBER);
        LookupComponent currentOperationalTaskLookup = (LookupComponent) view.getComponentByReference(WorkstationChangeoverForOperationalTaskFields.CURRENT_OPERATIONAL_TASK);
        LookupComponent previousOperationalTaskLookup = (LookupComponent) view.getComponentByReference(WorkstationChangeoverForOperationalTaskFields.PREVIOUS_OPERATIONAL_TASK);
        FieldComponent changeoverTypeField = (FieldComponent) view.getComponentByReference(WorkstationChangeoverForOperationalTaskFields.CHANGEOVER_TYPE);
        LookupComponent attributeLookup = (LookupComponent) view.getComponentByReference(WorkstationChangeoverForOperationalTaskFields.ATTRIBUTE);
        LookupComponent fromAttributeValueLookup = (LookupComponent) view.getComponentByReference(WorkstationChangeoverForOperationalTaskFields.FROM_ATTRIBUTE_VALUE);
        LookupComponent toAttributeValueLookup = (LookupComponent) view.getComponentByReference(WorkstationChangeoverForOperationalTaskFields.TO_ATTRIBUTE_VALUE);
        CheckBoxComponent isParallelCheckbox = (CheckBoxComponent) view.getComponentByReference(WorkstationChangeoverForOperationalTaskFields.IS_PARALLEL);

        String currentOperationalTaskNumber = (String) currentOperationalTaskNumberField.getFieldValue();
        String changeoverType = (String) changeoverTypeField.getFieldValue();

        boolean isOwn = WorkstationChangeoverForOperationalTaskChangeoverType.OWN.getStringValue().equals(changeoverType);
        boolean isCurrentOperationalTaskNumberEmpty = StringUtils.isEmpty(currentOperationalTaskNumber);

        workstationLookup.setEnabled(isOwn && isCurrentOperationalTaskNumberEmpty);
        workstationLookup.requestComponentUpdateState();

        currentOperationalTaskLookup.setEnabled(isOwn && isCurrentOperationalTaskNumberEmpty);
        currentOperationalTaskLookup.requestComponentUpdateState();

        Entity workstation = workstationLookup.getEntity();

        setOperationalTaskLookup(currentOperationalTaskLookup, workstation);
        setOperationalTaskLookup(previousOperationalTaskLookup, workstation);

        Entity attribute = attributeLookup.getEntity();

        setAttributeValueLookup(fromAttributeValueLookup, attribute);
        setAttributeValueLookup(toAttributeValueLookup, attribute);

        isParallelCheckbox.setEnabled(isOwn);
        isParallelCheckbox.requestComponentUpdateState();
    }

    private void setOperationalTaskLookup(final LookupComponent lookupComponent, final Entity workstation) {
        FilterValueHolder filterValueHolder = lookupComponent.getFilterValue();

        if (Objects.nonNull(workstation)) {
            filterValueHolder.put(OperationalTaskCriteriaModifiers.L_WORKSTATION_ID, workstation.getId());
        } else {
            filterValueHolder.remove(OperationalTaskCriteriaModifiers.L_WORKSTATION_ID);

            lookupComponent.setFieldValue(null);
        }

        lookupComponent.setFilterValue(filterValueHolder);
        lookupComponent.requestComponentUpdateState();
    }

    private void setAttributeValueLookup(final LookupComponent lookupComponent, final Entity attribute) {
        FilterValueHolder filterValueHolder = lookupComponent.getFilterValue();

        if (Objects.nonNull(attribute)) {
            filterValueHolder.put(AttributeValueCriteriaModifiers.L_ATTRIBUTE_ID, attribute.getId());
        } else {
            filterValueHolder.remove(AttributeValueCriteriaModifiers.L_ATTRIBUTE_ID);
        }

        lookupComponent.setFilterValue(filterValueHolder);
        lookupComponent.requestComponentUpdateState();
    }

    private void setRibbonState(final ViewDefinitionState view) {
        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);

        Ribbon ribbon = window.getRibbon();
        RibbonGroup actionsRibbonGroup = ribbon.getGroupByName(L_ACTIONS);
        RibbonActionItem copyRibbonActionItem = actionsRibbonGroup.getItemByName(L_COPY);

        FormComponent workstationChangeoverForOperationalTaskForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity workstationChangeoverForOperationalTask = workstationChangeoverForOperationalTaskForm.getEntity();

        String changeoverType = workstationChangeoverForOperationalTask.getStringField(WorkstationChangeoverForOperationalTaskFields.CHANGEOVER_TYPE);

        boolean isSaved = Objects.nonNull(workstationChangeoverForOperationalTask.getId());
        boolean isOwn = WorkstationChangeoverForOperationalTaskChangeoverType.OWN.getStringValue().equals(changeoverType);

        copyRibbonActionItem.setEnabled(isSaved && isOwn);
        copyRibbonActionItem.requestUpdate(true);
    }

}
