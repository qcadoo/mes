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
package com.qcadoo.mes.operationalTasksForOrders.hooks;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.operationalTasks.constants.OperationalTaskFields;
import com.qcadoo.mes.operationalTasks.constants.OperationalTasksConstants;
import com.qcadoo.mes.operationalTasksForOrders.OperationalTasksForOrdersService;
import com.qcadoo.mes.operationalTasksForOrders.constants.OperationalTaskFieldsOTFO;
import com.qcadoo.mes.operationalTasksForOrders.constants.TechOperCompOperationalTasksFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.technologies.constants.OperationFields;
import com.qcadoo.mes.technologies.constants.OperationProductOutComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;

@Service
public class OperationalTaskDetailsHooksOTFO {

    private static final String L_WINDOW = "window";

    private static final String L_FORM = "form";

    private static final String L_ORDER = "order";

    private static final String L_TECHNOLOGY_OPERATION_COMPONENT = "technologyOperationComponent";

    private static final String L_OPERATIONAL_TASKS = "operationalTasks";

    private static final String L_SHOW_ORDER = "showOrder";

    private static final String L_SHOW_OPERATION_PARAMETERS = "showOperationParameters";

    private static final String L_SHOW_OPERATIONAL_TASKS_WITH_ORDER = "showOperationalTasksWithOrder";

    private static final String PLANNED_QUANTITY_UNIT = "plannedQuantityUNIT";

    private static final String USED_QUANTITY_UNIT = "usedQuantityUNIT";

    @Autowired
    private DataDefinitionService dataDefinitionService;

	@Autowired
	private NumberService numberService;

    @Autowired
    private OperationalTasksForOrdersService operationalTasksForOrdersService;

    public void beforeRender(final ViewDefinitionState view) {
        setTechnology(view);
        setTechnologyOperationComponent(view);
        setAdditionalFields(view);
        disableFieldsWhenOrderTypeIsSelected(view);
        disableButtons(view);
    }

