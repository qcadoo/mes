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
package com.qcadoo.mes.productionScheduling.hooks;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.states.constants.OrderStateStringValues;
import com.qcadoo.mes.productionScheduling.constants.OrderFieldsPS;
import com.qcadoo.mes.productionScheduling.criteriaModifiers.OperCompTimeCalculationsCM;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.*;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class OperationDurationDetailsInOrderHooks {

    



    private static final String L_GENERATED_END_DATE = "generatedEndDate";

    private static final String L_OPERATIONAL_TASKS = "operationalTasks";

    private static final String L_CREATE_OPERATIONAL_TASKS = "createOperationalTasks";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ParameterService parameterService;

    public void onBeforeRender(final ViewDefinitionState view) {
        FormComponent orderForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        if (orderForm.getEntityId() == null && view.isViewAfterRedirect()) {
            CheckBoxComponent includeTpzField = (CheckBoxComponent) view.getComponentByReference(OrderFieldsPS.INCLUDE_TPZ);
            boolean checkIncludeTpzField = parameterService.getParameter().getBooleanField("includeTpzPS");
            includeTpzField.setChecked(checkIncludeTpzField);
            includeTpzField.requestComponentUpdateState();

            CheckBoxComponent includeAdditionalTimeField = (CheckBoxComponent) view.getComponentByReference(OrderFieldsPS.INCLUDE_ADDITIONAL_TIME);
            boolean checkIncludeAdditionalTimeField = parameterService.getParameter().getBooleanField("includeAdditionalTimePS");
            includeAdditionalTimeField.setChecked(checkIncludeAdditionalTimeField);
            includeAdditionalTimeField.requestComponentUpdateState();
        }
        fillUnitField(view);
        disableCopyRealizationTimeButton(view);
        setCriteriaModifierParameters(view);
        disableCreateButton(view);
    }

    private void setCriteriaModifierParameters(ViewDefinitionState view) {
        FormComponent orderForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Long orderId = orderForm.getEntityId();
        GridComponent grid = (GridComponent) view.getComponentByReference("operCompTimeCalculationsGrid");
        FilterValueHolder holder = grid.getFilterValue();
        holder.put(OperCompTimeCalculationsCM.ORDER_PARAMETER, orderId);
        grid.setFilterValue(holder);

    }

    private void fillUnitField(final ViewDefinitionState view) {
        FormComponent orderForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        FieldComponent unitField = (FieldComponent) view.getComponentByReference(OrderFieldsPS.OPERATION_DURATION_QUANTITY_UNIT);

        Long orderId = orderForm.getEntityId();

        if (orderId != null) {
            Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(orderId);

            if (order != null) {
                Entity product = order.getBelongsToField(OrderFields.PRODUCT);

                if (product != null) {
                    unitField.setFieldValue(product.getField(ProductFields.UNIT));
                }
            }
        }
    }

    private void disableCopyRealizationTimeButton(final ViewDefinitionState view) {
        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);

        RibbonGroup realizationTimeGroup = window.getRibbon().getGroupByName("operationDuration");
        RibbonActionItem realizationTime = realizationTimeGroup.getItemByName("copy");

        if (isGenerated(view)) {
            realizationTime.setEnabled(true);
        } else {
            realizationTime.setEnabled(false);
        }

        realizationTime.requestUpdate(true);
    }

    private boolean isGenerated(final ViewDefinitionState view) {
        FieldComponent generatedEndDateField = (FieldComponent) view.getComponentByReference(L_GENERATED_END_DATE);

        return !StringUtils.isEmpty((String) generatedEndDateField.getFieldValue());
    }

    void disableCreateButton(final ViewDefinitionState view) {
        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);

        RibbonGroup operationalTasks = window.getRibbon().getGroupByName(L_OPERATIONAL_TASKS);
        RibbonActionItem createOperationalTasks = operationalTasks.getItemByName(L_CREATE_OPERATIONAL_TASKS);

        if (isGenerated(view) && orderHasTechnologyAndCorrectState(view)) {
            createOperationalTasks.setEnabled(true);
        } else {
            createOperationalTasks.setEnabled(false);
        }

        createOperationalTasks.requestUpdate(true);
    }

    private boolean orderHasTechnologyAndCorrectState(final ViewDefinitionState view) {
        FormComponent orderForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Long orderId = orderForm.getEntityId();

        if (Objects.isNull(orderId)) {
            return false;
        }

        Entity order = orderForm.getEntity().getDataDefinition().get(orderId);

        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);
        String state = order.getStringField(OrderFields.STATE);

        return (!Objects.isNull(technology) && checkIfOrderStateIsCorrect(state));
    }

    private boolean checkIfOrderStateIsCorrect(final String state) {
        return (OrderStateStringValues.PENDING.equals(state) || OrderStateStringValues.ACCEPTED.equals(state)
                || OrderStateStringValues.IN_PROGRESS.equals(state) || OrderStateStringValues.INTERRUPTED.equals(state));
    }
}
