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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.qcadoo.mes.masterOrders.OrdersFromMOProductsGenerationService;
import com.qcadoo.mes.masterOrders.constants.MasterOrderFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderProductFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderState;
import com.qcadoo.mes.masterOrders.constants.MasterOrdersConstants;
import com.qcadoo.mes.masterOrders.constants.ProductsBySizeHelperFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.plugin.api.PluginUtils;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class MasterOrderDetailsListeners {

    private static final String L_WINDOW_ACTIVE_MENU = "window.activeMenu";

    @Autowired
    private OrdersFromMOProductsGenerationService ordersGenerationService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void onAddExistingEntity(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        form.performEvent(view, "reset");
    }

    public void onRemoveSelectedEntity(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        form.performEvent(view, "reset");
    }

    public void changeState(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent masterOrderForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity masterOrder = masterOrderForm.getEntity();
        Entity masterOrderDB = masterOrder.getDataDefinition().get(masterOrder.getId());

        String status = args[0];

        if (status.equals(MasterOrderState.COMPLETED.getStringValue())) {
            masterOrderDB.setField(MasterOrderFields.STATE, MasterOrderState.COMPLETED.getStringValue());
            masterOrderDB.getDataDefinition().save(masterOrderDB);
        } else if (status.equals(MasterOrderState.DECLINED.getStringValue())) {
            masterOrderDB.setField(MasterOrderFields.STATE, MasterOrderState.DECLINED.getStringValue());
            masterOrderDB.getDataDefinition().save(masterOrderDB);
        }
        state.performEvent(view, "reset");
    }

    public void clearAddress(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        LookupComponent address = (LookupComponent) view.getComponentByReference(MasterOrderFields.ADDRESS);

        address.setFieldValue(null);
    }

    public void onProductsChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent masterOrderProductsGrid = (GridComponent) view
                .getComponentByReference(MasterOrderFields.MASTER_ORDER_PRODUCTS);

        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
        RibbonGroup orders = window.getRibbon().getGroupByName("orders");
        RibbonActionItem createOrder = orders.getItemByName("createOrder");

        if (masterOrderProductsGrid.getSelectedEntities().isEmpty()) {
            createOrder.setEnabled(false);
        } else {
            createOrder.setEnabled(masterOrderProductsGrid.getSelectedEntities().size() == 1);
        }

        createOrder.requestUpdate(true);
    }

    public void refreshView(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent masterOrderForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        masterOrderForm.performEvent(view, "refresh");
    }

    public void addProductsBySize(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity helper = dataDefinitionService
                .get(MasterOrdersConstants.PLUGIN_IDENTIFIER, MasterOrdersConstants.MODEL_PRODUCTS_BY_SIZE_HELPER).create();
        helper.setField(ProductsBySizeHelperFields.MASTER_ORDER, form.getEntityId());
        helper = helper.getDataDefinition().save(helper);

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("form.id", helper.getId());

        String url = "../page/masterOrders/productsBySize.html";
        view.openModal(url, parameters);
    }

    public void generateOrders(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent masterOrderProductsGrid = (GridComponent) view
                .getComponentByReference(MasterOrderFields.MASTER_ORDER_PRODUCTS);
        List<Entity> masterOrderProducts = masterOrderProductsGrid.getSelectedEntities();
        ordersGenerationService.generateOrders(masterOrderProducts, null, null, true).showMessage(view);
        state.performEvent(view, "reset");
    }

    public void createOrder(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent masterOrderForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
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
        Optional<Entity> dtoEntity = Optional
                .ofNullable(masterOrderProduct.getDataDefinition().getMasterModelEntity(masterOrderProduct.getId()));
        return dtoEntity.orElse(masterOrderProduct);
    }

}
