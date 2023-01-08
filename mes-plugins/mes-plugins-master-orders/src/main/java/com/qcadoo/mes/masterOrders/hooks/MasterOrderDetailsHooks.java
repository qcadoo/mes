/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 * <p>
 * This file is part of Qcadoo.
 * <p>
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.masterOrders.hooks;

import static com.qcadoo.mes.masterOrders.constants.MasterOrderFields.ADD_MASTER_PREFIX_TO_NUMBER;
import static com.qcadoo.mes.masterOrders.constants.MasterOrderFields.NUMBER;

import java.util.Collections;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.CompanyFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderState;
import com.qcadoo.mes.masterOrders.criteriaModifier.OrderCriteriaModifier;
import com.qcadoo.model.api.Entity;
import com.qcadoo.plugin.api.PluginUtils;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class MasterOrderDetailsHooks {

    private static final String L_ORDERS_LOOKUP = "ordersLookup";

    private static final String L_ORDERS = "orders";

    private static final String L_CREATE_ORDER = "createOrder";

    private static final String L_DOCUMENTS = "documents";

    private static final String L_GENERATE_ORDERS = "generateOrders";

    private static final String L_CREATE_RELEASE_DOCUMENT = "createReleaseDocument";

    private static final String L_SIZE_ACTIONS = "sizeActions";

    private static final String L_ADD_PRODUCTS_BY_ATTRIBUTE = "addProductsByAttribute";

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private OrderCriteriaModifier orderCriteriaModifier;

    public void onBeforeRender(final ViewDefinitionState view) {
        initState(view);
        setOrderLookupCriteriaModifier(view);
        setDefaultMasterOrderNumber(view);
        disableFields(view);
        ribbonRender(view);
    }

    public void initState(final ViewDefinitionState view) {
        FormComponent masterOrderForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        FieldComponent stateField = (FieldComponent) view.getComponentByReference(MasterOrderFields.STATE);

        stateField.setEnabled(false);

        if (Objects.nonNull(masterOrderForm.getEntityId())) {
            return;
        }

        stateField.setFieldValue(MasterOrderState.NEW.getStringValue());
    }

    private void setOrderLookupCriteriaModifier(final ViewDefinitionState view) {
        FormComponent masterOrderForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity masterOrder = masterOrderForm.getEntity();
        LookupComponent orderLookup = (LookupComponent) view.getComponentByReference(L_ORDERS_LOOKUP);

        if (masterOrder.getBooleanField(ADD_MASTER_PREFIX_TO_NUMBER)) {
            orderCriteriaModifier.putMasterOrderNumberFilter(orderLookup, masterOrder.getStringField(NUMBER));
        } else {
            orderCriteriaModifier.clearMasterOrderNumberFilter(orderLookup);
        }
    }

    private void setDefaultMasterOrderNumber(final ViewDefinitionState view) {
        if (checkIfShouldInsertNumber(view)) {
            FieldComponent numberField = (FieldComponent) view.getComponentByReference(MasterOrderFields.NUMBER);

            numberField.setFieldValue(
                    jdbcTemplate.queryForObject("select generate_master_order_number()", Collections.emptyMap(), String.class));

            numberField.requestComponentUpdateState();
        }
    }

    private void disableFields(final ViewDefinitionState view) {
        FormComponent masterOrderForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        LookupComponent addressLookup = (LookupComponent) view.getComponentByReference(MasterOrderFields.ADDRESS);
        FieldComponent companyCategoryField = (FieldComponent) view.getComponentByReference(MasterOrderFields.COMPANY_CATEGORY);

        Entity masterOrder = masterOrderForm.getEntity();
        Entity company = masterOrder.getBelongsToField(MasterOrderFields.COMPANY);

        if (Objects.isNull(company)) {
            addressLookup.setFieldValue(null);
            addressLookup.setEnabled(false);
            companyCategoryField.setFieldValue(null);
        } else {
            addressLookup.setEnabled(true);
            companyCategoryField.setFieldValue(company.getStringField(CompanyFields.CONTRACTOR_CATEGORY));
        }
    }

    public void ribbonRender(final ViewDefinitionState view) {
        FormComponent masterOrderForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        GridComponent masterOrderProductsGrid = (GridComponent) view
                .getComponentByReference(MasterOrderFields.MASTER_ORDER_PRODUCTS);

        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
        RibbonGroup ordersRibbonGroup = window.getRibbon().getGroupByName(L_ORDERS);
        RibbonActionItem createOrderRibbonActionItem = ordersRibbonGroup.getItemByName(L_CREATE_ORDER);

        RibbonGroup documentsRibbonGroup = window.getRibbon().getGroupByName(L_DOCUMENTS);
        RibbonActionItem createReleaseDocumentRibbonActionItem = documentsRibbonGroup.getItemByName(L_CREATE_RELEASE_DOCUMENT);
        createReleaseDocumentRibbonActionItem.setMessage("masterOrders.ribbon.documents.createReleaseDocument.message");

        RibbonGroup sizeActionsGroup = window.getRibbon().getGroupByName(L_SIZE_ACTIONS);
        RibbonActionItem addProductsByAttributeRibbonActionItem = sizeActionsGroup.getItemByName(L_ADD_PRODUCTS_BY_ATTRIBUTE);

        Entity masterOrder = masterOrderForm.getEntity();

        String state = masterOrder.getStringField(MasterOrderFields.STATE);

        boolean isSaved = Objects.nonNull(masterOrder.getId());
        boolean isStateNotCompletedAndDeclined = !MasterOrderState.COMPLETED.getStringValue().equals(state) && !MasterOrderState.DECLINED.getStringValue().equals(state);

        if (masterOrderProductsGrid.getSelectedEntities().isEmpty()) {
            createOrderRibbonActionItem.setEnabled(false);
            createReleaseDocumentRibbonActionItem.setEnabled(false);
        } else {
            createOrderRibbonActionItem.setEnabled(masterOrderProductsGrid.getSelectedEntities().size() == 1);
            createReleaseDocumentRibbonActionItem.setEnabled(true);
        }

        if (PluginUtils.isEnabled("goodFood") && !masterOrderProductsGrid.getEntities().isEmpty()) {
            createOrderRibbonActionItem.setEnabled(true);
        } else {
            createOrderRibbonActionItem.setMessage("masterOrders.order.ribbon.message.selectOneProduct");
        }

        createOrderRibbonActionItem.requestUpdate(true);
        createReleaseDocumentRibbonActionItem.requestUpdate(true);
        toggleGenerateOrdersButton(view);
        addProductsByAttributeRibbonActionItem.setEnabled(isSaved && isStateNotCompletedAndDeclined);
        addProductsByAttributeRibbonActionItem.requestUpdate(true);
        window.requestRibbonRender();
    }

    private void toggleGenerateOrdersButton(final ViewDefinitionState view) {
        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
        RibbonGroup ordersRibbonGroup = window.getRibbon().getGroupByName(L_ORDERS);
        RibbonActionItem generateOrdersRibbonActionItem = ordersRibbonGroup.getItemByName(L_GENERATE_ORDERS);

        generateOrdersRibbonActionItem.setMessage("qcadooView.ribbon.orders.generateOrders.message");

        GridComponent masterOrderProductsGrid = (GridComponent) view
                .getComponentByReference(MasterOrderFields.MASTER_ORDER_PRODUCTS);

        generateOrdersRibbonActionItem.setEnabled(!masterOrderProductsGrid.getSelectedEntities().isEmpty());

        generateOrdersRibbonActionItem.requestUpdate(true);
    }

    private boolean checkIfShouldInsertNumber(final ViewDefinitionState state) {
        FormComponent masterOrderForm = (FormComponent) state.getComponentByReference(QcadooViewConstants.L_FORM);
        FieldComponent numberField = (FieldComponent) state.getComponentByReference(MasterOrderFields.NUMBER);

        if (Objects.nonNull(masterOrderForm.getEntityId())) {
            // form is already saved
            return false;
        }
        if (StringUtils.isNotBlank((String) numberField.getFieldValue())) {
            // number is already chosen
            return false;
        }
        // there is a validation message for that field
        return !numberField.isHasError();
    }

}
