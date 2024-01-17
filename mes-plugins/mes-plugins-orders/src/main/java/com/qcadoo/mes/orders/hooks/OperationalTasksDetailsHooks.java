/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 * <p>
 * This file is part of Qcadoo.
 * <p>
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.orders.hooks;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.orders.OperationalTasksService;
import com.qcadoo.mes.orders.constants.*;
import com.qcadoo.mes.orders.criteriaModifiers.OperationalTaskDetailsCriteriaModifiers;
import com.qcadoo.mes.orders.states.constants.OperationalTaskState;
import com.qcadoo.mes.orders.states.constants.OperationalTaskStateStringValues;
import com.qcadoo.mes.technologies.constants.OperationFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentFieldsTNFO;
import com.qcadoo.model.api.*;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.*;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.api.utils.NumberGeneratorService;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

@Service
public class OperationalTasksDetailsHooks {

    private static final String L_ORDER = "order";

    private static final String L_SHOW_ORDER = "showOrder";

    private static final String L_TECHNOLOGY_OPERATION_COMPONENT = "technologyOperationComponent";

    private static final String L_SHOW_OPERATION_PARAMETERS = "showOperationParameters";

    private static final String L_OPERATIONAL_TASKS = "operationalTasks";

    private static final String L_SHOW_OPERATIONAL_TASKS_WITH_ORDER = "showOperationalTasksWithOrder";

    private static final String L_WORKSTATION_CHANGEOVER_FOR_OPERATIONAL_TASKS = "workstationChangeoverForOperationalTasks";

    private static final String L_SHOW_WORKSTATION_CHANGEOVER_FOR_OPERATIONAL_TASKS = "showWorkstationChangeoverForOperationalTasks";

