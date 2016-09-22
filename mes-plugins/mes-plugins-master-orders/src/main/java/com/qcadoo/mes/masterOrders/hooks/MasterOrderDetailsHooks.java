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

import static com.qcadoo.mes.basic.constants.ProductFields.UNIT;
import static com.qcadoo.mes.masterOrders.constants.MasterOrderFields.ADD_MASTER_PREFIX_TO_NUMBER;
import static com.qcadoo.mes.masterOrders.constants.MasterOrderFields.COOMENTS;
import static com.qcadoo.mes.masterOrders.constants.MasterOrderFields.CUMULATED_ORDER_QUANTITY;
import static com.qcadoo.mes.masterOrders.constants.MasterOrderFields.DEFAULT_TECHNOLOGY;
import static com.qcadoo.mes.masterOrders.constants.MasterOrderFields.MASTER_ORDER_POSITION_STATUS;
import static com.qcadoo.mes.masterOrders.constants.MasterOrderFields.MASTER_ORDER_QUANTITY;
import static com.qcadoo.mes.masterOrders.constants.MasterOrderFields.MASTER_ORDER_TYPE;
import static com.qcadoo.mes.masterOrders.constants.MasterOrderFields.NUMBER;
import static com.qcadoo.mes.masterOrders.constants.MasterOrderFields.PRODUCT;
import static com.qcadoo.mes.masterOrders.constants.MasterOrderFields.TECHNOLOGY;

