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

import com.qcadoo.mes.masterOrders.constants.MasterOrderFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderState;
import com.qcadoo.mes.masterOrders.criteriaModifier.OrderCriteriaModifier;
import com.qcadoo.mes.masterOrders.util.MasterOrderOrdersDataProvider;
import com.qcadoo.mes.orders.TechnologyServiceO;
import com.qcadoo.mes.orders.criteriaModifiers.TechnologyCriteriaModifiersO;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.ExpressionService;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.plugin.api.PluginUtils;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.constants.QcadooViewConstants;

import java.util.Collections;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import static com.qcadoo.mes.masterOrders.constants.MasterOrderFields.ADD_MASTER_PREFIX_TO_NUMBER;
import static com.qcadoo.mes.masterOrders.constants.MasterOrderFields.NUMBER;

@Service
public class MasterOrderDetailsHooks {

    private static final String L_ORDERS = "orders";

    private static final String L_CREATE_ORDER = "createOrder";

    private static final String L_ORDERS_LOOKUP = "ordersLookup";

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

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

    public void onBeforeRender(final ViewDefinitionState view) {
        initState(view);
        setOrderLookupCriteriaModifier(view);
        setDefaultMasterOrderNumber(view);
        disableFields(view);
        ribbonRender(view);
    }

    public void initState(final ViewDefinitionState view) {
        FormComponent orderForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        FieldComponent stateField = (FieldComponent) view.getComponentByReference(MasterOrderFields.STATE);

        stateField.setEnabled(false);

        if (orderForm.getEntityId() != null) {
            return;
        }

        stateField.setFieldValue(MasterOrderState.NEW.getStringValue());
    }

    public void ribbonRender(final ViewDefinitionState view) {
        GridComponent masterOrderProductsGrid = (GridComponent) view
                .getComponentByReference(MasterOrderFields.MASTER_ORDER_PRODUCTS);

        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
        RibbonGroup orders = (RibbonGroup) window.getRibbon().getGroupByName(L_ORDERS);
        RibbonActionItem createOrder = (RibbonActionItem) orders.getItemByName(L_CREATE_ORDER);

        if (masterOrderProductsGrid.getSelectedEntities().isEmpty()) {
            createOrder.setEnabled(false);
        } else if (masterOrderProductsGrid.getSelectedEntities().size() == 1) {
            createOrder.setEnabled(true);
        } else {
            createOrder.setEnabled(false);
        }
        if (PluginUtils.isEnabled("goodFood") && !masterOrderProductsGrid.getEntities().isEmpty()) {
            createOrder.setEnabled(true);
        } else {
            createOrder.setMessage("masterOrders.order.ribbon.message.selectOneProduct");
        }
        createOrder.requestUpdate(true);
        toggleGenerateButton(view);
        window.requestRibbonRender();
    }

    private void toggleGenerateButton(final ViewDefinitionState view) {
        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
        RibbonGroup orders = (RibbonGroup) window.getRibbon().getGroupByName(L_ORDERS);
        RibbonActionItem createOrder = (RibbonActionItem) orders.getItemByName("generateOrders");

        createOrder.setMessage("qcadooView.ribbon.orders.generateOrders.message");

        GridComponent masterOrderProductsGrid = (GridComponent) view
                .getComponentByReference(MasterOrderFields.MASTER_ORDER_PRODUCTS);

        if (masterOrderProductsGrid.getSelectedEntities().isEmpty()) {
            createOrder.setEnabled(false);
        } else {
            createOrder.setEnabled(true);
        }

        createOrder.requestUpdate(true);
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

            numberField.setFieldValue(jdbcTemplate.queryForObject("select generate_master_order_number()",
                    Collections.emptyMap(), String.class));

            numberField.requestComponentUpdateState();
        }
    }

    private boolean checkIfShouldInsertNumber(final ViewDefinitionState state) {
        FormComponent form = (FormComponent) state.getComponentByReference(QcadooViewConstants.L_FORM);
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

    private void disableFields(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
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

    public void fillDefaultTechnology(final ViewDefinitionState view) {
        LookupComponent productField = (LookupComponent) view.getComponentByReference("product");
        FieldComponent defaultTechnologyField = (FieldComponent) view.getComponentByReference("defaultTechnology");
        LookupComponent technologyLookup = (LookupComponent) view.getComponentByReference("technology");

        Entity product = productField.getEntity();

        if (Objects.nonNull(product)) {
            FilterValueHolder holder = technologyLookup.getFilterValue();

            holder.put(TechnologyCriteriaModifiersO.PRODUCT_PARAMETER, product.getId());

            technologyLookup.setFilterValue(holder);

            Entity defaultTechnology = technologyServiceO.getDefaultTechnology(product);

            if (Objects.nonNull(defaultTechnology)) {
                String defaultTechnologyValue = expressionService.getValue(defaultTechnology, "#number + ' - ' + #name",
                        view.getLocale());

                defaultTechnologyField.setFieldValue(defaultTechnologyValue);
                technologyLookup.setFieldValue(defaultTechnology.getId());
            }
        }

    }

}
