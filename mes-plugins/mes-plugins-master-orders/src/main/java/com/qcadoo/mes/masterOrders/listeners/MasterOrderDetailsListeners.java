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
package com.qcadoo.mes.masterOrders.listeners;

import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.masterOrders.MasterOrderDocumentService;
import com.qcadoo.mes.masterOrders.OrdersFromMOProductsGenerationService;
import com.qcadoo.mes.masterOrders.constants.*;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MasterOrderDetailsListeners {

    private static final String L_WINDOW_ACTIVE_MENU = "window.activeMenu";

    private static final String L_MASTER_ORDER_RELEASE_LOCATION = "masterOrderReleaseLocation";

    private static final String L_ORDERS = "orders";

    private static final String L_CREATE_ORDER = "createOrder";

    @Autowired
    private OrdersFromMOProductsGenerationService ordersGenerationService;

    @Autowired
    private MasterOrderDocumentService masterOrderDocumentService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ParameterService parameterService;

    public void onAddExistingEntity(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent masterOrderForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        masterOrderForm.performEvent(view, "reset");
    }

    public void onRemoveSelectedEntity(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent masterOrderForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        masterOrderForm.performEvent(view, "reset");
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
        LookupComponent addressLookup = (LookupComponent) view.getComponentByReference(MasterOrderFields.ADDRESS);

        addressLookup.setFieldValue(null);
    }

    public void onProductsChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent masterOrderProductsGrid = (GridComponent) view
                .getComponentByReference(MasterOrderFields.MASTER_ORDER_PRODUCTS);

        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
        RibbonGroup orders = window.getRibbon().getGroupByName(L_ORDERS);
        RibbonActionItem createOrder = orders.getItemByName(L_CREATE_ORDER);

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
        FormComponent masterOrderForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity productsBySizeHelper = getProductsBySizeHelperDD().create();

        productsBySizeHelper.setField(ProductsBySizeHelperFields.MASTER_ORDER, masterOrderForm.getEntityId());

        productsBySizeHelper = productsBySizeHelper.getDataDefinition().save(productsBySizeHelper);

        Map<String, Object> parameters = Maps.newHashMap();

        parameters.put("form.id", productsBySizeHelper.getId());

        String url = "../page/masterOrders/productsBySize.html";

        view.openModal(url, parameters);
    }

    public void addProductsByAttribute(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent masterOrderForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity productsByAttributeHelper = getProductsByAttributeHelperDD().create();

        productsByAttributeHelper.setField(ProductsByAttributeHelperFields.MASTER_ORDER, masterOrderForm.getEntityId());

        productsByAttributeHelper = productsByAttributeHelper.getDataDefinition().save(productsByAttributeHelper);

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("form.id", productsByAttributeHelper.getId());

        String url = "../page/masterOrders/productsByAttribute.html";
        view.openModal(url, parameters);
    }

    public void createReleaseDocument(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent masterOrderProductsGrid = (GridComponent) view
                .getComponentByReference(MasterOrderFields.MASTER_ORDER_PRODUCTS);

        List<Entity> masterOrderProducts = masterOrderProductsGrid.getSelectedEntities();
        Entity masterOrderReleaseLocation = parameterService.getParameter().getBelongsToField(L_MASTER_ORDER_RELEASE_LOCATION);

        if (Objects.isNull(masterOrderReleaseLocation)) {
            view.addMessage("masterOrders.masterOrder.createReleaseDocument.masterOrderReleaseLocationIsEmpty", ComponentState.MessageType.FAILURE);

            return;
        }

        boolean anyZeroPositions = masterOrderProducts.stream().anyMatch(mo -> mo.getDecimalField(MasterOrderProductFields.MASTER_ORDER_QUANTITY).compareTo(BigDecimal.ZERO) == 0);

        if (anyZeroPositions) {
            view.addMessage("masterOrders.masterOrder.createReleaseDocument.masterOrderPositionsForZero", ComponentState.MessageType.FAILURE);

            return;
        }

        masterOrderDocumentService.createReleaseDocument(masterOrderProducts, view);
    }

    public void generateOrders(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent masterOrderProductsGrid = (GridComponent) view
                .getComponentByReference(MasterOrderFields.MASTER_ORDER_PRODUCTS);

        List<Entity> masterOrderProducts = masterOrderProductsGrid.getSelectedEntities();

        Set<Long> masterOrderProductIds = masterOrderProducts.stream().map(Entity::getId).collect(Collectors.toSet());

        if (checkMasterOrderProductAttrValues(masterOrderProductIds)) {
            ordersGenerationService.generateOrders(masterOrderProducts, null, null, true).showMessage(view);

            state.performEvent(view, "reset");
        } else {
            view.addMessage("masterOrders.masterOrderDetails.generateGroup.masterOrderProductAttrValuesNotFilled", ComponentState.MessageType.INFO);
        }
    }

    public boolean checkMasterOrderProductAttrValues(final Set<Long> masterOrderProductIds) {
        List<Entity> masterOrderProducts = getMasterOrderProducts(masterOrderProductIds);

        for (Entity masterOrderProduct : masterOrderProducts) {
            List<Entity> masterOrderProductAttrValues = masterOrderProduct.getHasManyField(MasterOrderProductFields.MASTER_ORDER_PRODUCT_ATTR_VALUES);

            if (masterOrderProductAttrValues.stream().anyMatch(masterOrderProductAttrValue -> StringUtils.isEmpty(masterOrderProductAttrValue.getStringField(MasterOrderProductAttrValueFields.VALUE)))) {
                return false;
            }
        }

        return true;
    }

    private List<Entity> getMasterOrderProducts(final Set<Long> masterOrderProductIds) {
        return getMasterOrderProductDD().find().add(SearchRestrictions.in("id", masterOrderProductIds)).list().getEntities();
    }

    public void createOrder(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent masterOrderForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity masterOrder = masterOrderForm.getEntity();

        Long masterOrderId = masterOrder.getId();

        if (Objects.isNull(masterOrderId)) {
            return;
        }

        Map<String, Object> parameters = Maps.newHashMap();

        parameters.put("form.masterOrder", masterOrderId);

        GridComponent masterOrderProductsGrid = (GridComponent) view
                .getComponentByReference(MasterOrderFields.MASTER_ORDER_PRODUCTS);

        Entity masterOrderProduct;

        if (PluginUtils.isEnabled("goodFood")) {
            masterOrderProduct = extractMasterOrderProduct(masterOrderProductsGrid.getEntities().get(0));
        } else {
            masterOrderProduct = extractMasterOrderProduct(masterOrderProductsGrid.getSelectedEntities().get(0));
        }

        Entity product = masterOrderProduct.getBelongsToField(MasterOrderProductFields.PRODUCT);

        if (checkMasterOrderProductAttrValues(masterOrderProduct)) {
            parameters.put("form.masterOrderProduct", product.getId());
            parameters.put("form.masterOrderProductComponent", masterOrderProduct.getId());

            parameters.put(L_WINDOW_ACTIVE_MENU, "orders.productionOrders");

            String url = "../page/orders/orderDetails.html";

            view.redirectTo(url, false, true, parameters);
        } else {
            view.addMessage("masterOrders.masterOrderDetails.generateGroup.masterOrderProductAttrValuesNotFilled", ComponentState.MessageType.INFO);
        }
    }

    private boolean checkMasterOrderProductAttrValues(final Entity masterOrderProduct) {
        List<Entity> masterOrderProductAttrValues = masterOrderProduct.getHasManyField(MasterOrderProductFields.MASTER_ORDER_PRODUCT_ATTR_VALUES);

        return masterOrderProductAttrValues.stream().noneMatch(masterOrderProductAttrValue -> StringUtils.isEmpty(masterOrderProductAttrValue.getStringField(MasterOrderProductAttrValueFields.VALUE)));
    }

    private Entity extractMasterOrderProduct(final Entity masterOrderProduct) {
        Optional<Entity> dtoEntity = Optional
                .ofNullable(masterOrderProduct.getDataDefinition().getMasterModelEntity(masterOrderProduct.getId()));

        return dtoEntity.orElse(masterOrderProduct);
    }

    private DataDefinition getMasterOrderProductDD() {
        return dataDefinitionService
                .get(MasterOrdersConstants.PLUGIN_IDENTIFIER, MasterOrdersConstants.MODEL_MASTER_ORDER_PRODUCT);
    }

    private DataDefinition getProductsBySizeHelperDD() {
        return dataDefinitionService
                .get(MasterOrdersConstants.PLUGIN_IDENTIFIER, MasterOrdersConstants.MODEL_PRODUCTS_BY_SIZE_HELPER);
    }

    private DataDefinition getProductsByAttributeHelperDD() {
        return dataDefinitionService
                .get(MasterOrdersConstants.PLUGIN_IDENTIFIER, MasterOrdersConstants.MODEL_PRODUCTS_BY_ATTRIBUTE_HELPER);
    }

}
