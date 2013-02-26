/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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

import static com.qcadoo.mes.orders.constants.OrderFields.COMMENT_REASON_TYPE_CORRECTION_DATE_FROM;
import static com.qcadoo.mes.orders.constants.OrderFields.COMMENT_REASON_TYPE_CORRECTION_DATE_TO;
import static com.qcadoo.mes.orders.constants.OrderFields.COMMENT_REASON_TYPE_DEVIATIONS_OF_EFFECTIVE_END;
import static com.qcadoo.mes.orders.constants.OrderFields.COMMENT_REASON_TYPE_DEVIATIONS_OF_EFFECTIVE_START;
import static com.qcadoo.mes.orders.constants.OrderFields.COMPANY;
import static com.qcadoo.mes.orders.constants.OrderFields.CORRECTED_DATE_FROM;
import static com.qcadoo.mes.orders.constants.OrderFields.CORRECTED_DATE_TO;
import static com.qcadoo.mes.orders.constants.OrderFields.DATE_FROM;
import static com.qcadoo.mes.orders.constants.OrderFields.DATE_TO;
import static com.qcadoo.mes.orders.constants.OrderFields.DEADLINE;
import static com.qcadoo.mes.orders.constants.OrderFields.EXTERNAL_NUMBER;
import static com.qcadoo.mes.orders.constants.OrderFields.EXTERNAL_SYNCHRONIZED;
import static com.qcadoo.mes.orders.constants.OrderFields.NAME;
import static com.qcadoo.mes.orders.constants.OrderFields.REASON_TYPE_CORRECTION_DATE_FROM;
import static com.qcadoo.mes.orders.constants.OrderFields.REASON_TYPE_CORRECTION_DATE_TO;
import static com.qcadoo.mes.orders.constants.OrderFields.REASON_TYPE_DEVIATIONS_OF_EFFECTIVE_END;
import static com.qcadoo.mes.orders.constants.OrderFields.REASON_TYPE_DEVIATIONS_OF_EFFECTIVE_START;
import static com.qcadoo.mes.orders.constants.OrderFields.STATE;
import static com.qcadoo.mes.orders.constants.OrdersConstants.BASIC_MODEL_PRODUCT;
import static com.qcadoo.mes.orders.constants.OrdersConstants.FIELD_FORM;
import static com.qcadoo.mes.orders.constants.OrdersConstants.FIELD_NUMBER;
import static com.qcadoo.mes.orders.constants.OrdersConstants.MODEL_ORDER;
import static com.qcadoo.mes.orders.constants.OrdersConstants.PLANNED_QUANTITY;
import static com.qcadoo.mes.orders.states.constants.OrderStateChangeFields.STATUS;
import static com.qcadoo.mes.states.constants.StateChangeStatus.SUCCESSFUL;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.google.common.collect.Lists;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.states.OrderStateService;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.mes.states.service.client.util.StateChangeHistoryService;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.CustomRestriction;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;