import java.math.BigDecimal;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.masterOrders.constants.MasterOrderFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderType;
import com.qcadoo.mes.masterOrders.constants.MasterOrdersConstants;
import com.qcadoo.mes.masterOrders.criteriaModifier.OrderCriteriaModifier;
import com.qcadoo.mes.masterOrders.util.MasterOrderOrdersDataProvider;
import com.qcadoo.mes.orders.TechnologyServiceO;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.ExpressionService;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class MasterOrderDetailsHooks {

    private static final String L_FORM = "form";

    private static final String L_WINDOW = "window";

    private static final String L_ORDERS = "orders";

    private static final String L_CREATE_ORDER = "createOrder";

    private static final String L_ORDERS_LOOKUP = "ordersLookup";

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private ExpressionService expressionService;

    @Autowired
    private TechnologyServiceO technologyServiceO;

    @Autowired
    private OrderCriteriaModifier orderCriteriaModifier;

    @Autowired
    private MasterOrderOrdersDataProvider masterOrderOrdersDataProvider;

    @Autowired
    private NumberService numberService;

    public void hideFieldDependOnMasterOrderType(final ViewDefinitionState view) {
        FieldComponent masterOrderType = (FieldComponent) view.getComponentByReference(MasterOrderFields.MASTER_ORDER_TYPE);
        Object masterOrderTypeValue = masterOrderType.getFieldValue();

        if (masterOrderTypeValue == null || StringUtils.isEmpty(masterOrderTypeValue.toString())
                || masterOrderTypeValue.equals(MasterOrderType.UNDEFINED.getStringValue())) {
            setFieldsVisibility(view, false, false);
        } else if (masterOrderTypeValue.equals(MasterOrderType.ONE_PRODUCT.getStringValue())) {
            setFieldsVisibility(view, true, false);
        } else {
            setFieldsVisibility(view, false, true);
        }
    }

    public void setFieldsVisibility(final ViewDefinitionState view, final boolean visibleFields, final boolean visibleGrid) {
        for (String reference : Arrays.asList(TECHNOLOGY, PRODUCT, DEFAULT_TECHNOLOGY, MASTER_ORDER_QUANTITY,
                CUMULATED_ORDER_QUANTITY, "producedOrderQuantity", COOMENTS, MASTER_ORDER_POSITION_STATUS)) {
            FieldComponent field = (FieldComponent) view.getComponentByReference(reference);
            field.setVisible(visibleFields);
        }

        GridComponent masterOrderProductsGrid = (GridComponent) view
                .getComponentByReference(MasterOrderFields.MASTER_ORDER_PRODUCTS);
        masterOrderProductsGrid.setVisible(visibleGrid);

        WindowComponent window = (WindowComponent) view.getComponentByReference(L_WINDOW);
        RibbonGroup orders = (RibbonGroup) window.getRibbon().getGroupByName(L_ORDERS);
        RibbonActionItem createOrder = (RibbonActionItem) orders.getItemByName(L_CREATE_ORDER);

        if (visibleGrid) {
            if (masterOrderProductsGrid.getSelectedEntities().isEmpty()) {
                createOrder.setEnabled(false);
            } else if (masterOrderProductsGrid.getSelectedEntities().size() == 1) {
                createOrder.setEnabled(true);
            } else {
                createOrder.setEnabled(false);
            }
            createOrder.setMessage("masterOrders.order.ribbon.message.selectOneProduct");
            createOrder.requestUpdate(true);
            window.requestRibbonRender();
        } else {
            createOrder.setEnabled(true);
            createOrder.requestUpdate(true);

        }

        ComponentState borderLayoutProductQuantity = view.getComponentByReference("borderLayoutProductQuantity");
        borderLayoutProductQuantity.setVisible(visibleFields);
    }

    public void fillUnitField(final ViewDefinitionState view) {
        LookupComponent productField = (LookupComponent) view.getComponentByReference(PRODUCT);
        Entity product = productField.getEntity();
        String unit = null;

        if (product != null) {
            unit = product.getStringField(UNIT);
        }

        for (String reference : Arrays.asList("cumulatedOrderQuantityUnit", "masterOrderQuantityUnit",
                "producedOrderQuantityUnit", "leftToReleaseUnit")) {
            FieldComponent field = (FieldComponent) view.getComponentByReference(reference);
            field.setFieldValue(unit);

            if (unit != null) {
                field.setVisible(true);
            }

            field.requestComponentUpdateState();
        }
    }

    public void fillDefaultTechnology(final ViewDefinitionState view) {
        LookupComponent productField = (LookupComponent) view.getComponentByReference("product");
        FieldComponent defaultTechnology = (FieldComponent) view.getComponentByReference("defaultTechnology");

        Entity product = productField.getEntity();

        if (product == null || technologyServiceO.getDefaultTechnology(product) == null) {
            defaultTechnology.setFieldValue(null);
            defaultTechnology.requestComponentUpdateState();
            return;
        }

        Entity defaultTechnologyEntity = technologyServiceO.getDefaultTechnology(product);
        String defaultTechnologyValue = expressionService.getValue(defaultTechnologyEntity, "#number + ' - ' + #name",
                view.getLocale());

        defaultTechnology.setFieldValue(defaultTechnologyValue);
        defaultTechnology.requestComponentUpdateState();
    }

    public void showErrorWhenCumulatedQuantity(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        Entity masterOrder = form.getEntity();

        if (masterOrder == null) {
            return;
        }

        if (!masterOrder.getStringField(MasterOrderFields.MASTER_ORDER_TYPE)
                .equals(MasterOrderType.ONE_PRODUCT.getStringValue())) {
            return;
        }

        BigDecimal cumulatedQuantity = masterOrder.getDecimalField(CUMULATED_ORDER_QUANTITY);
        BigDecimal masterQuantity = masterOrder.getDecimalField(MASTER_ORDER_QUANTITY);

        if (cumulatedQuantity != null && masterQuantity != null && cumulatedQuantity.compareTo(masterQuantity) == -1) {
            form.addMessage("masterOrders.masterOrder.masterOrderCumulatedQuantityField.wrongQuantity", MessageType.INFO, false);
        }
    }

    public void disabledGridWhenMasterOrderDoesnotHaveProduct(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        Entity masterOrder = form.getEntity();

        if (masterOrder == null) {
            return;
        }

        GridComponent ordersGrid = (GridComponent) view.getComponentByReference(MasterOrderFields.ORDERS);
        GridComponent masterOrderProductsGrid = (GridComponent) view
                .getComponentByReference(MasterOrderFields.MASTER_ORDER_PRODUCTS);

        if (masterOrder.getStringField(MasterOrderFields.MASTER_ORDER_TYPE).equals(MasterOrderType.MANY_PRODUCTS.getStringValue())
                && masterOrderProductsGrid.getEntities().isEmpty()) {
            ordersGrid.setEditable(false);
        } else {
            ordersGrid.setEditable(true);
        }
    }

    public void setUneditableWhenEntityHasUnsaveChanges(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        Long masterOrderId = form.getEntityId();

        if (masterOrderId == null) {
            return;
        }

        FieldComponent masterOrderTypeField = (FieldComponent) view.getComponentByReference(MASTER_ORDER_TYPE);
        Entity masterOrder = form.getEntity().getDataDefinition().get(masterOrderId);
        String masterOrderType = masterOrderTypeField.getFieldValue().toString();
        GridComponent productsGrid = (GridComponent) view.getComponentByReference(MasterOrderFields.MASTER_ORDER_PRODUCTS);

        if (!masterOrder.getStringField(MASTER_ORDER_TYPE).equals(masterOrderType)) {
            productsGrid.setEditable(false);
        } else {
            productsGrid.setEditable(true);
        }
    }

    public void setOrderLookupCriteriaModifier(final ViewDefinitionState view) {
        FormComponent masterOrderForm = (FormComponent) view.getComponentByReference(L_FORM);

        Entity masterOrder = masterOrderForm.getEntity();
        LookupComponent orderLookup = (LookupComponent) view.getComponentByReference(L_ORDERS_LOOKUP);
        if (masterOrder.getBooleanField(ADD_MASTER_PREFIX_TO_NUMBER)) {
            orderCriteriaModifier.putMasterOrderNumberFilter(orderLookup, masterOrder.getStringField(NUMBER));
        } else {
            orderCriteriaModifier.clearMasterOrderNumberFilter(orderLookup);
        }
    }

    public void setProductLookupRequired(final ViewDefinitionState view) {
        FieldComponent productField = (FieldComponent) view.getComponentByReference(PRODUCT);
        productField.setRequired(true);
    }

    public void setDefaultMasterOrderNumber(final ViewDefinitionState view) {
        if (checkIfShouldInsertNumber(view)) {
            FieldComponent numberField = (FieldComponent) view.getComponentByReference(MasterOrderFields.NUMBER);
            numberField.setFieldValue(numberGeneratorService.generateNumber(MasterOrdersConstants.PLUGIN_IDENTIFIER,
                    MasterOrdersConstants.MODEL_MASTER_ORDER));
            numberField.requestComponentUpdateState();
        }
    }

    public boolean checkIfShouldInsertNumber(final ViewDefinitionState state) {
        FormComponent form = (FormComponent) state.getComponentByReference(L_FORM);
        FieldComponent number = (FieldComponent) state.getComponentByReference(MasterOrderFields.NUMBER);
        if (form.getEntityId() != null) {
            // form is already saved
            return false;
        }
        if (StringUtils.isNotBlank((String) number.getFieldValue())) {
            // number is already chosen
            return false;
        }
        if (number.isHasError()) {
            // there is a validation message for that field
            return false;
        }
        return true;
    }

    public void calculateMasterOrderFields(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        Long masterOrderId = form.getEntityId();

        if (masterOrderId == null) {
            return;
        }
        Entity masterOrder = form.getEntity();

        calculateCumulativeQuantityFromOrders(view, masterOrder);
        fillRegisteredQuantity(view, masterOrder);

    }

    private void fillRegisteredQuantity(final ViewDefinitionState view, final Entity masterOrder) {
        if (masterOrder.getId() == null || MasterOrderType.of(masterOrder) != MasterOrderType.ONE_PRODUCT) {
            return;
        }
        Entity product = masterOrder.getBelongsToField(MasterOrderFields.PRODUCT);

        FieldComponent producedOrderQuantityField = (FieldComponent) view
                .getComponentByReference(MasterOrderFields.PRODUCED_ORDER_QUQNTITY);
        FieldComponent leftToReleaseField = (FieldComponent) view.getComponentByReference(MasterOrderFields.LEFT_TO_RELASE);

        BigDecimal doneQuantity = masterOrderOrdersDataProvider.sumBelongingOrdersDoneQuantities(masterOrder, product);

        producedOrderQuantityField.setFieldValue(numberService.formatWithMinimumFractionDigits(doneQuantity, 0));

        producedOrderQuantityField.requestComponentUpdateState();

        BigDecimal value = BigDecimalUtils.convertNullToZero(masterOrder.getDecimalField("masterOrderQuantity"))
                .subtract(BigDecimalUtils.convertNullToZero(doneQuantity), numberService.getMathContext());
        if (BigDecimal.ZERO.compareTo(value) == 1) {
            value = BigDecimal.ZERO;
        }
        leftToReleaseField.setFieldValue(numberService.formatWithMinimumFractionDigits(value, 0));

        leftToReleaseField.requestComponentUpdateState();
    }

    private void calculateCumulativeQuantityFromOrders(final ViewDefinitionState view, final Entity masterOrder) {
        if (masterOrder.getId() == null || MasterOrderType.of(masterOrder) != MasterOrderType.ONE_PRODUCT) {
            return;
        }
        Entity product = masterOrder.getBelongsToField(MasterOrderFields.PRODUCT);

        FieldComponent cumulatedOrderQuantityField = (FieldComponent) view
                .getComponentByReference(MasterOrderFields.CUMULATED_ORDER_QUANTITY);

        BigDecimal quantitiesSum = masterOrderOrdersDataProvider.sumBelongingOrdersPlannedQuantities(masterOrder, product);

        cumulatedOrderQuantityField.setFieldValue(numberService.formatWithMinimumFractionDigits(quantitiesSum, 0));

        cumulatedOrderQuantityField.requestComponentUpdateState();
    }

    public void disableFields(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        Entity masterOrder = form.getEntity();
        Entity company = masterOrder.getBelongsToField(MasterOrderFields.COMPANY);
        LookupComponent addressLookup = (LookupComponent) view.getComponentByReference(MasterOrderFields.ADDRESS);
        if (company == null) {
            addressLookup.setFieldValue(null);
            addressLookup.setEnabled(false);
        } else {
            addressLookup.setEnabled(true);
        }
    }

}
