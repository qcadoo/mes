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
package com.qcadoo.mes.masterOrders.listeners;

import com.google.common.collect.Maps;
import com.qcadoo.mes.masterOrders.OrdersFromMOProductsGenerationService;
import com.qcadoo.mes.masterOrders.constants.MasterOrderFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderProductFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderState;
import com.qcadoo.mes.masterOrders.hooks.MasterOrderDetailsHooks;
import com.qcadoo.mes.orders.TechnologyServiceO;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.ExpressionService;
import com.qcadoo.plugin.api.PluginUtils;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    @Autowired
    private OrdersFromMOProductsGenerationService ordersGenerationService;

    public void onAddExistingEntity(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        form.performEvent(view, "reset", new String[0]);
    }

    public void onRemoveSelectedEntity(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        form.performEvent(view, "reset", new String[0]);
    }

    public void changeState(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent masterOrderForm = (FormComponent) view.getComponentByReference(L_FORM);
        Entity masterOrder = masterOrderForm.getEntity();
        Entity masterOrderDB = masterOrder.getDataDefinition().get(masterOrder.getId());

        String status = args[0];

        if(status.equals(MasterOrderState.COMPLETED.getStringValue())) {
            masterOrderDB.setField(MasterOrderFields.STATE, MasterOrderState.COMPLETED.getStringValue());
            masterOrderDB.getDataDefinition().save(masterOrderDB);
        } else if(status.equals(MasterOrderState.DECLINED.getStringValue())){
            masterOrderDB.setField(MasterOrderFields.STATE, MasterOrderState.DECLINED.getStringValue());
            masterOrderDB.getDataDefinition().save(masterOrderDB);
        }
        state.performEvent(view, "reset", new String[0]);
    }

    public void clearAddress(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        LookupComponent address = (LookupComponent) view.getComponentByReference(MasterOrderFields.ADDRESS);

        address.setFieldValue(null);
    }

    public void onProductsChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent masterOrderProductsGrid = (GridComponent) view
                .getComponentByReference(MasterOrderFields.MASTER_ORDER_PRODUCTS);

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

    public void refreshView(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent masterOrderForm = (FormComponent) view.getComponentByReference(L_FORM);
        masterOrderForm.performEvent(view, "refresh");
    }

    public void generateOrders(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent masterOrderForm = (FormComponent) view.getComponentByReference(L_FORM);
        GridComponent masterOrderProductsGrid = (GridComponent) view
                .getComponentByReference(MasterOrderFields.MASTER_ORDER_PRODUCTS);
        List<Entity> masterOrderProducts = masterOrderProductsGrid.getSelectedEntities();

        ordersGenerationService.generateOrders(masterOrderProducts, true).showMessage(view);
    }

    public void createOrder(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent masterOrderForm = (FormComponent) view.getComponentByReference(L_FORM);
        Entity masterOrder = masterOrderForm.getEntity();

        Long masterOrderId = masterOrder.getId();

        if (masterOrderId == null) {
            return;
        }

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("form.masterOrder", masterOrderId);

        GridComponent masterOrderProductsGrid = (GridComponent) view
                .getComponentByReference(MasterOrderFields.MASTER_ORDER_PRODUCTS);

        if (PluginUtils.isEnabled("goodFood")) {
            Entity entity = extractMasterOrderProduct(masterOrderProductsGrid.getEntities().get(0));
            Entity product = entity.getBelongsToField(MasterOrderProductFields.PRODUCT);
            parameters.put("form.masterOrderProduct", product.getId());
            parameters.put("form.masterOrderProductComponent", entity.getId());
        } else {
            Entity entity = extractMasterOrderProduct(masterOrderProductsGrid.getSelectedEntities().get(0));
            Entity product = entity.getBelongsToField(MasterOrderProductFields.PRODUCT);
            parameters.put("form.masterOrderProduct", product.getId());
            parameters.put("form.masterOrderProductComponent", entity.getId());
        }

        parameters.put(L_WINDOW_ACTIVE_MENU, "orders.productionOrders");

        String url = "../page/orders/orderDetails.html";

        view.redirectTo(url, false, true, parameters);
    }

    private Entity extractMasterOrderProduct(Entity masterOrderProduct) {
        Optional<Entity> dtoEntity = Optional.ofNullable(masterOrderProduct.getDataDefinition().getMasterModelEntity(
                masterOrderProduct.getId()));
        if (dtoEntity.isPresent()) {
           return dtoEntity.get();
        } else {
            return masterOrderProduct;
        }
    }

}
