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
package com.qcadoo.mes.masterOrders.hooks;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.masterOrders.constants.*;
import com.qcadoo.mes.masterOrders.util.MasterOrderOrdersDataProvider;
import com.qcadoo.mes.orders.OrderService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.constants.ParameterFieldsO;
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
    private ParameterService parameterService;

    @Autowired
    private MasterOrderOrdersDataProvider masterOrderOrdersDataProvider;

    @Autowired
    private OrderService orderService;

    public void fillMasterOrderFields(final ViewDefinitionState view) {
        FormComponent orderForm = (FormComponent) view.getComponentByReference(L_FORM);

        Entity order = orderForm.getEntity();

        Entity masterOrder = order.getBelongsToField(OrderFieldsMO.MASTER_ORDER);
        Entity masterOrderProduct = order.getBelongsToField(OrderFieldsMO.MASTER_ORDER_PRODUCT);

        Entity masterOrderProductComponent = order.getBelongsToField(OrderFieldsMO.MASTER_ORDER_PRODUCT_COMPONENT);

        if (order.getId() == null) {

            if (masterOrder != null) {
                Long masterOrderId = masterOrder.getId();

                masterOrder = getMasterOrder(masterOrderId);

                fillMasterOrderFields(view, masterOrder, masterOrderProduct, masterOrderProductComponent);
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

    private void fillMasterOrderFields(final ViewDefinitionState view, final Entity masterOrder, final Entity product,
            Entity productComponent) {
        FieldComponent numberField = (FieldComponent) view.getComponentByReference(OrderFields.NUMBER);
        LookupComponent companyLookup = (LookupComponent) view.getComponentByReference(OrderFields.COMPANY);
        FieldComponent deadlineField = (FieldComponent) view.getComponentByReference(OrderFields.DEADLINE);
        FieldComponent dateFromField = (FieldComponent) view.getComponentByReference(OrderFields.DATE_FROM);
        FieldComponent dateToField = (FieldComponent) view.getComponentByReference(OrderFields.DATE_TO);
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(OrderFields.PRODUCT);
        LookupComponent technologyPrototypeLookup = (LookupComponent) view
                .getComponentByReference(OrderFields.TECHNOLOGY_PROTOTYPE);
        FieldComponent plannedQuantityField = (FieldComponent) view.getComponentByReference(OrderFields.PLANNED_QUANTITY);
        FieldComponent descriptionField = (FieldComponent) view.getComponentByReference(OrderFields.DESCRIPTION);
        boolean fillOrderDescriptionBasedOnTechnology = dataDefinitionService
                .get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PARAMETER).find().setMaxResults(1).uniqueResult()
                .getBooleanField(ParameterFieldsO.FILL_ORDER_DESCRIPTION_BASED_ON_TECHNOLOGY_DESCRIPTION);

        LookupComponent addressLookup = (LookupComponent) view.getComponentByReference(OrderFields.ADDRESS);
        if (masterOrder != null) {
            Entity parameter = parameterService.getParameter();

            String masterOrderNumber = masterOrder.getStringField(MasterOrderFields.NUMBER);
            Entity masterOrderCompany = masterOrder.getBelongsToField(MasterOrderFields.COMPANY);
            Date masterOrderDeadline = masterOrder.getDateField(MasterOrderFields.DEADLINE);
            Date masterOrderStartDate = masterOrder.getDateField(MasterOrderFields.START_DATE);
            Date masterOrderFinishDate = masterOrder.getDateField(MasterOrderFields.FINISH_DATE);
            Entity masterOrderAddress = masterOrder.getBelongsToField(MasterOrderFields.ADDRESS);

            BigDecimal masterOrderQuantity = BigDecimalUtils.convertNullToZero(productComponent
                    .getDecimalField(MasterOrderProductFields.MASTER_ORDER_QUANTITY));

            BigDecimal cumulatedOrderQuantity = BigDecimalUtils.convertNullToZero(productComponent
                    .getDecimalField(MasterOrderProductFields.CUMULATED_ORDER_QUANTITY));

            BigDecimal plannedQuantity = masterOrderQuantity.subtract(cumulatedOrderQuantity, numberService.getMathContext());

            String generatedNumber;

            if (parameter.getBooleanField(ParameterFieldsMO.SAME_ORDER_NUMBER)) {
                generatedNumber = masterOrderNumber;
            } else {
                generatedNumber = numberGeneratorService.generateNumberWithPrefix(OrdersConstants.PLUGIN_IDENTIFIER,
                        OrdersConstants.MODEL_ORDER, 3, masterOrderNumber + "-");
            }

            numberField.setFieldValue(generatedNumber);
            numberField.requestComponentUpdateState();

            if ((companyLookup.getEntity() == null) && (masterOrderCompany != null)) {
                companyLookup.setFieldValue(masterOrderCompany.getId());
                companyLookup.requestComponentUpdateState();
            }

            if ((addressLookup.getEntity() == null) && (masterOrderAddress != null)) {
                addressLookup.setFieldValue(masterOrderAddress.getId());
                addressLookup.requestComponentUpdateState();
            }

            if (StringUtils.isEmpty((String) deadlineField.getFieldValue()) && (masterOrderDeadline != null)) {
                deadlineField.setFieldValue(DateUtils.toDateTimeString(masterOrderDeadline));
                deadlineField.requestComponentUpdateState();
            }

            if (StringUtils.isEmpty((String) dateFromField.getFieldValue()) && (masterOrderStartDate != null)) {
                dateFromField.setFieldValue(DateUtils.toDateTimeString(masterOrderStartDate));
                dateFromField.requestComponentUpdateState();
            }

            if (StringUtils.isEmpty((String) dateToField.getFieldValue()) && (masterOrderFinishDate != null)) {
                dateToField.setFieldValue(DateUtils.toDateTimeString(masterOrderFinishDate));
                dateToField.requestComponentUpdateState();
            }

            if (StringUtils.isEmpty((String) plannedQuantityField.getFieldValue()) && (plannedQuantity != null)
                    && (BigDecimal.ZERO.compareTo(plannedQuantity) < 0)) {
                plannedQuantityField.setFieldValue(numberService.format(plannedQuantity));
                plannedQuantityField.requestComponentUpdateState();
            }

            if ((productLookup.getEntity() == null) && (product != null)) {
                productLookup.setFieldValue(product.getId());
                productLookup.requestComponentUpdateState();
                productLookup.performEvent(view, "onSelectedEntityChange", "");
            }
            Entity productComponentDB = productComponent.getDataDefinition().get(productComponent.getId());
            Entity masterOrderTechnology = productComponentDB.getBelongsToField(MasterOrderProductFields.TECHNOLOGY);
            if (view.isViewAfterRedirect() && masterOrderTechnology != null) {
                technologyPrototypeLookup.setFieldValue(masterOrderTechnology.getId());
                technologyPrototypeLookup.requestComponentUpdateState();
                technologyPrototypeLookup.performEvent(view, "onSelectedEntityChange", "");
            }

            String orderDescription = orderService.buildOrderDescription(masterOrder, masterOrderTechnology,
                    fillOrderDescriptionBasedOnTechnology);

            if (((String) descriptionField.getFieldValue()).isEmpty()) {
                descriptionField.setFieldValue("");
                descriptionField.requestComponentUpdateState();
                descriptionField.setFieldValue(orderDescription);
                descriptionField.requestComponentUpdateState();
            }
        }
    }

    private Entity getMasterOrder(final Long masterOrderId) {
        return dataDefinitionService.get(MasterOrdersConstants.PLUGIN_IDENTIFIER, MasterOrdersConstants.MODEL_MASTER_ORDER).get(
                masterOrderId);
    }

}