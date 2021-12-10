/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.orders.hooks;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.orders.OperationalTasksService;
import com.qcadoo.mes.orders.constants.OperationalTaskDtoFields;
import com.qcadoo.mes.orders.constants.OperationalTaskFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.technologies.constants.OperationFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.api.utils.NumberGeneratorService;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class OperationalTasksDetailsHooks {

    private static final String L_ORDER = "order";

    private static final String L_TECHNOLOGY_OPERATION_COMPONENT = "technologyOperationComponent";

    private static final String L_OPERATIONAL_TASKS = "operationalTasks";

    private static final String L_SHOW_ORDER = "showOrder";

    private static final String L_SHOW_OPERATION_PARAMETERS = "showOperationParameters";

    private static final String L_SHOW_OPERATIONAL_TASKS_WITH_ORDER = "showOperationalTasksWithOrder";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private OperationalTasksService operationalTasksService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    public void beforeRender(final ViewDefinitionState view) {
        generateOperationalTasksNumber(view);
        filterDivisionLookup(view);
        setTechnology(view);
        setQuantities(view);

        disableFieldsWhenOrderTypeIsSelected(view);
        disableButtons(view);
    }

    private void setQuantities(final ViewDefinitionState view) {
        final FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        if (Objects.nonNull(form.getEntityId())) {
            Entity ot = form.getEntity();
            Entity product = ot.getBelongsToField(OperationalTaskFields.PRODUCT);
            if (Objects.nonNull(product)) {
                Entity otDto = dataDefinitionService
                        .get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_OPERATIONAL_TASK_DTO).get(ot.getId());
                FieldComponent doneInPercentage = (FieldComponent) view.getComponentByReference("doneInPercentage");
                FieldComponent doneInPercentageUnit = (FieldComponent) view.getComponentByReference("doneInPercentageUNIT");
                FieldComponent usedQuantityUnit = (FieldComponent) view.getComponentByReference("usedQuantityUNIT");
                FieldComponent plannedQuantityUnit = (FieldComponent) view.getComponentByReference("plannedQuantityUNIT");
                FieldComponent usedQuantity = (FieldComponent) view.getComponentByReference("usedQuantity");
                FieldComponent plannedQuantity = (FieldComponent) view.getComponentByReference("plannedQuantity");
                usedQuantityUnit.setFieldValue(product.getStringField(ProductFields.UNIT));
                plannedQuantityUnit.setFieldValue(product.getStringField(ProductFields.UNIT));

                plannedQuantity.setFieldValue(numberService
                        .formatWithMinimumFractionDigits(otDto.getDecimalField(OperationalTaskDtoFields.PLANNED_QUANTITY), 0));
                usedQuantity.setFieldValue(numberService
                        .formatWithMinimumFractionDigits(otDto.getDecimalField(OperationalTaskDtoFields.USED_QUANTITY), 0));
                if (Objects.nonNull(otDto.getDecimalField(OperationalTaskDtoFields.PLANNED_QUANTITY))
                        && otDto.getDecimalField(OperationalTaskDtoFields.PLANNED_QUANTITY).compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal doneInPercentageQuantity = BigDecimalUtils.convertNullToZero(otDto.getDecimalField("usedQuantity"))
                            .multiply(new BigDecimal(100));
                    doneInPercentageQuantity = doneInPercentageQuantity
                            .divide(otDto.getDecimalField(OperationalTaskDtoFields.PLANNED_QUANTITY), MathContext.DECIMAL64);
                    doneInPercentage.setFieldValue(numberService
                            .formatWithMinimumFractionDigits(doneInPercentageQuantity.setScale(0, RoundingMode.CEILING), 0));
                    doneInPercentage.setEnabled(false);
                    doneInPercentageUnit.setFieldValue("%");
                } else {
                    doneInPercentage.setFieldValue(numberService.formatWithMinimumFractionDigits(BigDecimal.ZERO, 0));
                    doneInPercentage.setEnabled(false);
                    doneInPercentageUnit.setFieldValue("%");
                }
            }
        }
    }

    private void generateOperationalTasksNumber(final ViewDefinitionState view) {
        numberGeneratorService.generateAndInsertNumber(view, OrdersConstants.PLUGIN_IDENTIFIER,
                OrdersConstants.MODEL_OPERATIONAL_TASK, QcadooViewConstants.L_FORM, OperationalTaskFields.NUMBER);
    }

    private void filterDivisionLookup(final ViewDefinitionState view) {
        LookupComponent divisionLookup = (LookupComponent) view
                .getComponentByReference(OperationalTaskFields.DIVISION);
        LookupComponent workstationLookup = (LookupComponent) view.getComponentByReference(OperationalTaskFields.WORKSTATION);

        Entity division = divisionLookup.getEntity();

        FilterValueHolder filterValueHolder = workstationLookup.getFilterValue();

        if (Objects.nonNull(division)) {
            Long divisionId = division.getId();
            filterValueHolder.put(OperationalTaskFields.DIVISION, divisionId);
            workstationLookup.setFilterValue(filterValueHolder);
        }
    }

    public void disableFieldsWhenOrderTypeIsSelected(final ViewDefinitionState view) {
        FieldComponent typeField = (FieldComponent) view.getComponentByReference(OperationalTaskFields.TYPE);

        String type = (String) typeField.getFieldValue();

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

    public void disableButtons(final ViewDefinitionState view) {
        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
        FieldComponent typeField = (FieldComponent) view.getComponentByReference(OperationalTaskFields.TYPE);
        LookupComponent orderLookup = (LookupComponent) view.getComponentByReference(OperationalTaskFields.ORDER);
        LookupComponent technologyOperationComponentLookup = (LookupComponent) view
                .getComponentByReference(OperationalTaskFields.TECHNOLOGY_OPERATION_COMPONENT);

        String type = (String) typeField.getFieldValue();

        boolean isOperationalTaskTypeExecutionOperationInOrder = operationalTasksService
                .isOperationalTaskTypeExecutionOperationInOrder(type);
        boolean isOrderSelected = !Objects.isNull(orderLookup.getEntity());
        boolean isTechnologyOperationComponentSelected = !Objects.isNull(technologyOperationComponentLookup.getEntity());

        RibbonGroup order = window.getRibbon().getGroupByName(L_ORDER);
        RibbonGroup technologyOperationComponent = window.getRibbon().getGroupByName(L_TECHNOLOGY_OPERATION_COMPONENT);
        RibbonGroup operationalTasks = window.getRibbon().getGroupByName(L_OPERATIONAL_TASKS);

        RibbonActionItem showOrder = order.getItemByName(L_SHOW_ORDER);
        RibbonActionItem showOperationParameters = technologyOperationComponent.getItemByName(L_SHOW_OPERATION_PARAMETERS);
        RibbonActionItem showOperationalTasksWithOrder = operationalTasks.getItemByName(L_SHOW_OPERATIONAL_TASKS_WITH_ORDER);

        showOrder.setEnabled(isOperationalTaskTypeExecutionOperationInOrder && isOrderSelected);
        showOrder.requestUpdate(true);

        showOperationParameters.setEnabled(
                isOperationalTaskTypeExecutionOperationInOrder && isOrderSelected && isTechnologyOperationComponentSelected);
        showOperationParameters.requestUpdate(true);

        showOperationalTasksWithOrder.setEnabled(isOperationalTaskTypeExecutionOperationInOrder && isOrderSelected);
        showOperationalTasksWithOrder.requestUpdate(true);
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

            descriptionField
                    .setFieldValue(technologyOperationComponent.getStringField(TechnologyOperationComponentFields.COMMENT));

            if (!Objects.isNull(operation)) {
                nameField.setFieldValue(operation.getStringField(OperationFields.NAME));
            }
        }

        nameField.requestComponentUpdateState();
        descriptionField.requestComponentUpdateState();
    }

}
