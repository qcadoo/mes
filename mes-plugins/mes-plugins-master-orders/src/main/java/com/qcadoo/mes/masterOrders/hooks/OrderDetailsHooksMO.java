/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
package com.qcadoo.mes.masterOrders.hooks;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.masterOrders.constants.MasterOrderFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrdersConstants;
import com.qcadoo.mes.masterOrders.constants.OrderFieldsMO;
import com.qcadoo.mes.masterOrders.util.MasterOrderOrdersDataProvider;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class OrderDetailsHooksMO {

    private static final String L_FORM = "form";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private MasterOrderOrdersDataProvider masterOrderOrdersDataProvider;

    public void fillMasterOrderFields(final ViewDefinitionState view) {
        FormComponent orderForm = (FormComponent) view.getComponentByReference(L_FORM);

        Entity order = orderForm.getEntity();

        Entity masterOrder = order.getBelongsToField(OrderFieldsMO.MASTER_ORDER);
        Entity masterOrderProduct = order.getBelongsToField(OrderFieldsMO.MASTER_ORDER_PRODUCT);

        if (order.getId() == null) {

            if (masterOrder != null) {
                Long masterOrderId = masterOrder.getId();

                masterOrder = getMasterOrder(masterOrderId);

                fillMasterOrderFields(view, masterOrder, masterOrderProduct);
            }
        }
        if (masterOrder != null) {
            disableOrderType(view);
        }
    }

    private void disableOrderType(final ViewDefinitionState view) {
        FieldComponent orderTypeField = (FieldComponent) view.getComponentByReference(OrderFields.ORDER_TYPE);

        orderTypeField.setEnabled(false);
        orderTypeField.requestComponentUpdateState();
    }

    private void fillMasterOrderFields(final ViewDefinitionState view, final Entity masterOrder, final Entity product) {
        FieldComponent numberField = (FieldComponent) view.getComponentByReference(OrderFields.NUMBER);
        LookupComponent companyLookup = (LookupComponent) view.getComponentByReference(OrderFields.COMPANY);
        FieldComponent deadlineField = (FieldComponent) view.getComponentByReference(OrderFields.DEADLINE);
        FieldComponent dateFromField = (FieldComponent) view.getComponentByReference(OrderFields.DATE_FROM);
        FieldComponent dateToField = (FieldComponent) view.getComponentByReference(OrderFields.DATE_TO);
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(OrderFields.PRODUCT);
        LookupComponent technologyPrototypeLookup = (LookupComponent) view
                .getComponentByReference(OrderFields.TECHNOLOGY_PROTOTYPE);
        FieldComponent plannedQuantityField = (FieldComponent) view.getComponentByReference(OrderFields.PLANNED_QUANTITY);

        if (masterOrder != null) {
            String masterOrderNumber = masterOrder.getStringField(MasterOrderFields.NUMBER);
            Entity masterOrderCompany = masterOrder.getBelongsToField(MasterOrderFields.COMPANY);
            Date masterOrderDeadline = masterOrder.getDateField(MasterOrderFields.DEADLINE);
            Date masterOrderStartDate = masterOrder.getDateField(MasterOrderFields.START_DATE);
            Date masterOrderFinishDate = masterOrder.getDateField(MasterOrderFields.FINISH_DATE);
            Entity masterOrderProduct = masterOrder.getBelongsToField(MasterOrderFields.PRODUCT);

            BigDecimal masterOrderQuantity = BigDecimalUtils.convertNullToZero(masterOrder.getDecimalField(MasterOrderFields.MASTER_ORDER_QUANTITY));

            BigDecimal cumulatedOrderQuantity = BigDecimalUtils.convertNullToZero(masterOrderOrdersDataProvider.sumBelongingOrdersPlannedQuantities(masterOrder,
                    masterOrderProduct));

            BigDecimal plannedQuantity = masterOrderQuantity.subtract(cumulatedOrderQuantity, numberService.getMathContext());

            if (product != null) {
                masterOrderProduct = product;
            }

            Entity masterOrderTechnology = masterOrder.getBelongsToField(MasterOrderFields.TECHNOLOGY);

            String number = (String) numberField.getFieldValue();

            String generatedNumber = numberGeneratorService.generateNumberWithPrefix(OrdersConstants.PLUGIN_IDENTIFIER,
                    OrdersConstants.MODEL_ORDER, 3, masterOrderNumber + "-");

            // if (StringUtils.isEmpty(number) || generatedNumber.equals(number)) {
            numberField.setFieldValue(generatedNumber);
            // }

            if ((companyLookup.getEntity() == null) && (masterOrderCompany != null)) {
                companyLookup.setFieldValue(masterOrderCompany.getId());
            }

            if (StringUtils.isEmpty((String) deadlineField.getFieldValue()) && (masterOrderDeadline != null)) {
                deadlineField.setFieldValue(DateUtils.toDateTimeString(masterOrderDeadline));
            }

            if (StringUtils.isEmpty((String) dateFromField.getFieldValue()) && (masterOrderStartDate != null)) {
                dateFromField.setFieldValue(DateUtils.toDateTimeString(masterOrderStartDate));
            }

            if (StringUtils.isEmpty((String) dateToField.getFieldValue()) && (masterOrderFinishDate != null)) {
                dateToField.setFieldValue(DateUtils.toDateTimeString(masterOrderFinishDate));
            }

            if ((productLookup.getEntity() == null) && (masterOrderProduct != null)) {
                productLookup.setFieldValue(masterOrderProduct.getId());
            }

            if ((technologyPrototypeLookup.getEntity() == null) && (masterOrderTechnology != null)) {
                technologyPrototypeLookup.setFieldValue(masterOrderTechnology.getId());
            }

            if (StringUtils.isEmpty((String) plannedQuantityField.getFieldValue()) && (plannedQuantity != null)
                    && (BigDecimal.ZERO.compareTo(plannedQuantity) < 0)) {
                plannedQuantityField.setFieldValue(numberService.format(plannedQuantity));
            }

            numberField.requestComponentUpdateState();
            companyLookup.requestComponentUpdateState();
            deadlineField.requestComponentUpdateState();
            dateFromField.requestComponentUpdateState();
            dateToField.requestComponentUpdateState();
            productLookup.requestComponentUpdateState();
            technologyPrototypeLookup.requestComponentUpdateState();
            plannedQuantityField.requestComponentUpdateState();

            productLookup.performEvent(view, "onSelectedEntityChange", "");
        }
    }

    private Entity getMasterOrder(final Long masterOrderId) {
        return dataDefinitionService.get(MasterOrdersConstants.PLUGIN_IDENTIFIER, MasterOrdersConstants.MODEL_MASTER_ORDER).get(
                masterOrderId);
    }

}
