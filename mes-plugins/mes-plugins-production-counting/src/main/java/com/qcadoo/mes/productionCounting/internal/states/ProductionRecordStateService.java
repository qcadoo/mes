/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.2
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
package com.qcadoo.mes.productionCounting.internal.states;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.productionCounting.internal.logging.ProductionRecordLoggingService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.validators.ErrorMessage;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class ProductionRecordStateService {

    @Autowired
    private ProductionRecordLoggingService loggingService;

    private static final String STATE_FIELD = "state";

    public void changeRecordState(final ViewDefinitionState view, final ComponentState component, final String[] args) {
        final String targetState = getTargetStateFromArgs(args);
        final FormComponent form = (FormComponent) view.getComponentByReference("form");
        final Entity record = form.getEntity();

        setRecordState(view, component, record, targetState);
    }

    private void setRecordState(final ViewDefinitionState view, final ComponentState component, final Entity record,
            final String targetState) {
        if (record == null) {
            return;
        }

        final boolean sourceComponentIsForm = component instanceof FormComponent;
        final DataDefinition recordDataDefinition = record.getDataDefinition();
        final ComponentState stateFieldComponent = view.getComponentByReference(STATE_FIELD);

        ProductionCountingStates oldState = ProductionCountingStates.parseString(record.getStringField(STATE_FIELD));
        ProductionCountingStates newState = ProductionCountingStates.parseString(targetState);

        if (newState.equals(oldState)) {
            return;
        }

        if (sourceComponentIsForm) {
            stateFieldComponent.setFieldValue(newState.getStringValue());
            component.performEvent(view, "save", new String[0]);

            Entity savedRecord = recordDataDefinition.get(record.getId());
            stateFieldComponent.setFieldValue(savedRecord.getStringField(STATE_FIELD));

            loggingService.logStateChange(savedRecord, oldState, newState);
        } else {
            record.setField(STATE_FIELD, newState.getStringValue());
            Entity savedRecord = recordDataDefinition.save(record);

            loggingService.logStateChange(record, oldState, newState);

            List<ErrorMessage> errorMessages = Lists.newArrayList();
            errorMessages.addAll(savedRecord.getErrors().values());
            errorMessages.addAll(savedRecord.getGlobalErrors());

            for (ErrorMessage message : errorMessages) {
                view.getComponentByReference("grid").addMessage(message.getMessage(), MessageType.INFO);
            }
        }

    }

    public void disabledFieldWhenStateNotDraft(final ViewDefinitionState view) {
        final FormComponent form = (FormComponent) view.getComponentByReference("form");
        if (form.getEntity() == null) {
            return;
        }
        final Entity productionRecord = form.getEntity();
        String states = productionRecord.getStringField(STATE_FIELD);
        if (!states.equals(ProductionCountingStates.DRAFT.getStringValue())) {
            for (String reference : Arrays.asList("lastRecord", "number", "order", "orderOperationComponent", "shift",
                    "machineTime", "laborTime")) {
                FieldComponent field = (FieldComponent) view.getComponentByReference(reference);
                field.setEnabled(false);
                field.requestComponentUpdateState();
            }
            GridComponent gridProductInComponent = (GridComponent) view
                    .getComponentByReference("recordOperationProductInComponent");
            gridProductInComponent.setEditable(false);
            GridComponent gridProductOutComponent = (GridComponent) view
                    .getComponentByReference("recordOperationProductOutComponent");
            gridProductOutComponent.setEditable(false);
        }
    }

    private String getTargetStateFromArgs(final String[] args) {
        String retval = "";
        if (args != null && args.length > 0 && !"null".equals(args[0])) {
            retval = args[0];
        }
        return retval;
    }
}