@Service
public class OrderDetailsHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private OrderStateService orderStateService;

    @Autowired
    private StateChangeHistoryService stateChangeHistoryService;

    public void changedEnabledFieldForSpecificOrderState(final ViewDefinitionState view) {
        final FormComponent form = (FormComponent) view.getComponentByReference("form");
        if (form.getEntityId() == null) {
            return;
        }
        final Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                form.getEntityId());

        if (order.getStringField(STATE).equals(OrderState.PENDING.getStringValue())) {
            List<String> references = Arrays.asList(CORRECTED_DATE_FROM, CORRECTED_DATE_TO, REASON_TYPE_CORRECTION_DATE_FROM,
                    REASON_TYPE_CORRECTION_DATE_TO, REASON_TYPE_DEVIATIONS_OF_EFFECTIVE_END,
                    REASON_TYPE_DEVIATIONS_OF_EFFECTIVE_START, COMMENT_REASON_TYPE_DEVIATIONS_OF_EFFECTIVE_END,
                    COMMENT_REASON_TYPE_DEVIATIONS_OF_EFFECTIVE_START, COMMENT_REASON_TYPE_CORRECTION_DATE_TO,
                    COMMENT_REASON_TYPE_CORRECTION_DATE_FROM);
            changedEnabledFields(view, references, false);
        }
        if (order.getStringField(STATE).equals(OrderState.ACCEPTED.getStringValue())) {
            List<String> references = Arrays.asList(CORRECTED_DATE_FROM, CORRECTED_DATE_TO, REASON_TYPE_CORRECTION_DATE_FROM,
                    COMMENT_REASON_TYPE_CORRECTION_DATE_FROM, REASON_TYPE_CORRECTION_DATE_TO,
                    COMMENT_REASON_TYPE_CORRECTION_DATE_TO, DATE_FROM, DATE_TO);
            changedEnabledFields(view, references, true);
        }
        if (order.getStringField(STATE).equals(OrderState.IN_PROGRESS.getStringValue())) {
            List<String> references = Arrays.asList(DATE_FROM, DATE_TO, CORRECTED_DATE_TO, REASON_TYPE_CORRECTION_DATE_TO,
                    COMMENT_REASON_TYPE_CORRECTION_DATE_TO);
            changedEnabledFields(view, references, true);
        }
    }

    private void changedEnabledFields(final ViewDefinitionState view, final List<String> references, final boolean enabled) {
        for (String reference : references) {
            FieldComponent field = (FieldComponent) view.getComponentByReference(reference);
            field.setEnabled(enabled);
            field.requestComponentUpdateState();
        }
    }

    public void disableOrderFormForExternalItems(final ViewDefinitionState state) {
        FormComponent form = (FormComponent) state.getComponentByReference(FIELD_FORM);

        if (form.getEntityId() == null) {
            return;
        }
        Entity entity = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, MODEL_ORDER).get(form.getEntityId());
        if (entity == null) {
            return;
        }
        String externalNumber = entity.getStringField(EXTERNAL_NUMBER);
        boolean externalSynchronized = (Boolean) entity.getField(EXTERNAL_SYNCHRONIZED);

        if (StringUtils.hasText(externalNumber) || !externalSynchronized) {
            state.getComponentByReference(FIELD_NUMBER).setEnabled(false);
            state.getComponentByReference(NAME).setEnabled(false);
            state.getComponentByReference(COMPANY).setEnabled(false);
            state.getComponentByReference(DEADLINE).setEnabled(false);
            state.getComponentByReference(BASIC_MODEL_PRODUCT).setEnabled(false);
            state.getComponentByReference(PLANNED_QUANTITY).setEnabled(false);
        }
    }

    public void filterStateChangeHistory(final ViewDefinitionState view) {
        final GridComponent historyGrid = (GridComponent) view.getComponentByReference("grid");
        final CustomRestriction onlySuccessfulRestriction = stateChangeHistoryService.buildStatusRestriction(STATUS,
                Lists.newArrayList(SUCCESSFUL.getStringValue()));
        historyGrid.setCustomRestriction(onlySuccessfulRestriction);
    }

    public void disabledRibbonWhenOrderIsSynchronized(final ViewDefinitionState view) {
        WindowComponent window = (WindowComponent) view.getComponentByReference("window");
        Ribbon ribbon = window.getRibbon();
        List<RibbonGroup> ribbonGroups = ribbon.getGroups();
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Long orderId = form.getEntityId();
        if (orderId == null) {
            return;
        }
        if (orderStateService.isSynchronized(form.getEntity().getDataDefinition().get(orderId))) {
            return;
        }
        for (RibbonGroup ribbonGroup : ribbonGroups) {
            for (RibbonActionItem actionItem : ribbonGroup.getItems()) {
                actionItem.setEnabled(false);
                actionItem.requestUpdate(true);
            }
        }
        RibbonActionItem refresh = ribbon.getGroupByName("actions").getItemByName("refresh");
        RibbonActionItem back = ribbon.getGroupByName("navigation").getItemByName("back");
        refresh.setEnabled(true);
        back.setEnabled(true);
        refresh.requestUpdate(true);
        back.requestUpdate(true);
        form.setFormEnabled(false);
    }

    public void compareDeadlineAndEndDate(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        if (form.getEntityId() == null) {
            return;
        }
        FieldComponent finishDateComponent = (FieldComponent) view.getComponentByReference(DATE_TO);
        FieldComponent deadlineDateComponent = (FieldComponent) view.getComponentByReference(DEADLINE);
        Date finishDate = DateUtils.parseDate(finishDateComponent.getFieldValue());
        Date deadlineDate = DateUtils.parseDate(deadlineDateComponent.getFieldValue());
        if (finishDate != null && deadlineDate != null && deadlineDate.before(finishDate)) {
            form.addMessage("orders.validate.global.error.deadline", MessageType.INFO, false);
        }
    }

    public void compareDeadlineAndStartDate(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        if (form.getEntityId() == null) {
            return;
        }
        FieldComponent startDateComponent = (FieldComponent) view.getComponentByReference(DATE_FROM);
        FieldComponent finishDateComponent = (FieldComponent) view.getComponentByReference(DATE_TO);
        FieldComponent deadlineDateComponent = (FieldComponent) view.getComponentByReference(DEADLINE);
        Date startDate = DateUtils.parseDate(startDateComponent.getFieldValue());
        Date finidhDate = DateUtils.parseDate(finishDateComponent.getFieldValue());
        Date deadlineDate = DateUtils.parseDate(deadlineDateComponent.getFieldValue());
        if (startDate != null && deadlineDate != null && finidhDate == null && deadlineDate.before(startDate)) {
            form.addMessage("orders.validate.global.error.deadlineBeforeStartDate", MessageType.INFO, false);
        }
    }
}
