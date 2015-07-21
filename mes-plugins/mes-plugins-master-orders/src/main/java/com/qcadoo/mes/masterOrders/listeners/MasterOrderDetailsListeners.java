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
package com.qcadoo.mes.masterOrders.listeners;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.qcadoo.mes.masterOrders.constants.MasterOrderFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderProductFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderType;
import com.qcadoo.mes.masterOrders.hooks.MasterOrderDetailsHooks;
import com.qcadoo.mes.orders.TechnologyServiceO;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.ExpressionService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;

@Service
public class MasterOrderDetailsListeners {

    private static final String L_FORM = "form";

    private static final String L_WINDOW = "window";

    private static final String L_WINDOW_ACTIVE_MENU = "window.activeMenu";

    @Autowired
    private ExpressionService expressionService;

    @Autowired
    private TechnologyServiceO technologyServiceO;

    @Autowired
    private MasterOrderDetailsHooks masterOrderDetailsHooks;

    public void onProductsChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent masterOrderProductsGrid = (GridComponent) view.getComponentByReference(MasterOrderFields.MASTER_ORDER_PRODUCTS);

        WindowComponent window = (WindowComponent) view.getComponentByReference(L_WINDOW);
        RibbonGroup orders = (RibbonGroup) window.getRibbon().getGroupByName("orders");
        RibbonActionItem createOrder = (RibbonActionItem) orders.getItemByName("createOrder");
        if (masterOrderProductsGrid.getSelectedEntities().isEmpty()) {
            createOrder.setEnabled(false);
        } else if (masterOrderProductsGrid.getSelectedEntities().size() == 1) {
            createOrder.setEnabled(true);
        } else {
            createOrder.setEnabled(false);
        }
        createOrder.requestUpdate(true);
    }

    public void hideFieldDependOnMasterOrderType(final ViewDefinitionState view, final ComponentState state,
            final String[] args) {
        masterOrderDetailsHooks.hideFieldDependOnMasterOrderType(view);
    }

    public void fillUnitField(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        masterOrderDetailsHooks.fillUnitField(view);
    }

    public void fillDefaultTechnology(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        LookupComponent productField = (LookupComponent) view.getComponentByReference(MasterOrderFields.PRODUCT);
        FieldComponent defaultTechnology = (FieldComponent) view.getComponentByReference(MasterOrderFields.DEFAULT_TECHNOLOGY);
        FieldComponent technology = (FieldComponent) view.getComponentByReference(MasterOrderFields.TECHNOLOGY);

        Entity product = productField.getEntity();

        if (product == null || technologyServiceO.getDefaultTechnology(product) == null) {
            defaultTechnology.setFieldValue(null);
            technology.setFieldValue(null);
            defaultTechnology.requestComponentUpdateState();
            technology.requestComponentUpdateState();

            return;
        }

        Entity defaultTechnologyEntity = technologyServiceO.getDefaultTechnology(product);
        String defaultTechnologyValue = expressionService.getValue(defaultTechnologyEntity, "#number + ' - ' + #name",
                view.getLocale());

        defaultTechnology.setFieldValue(defaultTechnologyValue);
        technology.setFieldValue(defaultTechnologyEntity.getId());

        defaultTechnology.requestComponentUpdateState();
        technology.requestComponentUpdateState();
    }

    public void refreshView(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent masterOrderForm = (FormComponent) view.getComponentByReference(L_FORM);
        masterOrderForm.performEvent(view, "refresh");
    }

    public void setUneditableWhenEntityHasUnsaveChanges(final ViewDefinitionState view, final ComponentState state,
            final String[] args) {
        masterOrderDetailsHooks.setUneditableWhenEntityHasUnsaveChanges(view);
    }

    public void createOrder(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FieldComponent masterOrderType = (FieldComponent) view.getComponentByReference(MasterOrderFields.MASTER_ORDER_TYPE);
        Object masterOrderTypeValue = masterOrderType.getFieldValue();

        FormComponent masterOrderForm = (FormComponent) view.getComponentByReference(L_FORM);
        Entity masterOrder = masterOrderForm.getEntity();

        Long masterOrderId = masterOrder.getId();

        if (masterOrderId == null) {
            return;
        }

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("form.masterOrder", masterOrderId);

        if (masterOrderTypeValue.equals(MasterOrderType.MANY_PRODUCTS.getStringValue())) {
            GridComponent masterOrderProductsGrid = (GridComponent) view.getComponentByReference(MasterOrderFields.MASTER_ORDER_PRODUCTS);
            Entity entity = masterOrderProductsGrid.getSelectedEntities().get(0).getBelongsToField(MasterOrderProductFields.PRODUCT);
            parameters.put("form.masterOrderProduct", entity.getId());
        }

        parameters.put(L_WINDOW_ACTIVE_MENU, "orders.productionOrders");

        String url = "../page/orders/orderDetails.html";
        view.redirectTo(url, false, true, parameters);
    }

}
