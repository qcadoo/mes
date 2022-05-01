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

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.masterOrders.OrdersFromMOProductsGenerationService;
import com.qcadoo.mes.masterOrders.constants.GeneratingOrdersHelperFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderPositionDtoFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrdersConstants;
import com.qcadoo.mes.masterOrders.helpers.MasterOrderPositionsHelper;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class MasterOrderPositionsListListeners {

    private static final String L_GENERATED = "generated";

    private static final String L_WINDOW_ACTIVE_MENU = "window.activeMenu";

    private static final String L_CREATE_COLLECTIVE_ORDERS = "createCollectiveOrders";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private OrdersFromMOProductsGenerationService ordersGenerationService;

    @Autowired
    private MasterOrderPositionsHelper masterOrderPositionsHelper;

    public void createOrder(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent masterOrderPositionGrid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);

        List<Entity> selectedEntity = masterOrderPositionGrid.getSelectedEntities();

        if (selectedEntity.isEmpty()) {
            state.addMessage("masterOrders.masterOrder.masterOrdersPosition.lessEntitiesSelectedThanAllowed",
                    ComponentState.MessageType.INFO);

            return;
        } else if (selectedEntity.size() != 1) {
            state.addMessage("masterOrders.masterOrder.masterOrdersPosition.moreEntitiesSelectedThanAllowed",
                    ComponentState.MessageType.INFO);

            return;
        }

        Entity masterOrderPosition = selectedEntity.get(0);

        Integer masterOrderId = masterOrderPosition.getIntegerField(MasterOrderPositionDtoFields.MASTER_ORDER_ID);

        if (Objects.isNull(masterOrderId)) {
            return;
        }

        Map<String, Object> parameters = Maps.newHashMap();

        parameters.put("form.masterOrder", masterOrderId);

        Integer productId = masterOrderPosition.getIntegerField(MasterOrderPositionDtoFields.PRODUCT_ID);
        Integer masterOrderProductId = masterOrderPosition.getIntegerField(MasterOrderPositionDtoFields.MASTER_ORDER_PRODUCT_ID);

        parameters.put("form.masterOrderProduct", productId);
        parameters.put("form.masterOrderProductComponent", masterOrderProductId);

        parameters.put(L_WINDOW_ACTIVE_MENU, "orders.productionOrders");

        String url = "../page/orders/orderDetails.html";
        view.redirectTo(url, false, true, parameters);
    }

    public void goToGenerateOrders(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent masterOrderPositionGrid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);

        Set<Long> selected = masterOrderPositionGrid.getSelectedEntitiesIds();

        boolean createCollectiveOrders = parameterService.getParameter().getBooleanField(L_CREATE_COLLECTIVE_ORDERS);

        if (createCollectiveOrders) {
            Entity generatingOrders = getGeneratingOrdersHelperDD().create();
            generatingOrders.setField(GeneratingOrdersHelperFields.SELECTED_ENTITIES,
                    selected.stream().map(Object::toString).collect(Collectors.joining(",")));
            generatingOrders = generatingOrders.getDataDefinition().save(generatingOrders);

            Map<String, Object> parameters = Maps.newHashMap();

            parameters.put("form.id", generatingOrders.getId());

            String url = "../page/masterOrders/generatingOrders.html";
            view.openModal(url, parameters);
        } else {
            List<Entity> masterOrderProducts = getMasterOrderPositionDtoDD().find().add(SearchRestrictions.in("id", selected))
                    .list().getEntities();

            ordersGenerationService.generateOrders(masterOrderProducts, null, null, true).showMessage(view);

            state.performEvent(view, "reset");
        }
    }

    public void showGroupedByProduct(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent masterOrderPositionGrid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);

        Map<String, Object> parameters = Maps.newHashMap();

        parameters.put("positionsIds", masterOrderPositionGrid.getSelectedEntitiesIds().stream().map(String::valueOf).collect(Collectors.joining(",")));

        String url = "../page/masterOrders/masterOrderPositionsGroupedByProductList.html";
        view.redirectTo(url, false, true, parameters);

    }

    public void showGroupedByProductAndDate(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent masterOrderPositionGrid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);

        Map<String, Object> parameters = Maps.newHashMap();

        parameters.put("positionsIds", masterOrderPositionGrid.getSelectedEntitiesIds().stream().map(String::valueOf).collect(Collectors.joining(",")));

        String url = "../page/masterOrders/masterOrderPositionsGroupedByProductAndDateList.html";
        view.redirectTo(url, false, true, parameters);

    }

    public void updateWarehouseStateAndDelivery(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        List<Entity> masterOrderProducts = dataDefinitionService.get(MasterOrdersConstants.PLUGIN_IDENTIFIER,
                MasterOrdersConstants.MODEL_MASTER_ORDER_PRODUCT).find().list().getEntities();
        if (!masterOrderProducts.isEmpty()) {
            Entity parameter = parameterService.getParameter();
            masterOrderPositionsHelper.updateDeliveriesProductQuantities(view, masterOrderProducts, parameter);
            masterOrderPositionsHelper.updateWarehouseStates(masterOrderProducts, parameter);

            view.addMessage("masterOrders.masterOrderPositionsList.updateWarehouseStateAndDelivery.success",
                    ComponentState.MessageType.SUCCESS);
        }
    }

    public void generateOrders(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent masterOrderForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        CheckBoxComponent generatedCheckBox = (CheckBoxComponent) view.getComponentByReference(L_GENERATED);

        Entity helper = masterOrderForm.getPersistedEntityWithIncludedFormValues();
        String selectedEntities = helper.getStringField(GeneratingOrdersHelperFields.SELECTED_ENTITIES);

        Date start = helper.getDateField(GeneratingOrdersHelperFields.START_DATE);
        Date finish = helper.getDateField(GeneratingOrdersHelperFields.FINISH_DATE);

        List<Long> ids = Lists.newArrayList(selectedEntities.split(",")).stream().map(Long::valueOf).collect(Collectors.toList());

        List<Entity> masterOrderProducts = getMasterOrderPositionDtoDD().find().add(SearchRestrictions.in("id", ids)).list()
                .getEntities();

        ordersGenerationService.generateOrders(masterOrderProducts, start, finish, true).showMessage(view);

        generatedCheckBox.setChecked(true);
    }

    public void openMasterOrdersImportPage(final ViewDefinitionState view, final ComponentState state,
                                           final String[] args) {
        view.openModal("../page/masterOrders/masterOrdersImport.html");
    }

    private DataDefinition getGeneratingOrdersHelperDD() {
        return dataDefinitionService.get(MasterOrdersConstants.PLUGIN_IDENTIFIER, MasterOrdersConstants.GENERATING_ORDERS_HELPER);
    }

    private DataDefinition getMasterOrderPositionDtoDD() {
        return dataDefinitionService.get(MasterOrdersConstants.PLUGIN_IDENTIFIER,
                MasterOrdersConstants.MODEL_MASTER_ORDER_POSITION_DTO);
    }

}