    private static final String L_STAFF_LOOKUP = "staffLookup";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private OperationalTasksService operationalTasksService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    public void customRestrictionAfterRedirectFromOrder(final ViewDefinitionState view) {
        if (view.isViewAfterRedirect() && Objects.nonNull(view.getJsonContext()) && view.getJsonContext().has("window.fromOrderDetails")) {
            GridComponent grid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);

            grid.removeFilterForField(OperationalTaskFields.STATE);

            grid.setCustomRestriction(searchBuilder -> searchBuilder.add(SearchRestrictions.or(
                    SearchRestrictions.eq(OperationalTaskFields.STATE, OperationalTaskState.PENDING.getStringValue()),
                    SearchRestrictions.eq(OperationalTaskFields.STATE, OperationalTaskState.FINISHED.getStringValue()),
                    SearchRestrictions.eq(OperationalTaskFields.STATE, OperationalTaskState.REJECTED.getStringValue()),
                    SearchRestrictions.eq(OperationalTaskFields.STATE, OperationalTaskState.STARTED.getStringValue()))));
            view.getJsonContext().remove("window.fromOrderDetails");
        }
    }

    public void onBeforeRender(final ViewDefinitionState view) {
        fetchNumberFromDatabase(view);
        filterDivisionLookup(view);
        setTechnology(view);
        setQuantities(view);
        setStaff(view);
        disableFieldsWhenOrderTypeIsSelected(view);
        disableButtons(view);
        fillCriteriaModifiers(view);
        setHasChangeovers(view);
    }

    private void fetchNumberFromDatabase(final ViewDefinitionState view) {
        FormComponent operationalTaskForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        ComponentState numberField = view.getComponentByReference(OperationalTaskFields.NUMBER);

        if (Objects.nonNull(operationalTaskForm.getEntityId())) {
            String numberFieldValue = (String) numberField.getFieldValue();

            if (Strings.isNullOrEmpty(numberFieldValue)) {
                Entity operationalTask = getOperationalTaskDD().get(operationalTaskForm.getEntityId());

                numberField.setFieldValue(operationalTask.getField(OperationalTaskFields.NUMBER));
            }
        }
    }

    private void filterDivisionLookup(final ViewDefinitionState view) {
        LookupComponent divisionLookup = (LookupComponent) view.getComponentByReference(OperationalTaskFields.DIVISION);
        LookupComponent workstationLookup = (LookupComponent) view.getComponentByReference(OperationalTaskFields.WORKSTATION);

        Entity division = divisionLookup.getEntity();

        FilterValueHolder filterValueHolder = workstationLookup.getFilterValue();

        if (Objects.nonNull(division)) {
            Long divisionId = division.getId();

            filterValueHolder.put(OperationalTaskFields.DIVISION, divisionId);

            workstationLookup.setFilterValue(filterValueHolder);
        }
    }

    private void setTechnology(final ViewDefinitionState view) {
        LookupComponent technologyLookup = (LookupComponent) view.getComponentByReference(OperationalTaskFields.TECHNOLOGY);
        LookupComponent orderLookup = (LookupComponent) view.getComponentByReference(OperationalTaskFields.ORDER);

        Entity order = orderLookup.getEntity();

        if (Objects.isNull(order)) {
            return;
        }

        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

        if (!Objects.isNull(technology)) {
            technologyLookup.setFieldValue(technology.getId());
            technologyLookup.requestComponentUpdateState();
        }
    }

    private void setQuantities(final ViewDefinitionState view) {
        final FormComponent operationalTaskForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Long operationalTaskId = operationalTaskForm.getEntityId();

        if (Objects.nonNull(operationalTaskId)) {
            Entity operationalTask = operationalTaskForm.getEntity();

            Entity product = operationalTask.getBelongsToField(OperationalTaskFields.PRODUCT);

            if (Objects.nonNull(product)) {
                Entity operationalTaskDto = getOperationalTaskDtoDD().get(operationalTaskId);

                FieldComponent doneInPercentage = (FieldComponent) view.getComponentByReference("doneInPercentage");
                FieldComponent doneInPercentageUnit = (FieldComponent) view.getComponentByReference("doneInPercentageUNIT");
                FieldComponent usedQuantityUnit = (FieldComponent) view.getComponentByReference("usedQuantityUNIT");
                FieldComponent plannedQuantityUnit = (FieldComponent) view.getComponentByReference("plannedQuantityUNIT");
                FieldComponent usedQuantity = (FieldComponent) view.getComponentByReference("usedQuantity");
                FieldComponent plannedQuantity = (FieldComponent) view.getComponentByReference("plannedQuantity");

                usedQuantityUnit.setFieldValue(product.getStringField(ProductFields.UNIT));
                plannedQuantityUnit.setFieldValue(product.getStringField(ProductFields.UNIT));

                plannedQuantity.setFieldValue(numberService
                        .formatWithMinimumFractionDigits(operationalTaskDto.getDecimalField(OperationalTaskDtoFields.PLANNED_QUANTITY), 0));
                usedQuantity.setFieldValue(numberService
                        .formatWithMinimumFractionDigits(operationalTaskDto.getDecimalField(OperationalTaskDtoFields.USED_QUANTITY), 0));

                if (Objects.nonNull(operationalTaskDto.getDecimalField(OperationalTaskDtoFields.PLANNED_QUANTITY))
                        && operationalTaskDto.getDecimalField(OperationalTaskDtoFields.PLANNED_QUANTITY).compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal doneInPercentageQuantity = BigDecimalUtils.convertNullToZero(operationalTaskDto.getDecimalField(OperationalTaskDtoFields.USED_QUANTITY))
                            .multiply(new BigDecimal(100));
                    doneInPercentageQuantity = doneInPercentageQuantity
                            .divide(operationalTaskDto.getDecimalField(OperationalTaskDtoFields.PLANNED_QUANTITY), MathContext.DECIMAL64);
                    doneInPercentage.setFieldValue(numberService
                            .formatWithMinimumFractionDigits(doneInPercentageQuantity.setScale(0, RoundingMode.CEILING), 0));
                } else {
                    doneInPercentage.setFieldValue(numberService.formatWithMinimumFractionDigits(BigDecimal.ZERO, 0));
                }

                doneInPercentage.setEnabled(false);
                doneInPercentageUnit.setFieldValue("%");
            }
        }
    }

    private void setStaff(final ViewDefinitionState view) {
        FieldComponent minStaffField = (FieldComponent) view.getComponentByReference(TechnologyOperationComponentFieldsTNFO.MIN_STAFF);
        FieldComponent optimalStaffField = (FieldComponent) view.getComponentByReference(TechnologyOperationComponentFieldsTNFO.OPTIMAL_STAFF);
        FieldComponent actualStaffField = (FieldComponent) view.getComponentByReference(OperationalTaskFields.ACTUAL_STAFF);
        GridComponent workersGrid = (GridComponent) view.getComponentByReference(OperationalTaskFields.WORKERS);
        LookupComponent technologyOperationComponentLookup = (LookupComponent) view.getComponentByReference(OperationalTaskFields.TECHNOLOGY_OPERATION_COMPONENT);

        Entity technologyOperationComponent = technologyOperationComponentLookup.getEntity();

        int minStaff;
        if (!Objects.isNull(technologyOperationComponent)) {
            minStaff = technologyOperationComponent.getIntegerField(TechnologyOperationComponentFieldsTNFO.MIN_STAFF);
        } else {
            minStaff = 1;
        }

        minStaffField.setFieldValue(minStaff);

        int optimalStaff;
        if (!Objects.isNull(technologyOperationComponent)) {
            optimalStaff = technologyOperationComponent.getIntegerField(TechnologyOperationComponentFieldsTNFO.OPTIMAL_STAFF);
        } else {
            optimalStaff = 1;
        }

        optimalStaffField.setFieldValue(optimalStaff);
        if (Objects.isNull(actualStaffField.getFieldValue()) || !NumberUtils.isDigits(actualStaffField.getFieldValue().toString())) {
            actualStaffField.setFieldValue(optimalStaff);
        }

        LookupComponent staff = (LookupComponent) view.getComponentByReference(OperationalTaskFields.STAFF);

        List<Entity> workers = workersGrid.getEntities();

        if (Objects.nonNull(staff.getEntity()) && workers.size() != 1) {
            staff.setFieldValue(null);
        } else if (workers.size() == 1 && !staff.isClearCurrentCode() && "".equals(staff.getCurrentCode())) {
            staff.setFieldValue(workers.get(0).getId());
        }

        staff.setEnabled(workers.size() <= 1);

        int actualStaff = Integer.parseInt((String) actualStaffField.getFieldValue());

        if (view.isViewAfterRedirect() && actualStaff != workers.size()) {
            view.addMessage(
                    "orders.operationalTask.error.workersQuantityDifferentThanActualStaff", ComponentState.MessageType.INFO);
        }
    }

    public void disableFieldsWhenOrderTypeIsSelected(final ViewDefinitionState view) {
        FormComponent operationalTaskForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        FieldComponent typeField = (FieldComponent) view.getComponentByReference(OperationalTaskFields.TYPE);
        FieldComponent stateField = (FieldComponent) view.getComponentByReference(OperationalTaskFields.STATE);
        FieldComponent actualStaff = (FieldComponent) view.getComponentByReference(OperationalTaskFields.ACTUAL_STAFF);
        GridComponent workers = (GridComponent) view.getComponentByReference(OperationalTaskFields.WORKERS);

        String type = (String) typeField.getFieldValue();
        String state = (String) stateField.getFieldValue();

        List<String> referenceBasicFields = Lists.newArrayList(OperationalTaskFields.NAME);
        List<String> extendFields = Lists.newArrayList(OperationalTaskFields.ORDER,
                OperationalTaskFields.TECHNOLOGY_OPERATION_COMPONENT);

        if (operationalTasksService.isOperationalTaskTypeOtherCase(type)) {
            changedStateField(view, referenceBasicFields, true);
            changedStateField(view, extendFields, false);

            clearFieldValue(view, extendFields);
        } else {
            changedStateField(view, referenceBasicFields, false);
            changedStateField(view, extendFields, true);
        }

        boolean staffTabEnabled = !OperationalTaskStateStringValues.FINISHED.equals(state) && !OperationalTaskStateStringValues.REJECTED.equals(state);

        actualStaff.setEnabled(staffTabEnabled);
        workers.setEnabled(staffTabEnabled && Objects.nonNull(operationalTaskForm.getEntityId()));
    }

    public void disableButtons(final ViewDefinitionState view) {
        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);

        RibbonGroup orderRibbonGroup = window.getRibbon().getGroupByName(L_ORDER);
        RibbonGroup technologyOperationComponentRibbonGroup = window.getRibbon().getGroupByName(L_TECHNOLOGY_OPERATION_COMPONENT);
        RibbonGroup operationalTasksRibbonGroup = window.getRibbon().getGroupByName(L_OPERATIONAL_TASKS);
        RibbonGroup changeoversRibbonGroup = window.getRibbon().getGroupByName(L_WORKSTATION_CHANGEOVER_FOR_OPERATIONAL_TASKS);

        RibbonActionItem showOrderRibbonItem = orderRibbonGroup.getItemByName(L_SHOW_ORDER);
        RibbonActionItem showOperationParametersRibbonItem = technologyOperationComponentRibbonGroup.getItemByName(L_SHOW_OPERATION_PARAMETERS);
        RibbonActionItem showOperationalTasksWithOrderRibbonItem = operationalTasksRibbonGroup.getItemByName(L_SHOW_OPERATIONAL_TASKS_WITH_ORDER);
        RibbonActionItem showWorkstationChangeoverForOperationalTasksRibbonItem = changeoversRibbonGroup.getItemByName(L_SHOW_WORKSTATION_CHANGEOVER_FOR_OPERATIONAL_TASKS);

        FieldComponent typeField = (FieldComponent) view.getComponentByReference(OperationalTaskFields.TYPE);
        LookupComponent orderLookup = (LookupComponent) view.getComponentByReference(OperationalTaskFields.ORDER);
        LookupComponent technologyOperationComponentLookup = (LookupComponent) view
                .getComponentByReference(OperationalTaskFields.TECHNOLOGY_OPERATION_COMPONENT);
        LookupComponent workstationLookup = (LookupComponent) view.getComponentByReference(OperationalTaskFields.WORKSTATION);

        String type = (String) typeField.getFieldValue();

        boolean isOperationalTaskTypeExecutionOperationInOrder = operationalTasksService.isOperationalTaskTypeExecutionOperationInOrder(type);
        boolean isOrderSelected = Objects.nonNull(orderLookup.getEntity());
        boolean isTechnologyOperationComponentSelected = Objects.nonNull(technologyOperationComponentLookup.getEntity());
        boolean isWorkstationSelected = Objects.nonNull(workstationLookup.getEntity());

        showOrderRibbonItem.setEnabled(isOperationalTaskTypeExecutionOperationInOrder && isOrderSelected);
        showOrderRibbonItem.requestUpdate(true);

        showOperationParametersRibbonItem.setEnabled(isOperationalTaskTypeExecutionOperationInOrder && isOrderSelected && isTechnologyOperationComponentSelected);
        showOperationParametersRibbonItem.requestUpdate(true);

        showOperationalTasksWithOrderRibbonItem.setEnabled(isOperationalTaskTypeExecutionOperationInOrder && isOrderSelected);
        showOperationalTasksWithOrderRibbonItem.requestUpdate(true);

        showWorkstationChangeoverForOperationalTasksRibbonItem.setEnabled(isWorkstationSelected);
        showWorkstationChangeoverForOperationalTasksRibbonItem.requestUpdate(true);
    }

    public void fillCriteriaModifiers(final ViewDefinitionState view) {
        FormComponent operationalTaskForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        LookupComponent staffLookup = (LookupComponent) view.getComponentByReference(L_STAFF_LOOKUP);

        if (Objects.nonNull(operationalTaskForm.getEntityId())) {
            FilterValueHolder filter = staffLookup.getFilterValue();

            filter.put(OperationalTaskDetailsCriteriaModifiers.OPERATIONAL_TASK_ID, operationalTaskForm.getEntityId());

            staffLookup.setFilterValue(filter);
            staffLookup.requestComponentUpdateState();
        }
    }

    private void changedStateField(final ViewDefinitionState view, final List<String> references, final boolean enabled) {
        for (String reference : references) {
            FieldComponent fieldComponent = (FieldComponent) view.getComponentByReference(reference);

            fieldComponent.setEnabled(enabled);
        }
    }

    private void clearFieldValue(final ViewDefinitionState view, final List<String> references) {
        for (String reference : references) {
            FieldComponent fieldComponent = (FieldComponent) view.getComponentByReference(reference);

            fieldComponent.setFieldValue(null);
            fieldComponent.requestComponentUpdateState();
        }
    }

    public void setNameAndDescription(final ViewDefinitionState view) {
        LookupComponent technologyOperationComponentLookup = (LookupComponent) view
                .getComponentByReference(OperationalTaskFields.TECHNOLOGY_OPERATION_COMPONENT);
        FieldComponent nameField = (FieldComponent) view.getComponentByReference(OperationalTaskFields.NAME);
        FieldComponent descriptionField = (FieldComponent) view.getComponentByReference(OperationalTaskFields.DESCRIPTION);

        Entity technologyOperationComponent = technologyOperationComponentLookup.getEntity();

        if (Objects.isNull(technologyOperationComponent)) {
            nameField.setFieldValue(null);
            descriptionField.setFieldValue(null);
        } else {
            Entity operation = technologyOperationComponent.getBelongsToField(TechnologyOperationComponentFields.OPERATION);

            descriptionField.setFieldValue(technologyOperationComponent.getStringField(TechnologyOperationComponentFields.COMMENT));

            if (!Objects.isNull(operation)) {
                nameField.setFieldValue(operation.getStringField(OperationFields.NAME));
            }
        }

        nameField.requestComponentUpdateState();
        descriptionField.requestComponentUpdateState();
    }

    public void setHasChangeovers(final ViewDefinitionState view) {
        FormComponent operationalTaskForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        CheckBoxComponent hasChangeoversCheckBox = (CheckBoxComponent) view.getComponentByReference(OperationalTaskFields.HAS_CHANGEOVERS);

        Long operationalTaskId = operationalTaskForm.getEntityId();

        if (Objects.nonNull(operationalTaskId)) {
            Entity operationalTaskWithColorDto = getOperationalTaskWithColorDtoDD().get(operationalTaskId);

            if (Objects.nonNull(operationalTaskWithColorDto)) {
                boolean hasChangeovers = operationalTaskWithColorDto.getBooleanField(OperationalTaskWithColorDtoFields.HAS_CHANGEOVERS);

                hasChangeoversCheckBox.setChecked(hasChangeovers);
                hasChangeoversCheckBox.requestComponentUpdateState();
            }
        }
    }

    private DataDefinition getOperationalTaskDD() {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_OPERATIONAL_TASK);
    }

    private DataDefinition getOperationalTaskDtoDD() {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_OPERATIONAL_TASK_DTO);
    }

    private DataDefinition getOperationalTaskWithColorDtoDD() {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_OPERATIONAL_TASK_WITH_COLOR_DTO);
    }

}
