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

import java.util.Date;
import java.util.Objects;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.UserFields;
import com.qcadoo.mes.orders.constants.OrderTechnologicalProcessFields;
import com.qcadoo.mes.orders.constants.OrderTechnologicalProcessWasteFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.technologies.constants.TechnologicalProcessFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.security.api.UserService;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class OrderTechnologicalProcessWasteDetailsHooks {

    private static final String L_ORDER_TECHNOLOGICAL_PROCESS_ID = "window.mainTab.orderTechnologicalProcessWaste.orderTechnologicalProcessId";

    private static final String L_TECHNOLOGICAL_PROCESS_NAME = "technologicalProcessName";

    private static final String L_WASTE_QUANTITY_UNIT = "wasteQuantityUnit";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private UserService userService;

    public final void onBeforeRender(final ViewDefinitionState view) {
        FormComponent orderTechnologicalProcessWasteForm = (FormComponent) view
                .getComponentByReference(QcadooViewConstants.L_FORM);

        Entity orderTechnologicalProcessWaste = orderTechnologicalProcessWasteForm.getEntity();
        Long orderTechnologicalProcessWasteId = orderTechnologicalProcessWaste.getId();

        if (Objects.nonNull(orderTechnologicalProcessWasteId)) {
            orderTechnologicalProcessWaste = orderTechnologicalProcessWaste.getDataDefinition()
                    .get(orderTechnologicalProcessWasteId);

            setFieldsEnabled(view, orderTechnologicalProcessWaste);

            fillTechnologicalProcessName(view, orderTechnologicalProcessWaste);
            fillUnit(view, orderTechnologicalProcessWaste);
        } else {
            setFieldsEnabled(view, orderTechnologicalProcessWaste);

            fillOrderPackAndTechnologicalProcessName(view);
            fillUnit(view);
            fillDateAndWorker(view);
        }
    }

    public void setFieldsEnabled(final ViewDefinitionState view, final Entity orderTechnologicalProcessWaste) {
        LookupComponent orderTechnologicalProcessLookup = (LookupComponent) view
                .getComponentByReference(OrderTechnologicalProcessWasteFields.ORDER_TECHNOLOGICAL_PROCESS);

        boolean isSaved = Objects.nonNull(orderTechnologicalProcessWaste.getId());

        orderTechnologicalProcessLookup.setEnabled(!isSaved);
    }

    private void fillUnit(final ViewDefinitionState view, final Entity orderTechnologicalProcessWaste) {
        FieldComponent quantityUnit = (FieldComponent) view.getComponentByReference(L_WASTE_QUANTITY_UNIT);

        Entity product = orderTechnologicalProcessWaste.getBelongsToField(OrderTechnologicalProcessWasteFields.PRODUCT);

        if (Objects.nonNull(product)) {
            String unit = product.getStringField(ProductFields.UNIT);

            quantityUnit.setFieldValue(unit);
            quantityUnit.requestComponentUpdateState();
        }
    }

    private void fillUnit(final ViewDefinitionState view) {
        FieldComponent quantityUnit = (FieldComponent) view.getComponentByReference(L_WASTE_QUANTITY_UNIT);

        LookupComponent orderTechnologicalProcessLookup = (LookupComponent) view
                .getComponentByReference(OrderTechnologicalProcessWasteFields.ORDER_TECHNOLOGICAL_PROCESS);

        Entity orderTechnologicalProcess = orderTechnologicalProcessLookup.getEntity();

        if (Objects.nonNull(orderTechnologicalProcess)) {
            Entity product = orderTechnologicalProcess.getBelongsToField(OrderTechnologicalProcessFields.PRODUCT);

            if (Objects.nonNull(product)) {
                String unit = product.getStringField(ProductFields.UNIT);

                quantityUnit.setFieldValue(unit);
                quantityUnit.requestComponentUpdateState();
            }
        }
    }

    private void fillOrderPackAndTechnologicalProcessName(final ViewDefinitionState view) {
        LookupComponent orderTechnologicalProcessLookup = (LookupComponent) view
                .getComponentByReference(OrderTechnologicalProcessWasteFields.ORDER_TECHNOLOGICAL_PROCESS);
        LookupComponent orderPackLookup = (LookupComponent) view
                .getComponentByReference(OrderTechnologicalProcessWasteFields.ORDER_PACK);
        FieldComponent technologicalProcessNameField = (FieldComponent) view
                .getComponentByReference(L_TECHNOLOGICAL_PROCESS_NAME);

        Entity orderTechnologicalProcess = orderTechnologicalProcessLookup.getEntity();

        if (view.isViewAfterRedirect()) {
            Long orderTechnologicalProcessId;

            try {
                orderTechnologicalProcessId = Long
                        .valueOf(view.getJsonContext().get(L_ORDER_TECHNOLOGICAL_PROCESS_ID).toString());
            } catch (JSONException e) {
                orderTechnologicalProcessId = 0L;
            }

            orderTechnologicalProcess = getOrderTechnologicalProcessDD().get(orderTechnologicalProcessId);
        }
        if (Objects.nonNull(orderTechnologicalProcess)) {
            orderTechnologicalProcessLookup.setFieldValue(orderTechnologicalProcess.getId());
            orderTechnologicalProcessLookup.requestComponentUpdateState();

            Entity orderPack = orderTechnologicalProcess.getBelongsToField(OrderTechnologicalProcessFields.ORDER_PACK);
            Entity technologicalProcess = orderTechnologicalProcess
                    .getBelongsToField(OrderTechnologicalProcessFields.TECHNOLOGICAL_PROCESS);

            if (Objects.nonNull(orderPack)) {
                orderPackLookup.setFieldValue(orderPack.getId());
            } else {
                orderPackLookup.setFieldValue(null);
            }
            orderPackLookup.requestComponentUpdateState();
            if (Objects.nonNull(technologicalProcess)) {
                technologicalProcessNameField.setFieldValue(technologicalProcess.getStringField(TechnologicalProcessFields.NAME));
                technologicalProcessNameField.requestComponentUpdateState();
            }
        }
    }

    private void fillTechnologicalProcessName(final ViewDefinitionState view, final Entity orderTechnologicalProcessWaste) {
        FieldComponent technologicalProcessNameField = (FieldComponent) view
                .getComponentByReference(L_TECHNOLOGICAL_PROCESS_NAME);

        Entity technologicalProcess = orderTechnologicalProcessWaste
                .getBelongsToField(OrderTechnologicalProcessWasteFields.TECHNOLOGICAL_PROCESS);

        if (Objects.nonNull(technologicalProcess)) {
            technologicalProcessNameField.setFieldValue(technologicalProcess.getStringField(TechnologicalProcessFields.NAME));
            technologicalProcessNameField.requestComponentUpdateState();
        }
    }

    private void fillDateAndWorker(final ViewDefinitionState view) {
        FieldComponent dateField = (FieldComponent) view.getComponentByReference(OrderTechnologicalProcessWasteFields.DATE);
        LookupComponent workerLookup = (LookupComponent) view
                .getComponentByReference(OrderTechnologicalProcessWasteFields.WORKER);

        if (view.isViewAfterRedirect()) {
            Entity currentUser = userService.getCurrentUserEntity();

            Entity worker = currentUser.getBelongsToField(UserFields.STAFF);

            dateField.setFieldValue(DateUtils.toDateTimeString(new Date()));

            if (Objects.nonNull(worker)) {
                workerLookup.setFieldValue(worker.getId());
                workerLookup.requestComponentUpdateState();
            }
        }
    }

    private DataDefinition getOrderTechnologicalProcessDD() {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER_TECHNOLOGICAL_PROCESS);
    }

}
