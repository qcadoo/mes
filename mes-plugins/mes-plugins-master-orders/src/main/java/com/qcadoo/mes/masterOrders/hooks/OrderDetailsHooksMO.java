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

import com.google.common.base.Strings;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.masterOrders.OrdersFromMOProductsGenerationService;
import com.qcadoo.mes.masterOrders.constants.*;
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
import com.qcadoo.view.constants.QcadooViewConstants;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;

import static com.qcadoo.mes.orders.constants.ParameterFieldsO.DEADLINE_FOR_ORDER_BASED_ON_DELIVERY_DATE;
import static com.qcadoo.mes.orders.constants.ParameterFieldsO.DEADLINE_FOR_ORDER_EARLIER_THAN_DELIVERY_DATE;

@Service
public class OrderDetailsHooksMO {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private OrdersFromMOProductsGenerationService ordersFromMOProductsGenerationService;

    public void fillMasterOrderFields(final ViewDefinitionState view) {
        FormComponent orderForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

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
    }

    private void fillMasterOrderFields(final ViewDefinitionState view, final Entity masterOrder, final Entity product,
                                       Entity productComponent) {
        FieldComponent numberField = (FieldComponent) view.getComponentByReference(OrderFields.NUMBER);
        LookupComponent companyLookup = (LookupComponent) view.getComponentByReference(OrderFields.COMPANY);
        FieldComponent deadlineField = (FieldComponent) view.getComponentByReference(OrderFields.DEADLINE);
        FieldComponent dateFromField = (FieldComponent) view.getComponentByReference(OrderFields.DATE_FROM);
        FieldComponent dateToField = (FieldComponent) view.getComponentByReference(OrderFields.DATE_TO);
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(OrderFields.PRODUCT);
        LookupComponent technologyLookup = (LookupComponent) view
                .getComponentByReference(OrderFields.TECHNOLOGY);
        FieldComponent plannedQuantityField = (FieldComponent) view.getComponentByReference(OrderFields.PLANNED_QUANTITY);
        FieldComponent descriptionField = (FieldComponent) view.getComponentByReference(OrderFields.DESCRIPTION);
        FieldComponent vendorInfoField = (FieldComponent) view.getComponentByReference(OrderFields.VENDOR_INFO);

        LookupComponent addressLookup = (LookupComponent) view.getComponentByReference(OrderFields.ADDRESS);
        if (masterOrder != null && productComponent != null && productComponent.getId() != null) {
            Entity parameter = parameterService.getParameter();

            String masterOrderNumber = masterOrder.getStringField(MasterOrderFields.NUMBER);
            Entity masterOrderCompany = masterOrder.getBelongsToField(MasterOrderFields.COMPANY);
            Date masterOrderDeadline = masterOrder.getDateField(MasterOrderFields.DEADLINE);
            boolean deadlineForOrderBasedOnDeliveryDate = parameter.getBooleanField(DEADLINE_FOR_ORDER_BASED_ON_DELIVERY_DATE);
            if (deadlineForOrderBasedOnDeliveryDate) {
                masterOrderDeadline = productComponent.getDateField(MasterOrderProductFields.DELIVERY_DATE);
                Integer deadlineForOrderEarlierThanDeliveryDate = parameter.getIntegerField(DEADLINE_FOR_ORDER_EARLIER_THAN_DELIVERY_DATE);
                if (masterOrderDeadline != null && deadlineForOrderEarlierThanDeliveryDate != null && deadlineForOrderEarlierThanDeliveryDate > 0) {
                    masterOrderDeadline = new DateTime(masterOrderDeadline).minusDays(deadlineForOrderEarlierThanDeliveryDate).toDate();
                }
            }
            Date masterOrderStartDate = masterOrder.getDateField(MasterOrderFields.START_DATE);
            Date masterOrderFinishDate = masterOrder.getDateField(MasterOrderFields.FINISH_DATE);
            Entity masterOrderAddress = masterOrder.getBelongsToField(MasterOrderFields.ADDRESS);
            String vendorInfo = productComponent.getStringField(MasterOrderProductFields.VENDOR_INFO);
            Entity masterOrderProductDto = dataDefinitionService
                    .get(MasterOrdersConstants.PLUGIN_IDENTIFIER, MasterOrdersConstants.MODEL_MASTER_ORDER_POSITION_DTO)
                    .get(productComponent.getId());

            BigDecimal plannedQuantity = BigDecimalUtils.convertNullToZero(
                    masterOrderProductDto.getDecimalField(MasterOrderPositionDtoFields.QUANTITY_REMAINING_TO_ORDER));
            String generatedNumber;

            if (parameter.getBooleanField(ParameterFieldsMO.SAME_ORDER_NUMBER)) {
                generatedNumber = masterOrderNumber;
            } else {
                generatedNumber = numberGeneratorService.generateNumberWithPrefix(OrdersConstants.PLUGIN_IDENTIFIER,
                        OrdersConstants.MODEL_ORDER, 3, masterOrderNumber + "-");
            }

            numberField.setFieldValue(generatedNumber);
            numberField.requestComponentUpdateState();

            if (vendorInfo != null && StringUtils.isEmpty((String) vendorInfoField.getFieldValue())) {
                vendorInfoField.setFieldValue(vendorInfo);
                vendorInfoField.requestComponentUpdateState();
            }

            if ((companyLookup.getEntity() == null) && (masterOrderCompany != null)) {
                companyLookup.setFieldValue(masterOrderCompany.getId());
                companyLookup.requestComponentUpdateState();
            }

            if ((addressLookup.getEntity() == null) && (masterOrderAddress != null)) {
                addressLookup.setFieldValue(masterOrderAddress.getId());
                addressLookup.requestComponentUpdateState();
            }

            if (StringUtils.isEmpty((String) deadlineField.getFieldValue()) && masterOrderDeadline != null) {
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

            if (StringUtils.isEmpty((String) plannedQuantityField.getFieldValue())
                    && BigDecimal.ZERO.compareTo(plannedQuantity) < 0) {
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
                technologyLookup.setFieldValue(masterOrderTechnology.getId());
                technologyLookup.requestComponentUpdateState();
                technologyLookup.performEvent(view, "onSelectedEntityChange", "");
            }
            String orderDescription = ordersFromMOProductsGenerationService.buildDescription(parameter, masterOrder,
                    productComponentDB, masterOrderTechnology, product);

            if ((Strings.nullToEmpty((String) descriptionField.getFieldValue())).isEmpty()) {
                descriptionField.setFieldValue("");
                descriptionField.requestComponentUpdateState();
                descriptionField.setFieldValue(orderDescription);
                descriptionField.requestComponentUpdateState();
            }
        }
    }

    private Entity getMasterOrder(final Long masterOrderId) {
        return dataDefinitionService.get(MasterOrdersConstants.PLUGIN_IDENTIFIER, MasterOrdersConstants.MODEL_MASTER_ORDER)
                .get(masterOrderId);
    }

}