    public void disableFieldsWhenOrderTypeIsSelected(final ViewDefinitionState view) {
        FieldComponent typeField = (FieldComponent) view.getComponentByReference(OperationalTaskFields.TYPE);

        String type = (String) typeField.getFieldValue();

        List<String> referenceBasicFields = Lists.newArrayList(OperationalTaskFields.NAME, OperationalTaskFields.PRODUCTION_LINE,
                OperationalTaskFields.DESCRIPTION);
        List<String> extendFields = Lists.newArrayList(OperationalTaskFieldsOTFO.ORDER,
                OperationalTaskFieldsOTFO.TECHNOLOGY_OPERATION_COMPONENT);

        if (operationalTasksForOrdersService.isOperationalTaskTypeOtherCase(type)) {
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
        WindowComponent window = (WindowComponent) view.getComponentByReference(L_WINDOW);
        FieldComponent typeField = (FieldComponent) view.getComponentByReference(OperationalTaskFields.TYPE);
        LookupComponent orderLookup = (LookupComponent) view.getComponentByReference(OperationalTaskFieldsOTFO.ORDER);
        LookupComponent technologyOperationComponentLookup = (LookupComponent) view
                .getComponentByReference(OperationalTaskFieldsOTFO.TECHNOLOGY_OPERATION_COMPONENT);

        String type = (String) typeField.getFieldValue();

        boolean isOperationalTaskTypeExecutionOperationInOrder = operationalTasksForOrdersService
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
        LookupComponent technologyLookup = (LookupComponent) view.getComponentByReference(OperationalTaskFieldsOTFO.TECHNOLOGY);
        LookupComponent orderLookup = (LookupComponent) view.getComponentByReference(OperationalTaskFieldsOTFO.ORDER);

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

    private void setTechnologyOperationComponent(final ViewDefinitionState view) {
        FormComponent operationalTaskForm = (FormComponent) view.getComponentByReference(L_FORM);

        FieldComponent typeField = (FieldComponent) view.getComponentByReference(OperationalTaskFields.TYPE);
        LookupComponent orderLookup = (LookupComponent) view.getComponentByReference(OperationalTaskFieldsOTFO.ORDER);
        LookupComponent technologyOperationComponentLookup = (LookupComponent) view
                .getComponentByReference(OperationalTaskFieldsOTFO.TECHNOLOGY_OPERATION_COMPONENT);

        Long operationalTaskId = operationalTaskForm.getEntityId();

        if (Objects.isNull(operationalTaskId)) {
            return;
        }

        String type = (String) typeField.getFieldValue();

        if (operationalTasksForOrdersService.isOperationalTaskTypeExecutionOperationInOrder(type)) {
            if (!Objects.isNull(orderLookup.getEntity()) && Objects.isNull(technologyOperationComponentLookup.getEntity())) {
                Entity operationalTask = getOperationalTaskDD().get(operationalTaskId);

                if (!Objects.isNull(operationalTask)) {
                    Entity techOperCompOperationalTask = operationalTask
                            .getBelongsToField(OperationalTaskFieldsOTFO.TECH_OPER_COMP_OPERATIONAL_TASK);

                    if (!Objects.isNull(techOperCompOperationalTask)) {
                        Entity technologyOperationComponent = techOperCompOperationalTask
                                .getBelongsToField(TechOperCompOperationalTasksFields.TECHNOLOGY_OPERATION_COMPONENT);

                        technologyOperationComponentLookup.setFieldValue(technologyOperationComponent.getId());
                        technologyOperationComponentLookup.requestComponentUpdateState();
                    }
                }
            }
        } else {
            technologyOperationComponentLookup.setFieldValue(null);
            technologyOperationComponentLookup.requestComponentUpdateState();
        }
    }

    public void setNameAndDescription(final ViewDefinitionState view) {
        LookupComponent technologyOperationComponentLookup = (LookupComponent) view
                .getComponentByReference(OperationalTaskFieldsOTFO.TECHNOLOGY_OPERATION_COMPONENT);
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

    public void setAdditionalFields(final ViewDefinitionState view) {
        LookupComponent technologyOperationComponentLookup = (LookupComponent) view
                .getComponentByReference(OperationalTaskFieldsOTFO.TECHNOLOGY_OPERATION_COMPONENT);
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(OperationalTaskFieldsOTFO.PRODUCT);
        FieldComponent plannedQuantityField = (FieldComponent) view
                .getComponentByReference(OperationalTaskFieldsOTFO.PLANNED_QUANTITY);
        FieldComponent plannedQuantityUnitField = (FieldComponent) view.getComponentByReference(PLANNED_QUANTITY_UNIT);
        FieldComponent usedQuantityField = (FieldComponent) view.getComponentByReference(OperationalTaskFieldsOTFO.USED_QUANTITY);
        FieldComponent usedQuantityUnitField = (FieldComponent) view.getComponentByReference(USED_QUANTITY_UNIT);

        Entity technologyOperationComponent = technologyOperationComponentLookup.getEntity();

        if (Objects.isNull(technologyOperationComponent)) {
            productLookup.setFieldValue(null);
            plannedQuantityField.setFieldValue(null);
            plannedQuantityUnitField.setFieldValue(null);
            usedQuantityField.setFieldValue(null);
            usedQuantityUnitField.setFieldValue(null);
        } else {
            Entity operationProductOutComponent = getOperationProductOutComponent(technologyOperationComponent);

            if (!Objects.isNull(operationProductOutComponent)) {
                Entity product = operationProductOutComponent.getBelongsToField(OperationProductOutComponentFields.PRODUCT);
                BigDecimal plannedQuantity = operationProductOutComponent.getDecimalField(OperationProductOutComponentFields.QUANTITY);
				BigDecimal usedQuantity = BigDecimal.ZERO;

                String unit = product.getStringField(ProductFields.UNIT);

                productLookup.setFieldValue(product.getId());
                plannedQuantityField.setFieldValue(numberService.formatWithMinimumFractionDigits(plannedQuantity, 0));
                plannedQuantityUnitField.setFieldValue(unit);
                usedQuantityField.setFieldValue(numberService.formatWithMinimumFractionDigits(usedQuantity, 0));
                usedQuantityUnitField.setFieldValue(unit);
            }
        }

        productLookup.requestComponentUpdateState();
        plannedQuantityField.requestComponentUpdateState();
        plannedQuantityUnitField.requestComponentUpdateState();
        usedQuantityField.requestComponentUpdateState();
        usedQuantityUnitField.requestComponentUpdateState();
    }

    public Entity getOperationProductOutComponent(final Entity technologyOperationComponent) {
        return getOperationProductOutComponentDD().find().add(SearchRestrictions
                .belongsTo(OperationProductOutComponentFields.OPERATION_COMPONENT, technologyOperationComponent)).setMaxResults(1)
                .uniqueResult();
    }

    private DataDefinition getOperationalTaskDD() {
        return dataDefinitionService.get(OperationalTasksConstants.PLUGIN_IDENTIFIER,
                OperationalTasksConstants.MODEL_OPERATIONAL_TASK);
    }

    private DataDefinition getOperationProductOutComponentDD() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_OPERATION_PRODUCT_OUT_COMPONENT);
    }

}
