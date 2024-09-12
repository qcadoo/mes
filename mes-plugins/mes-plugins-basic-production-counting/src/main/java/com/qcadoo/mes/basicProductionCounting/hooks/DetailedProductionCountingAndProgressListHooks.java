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
package com.qcadoo.mes.basicProductionCounting.hooks;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.constants.GlobalTypeOfMaterial;
import com.qcadoo.mes.basicProductionCounting.ProductionTrackingUpdateService;
import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingConstants;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityRole;
import com.qcadoo.mes.basicProductionCounting.hooks.util.ProductionProgressModifyLockHelper;
import com.qcadoo.mes.orders.OrderService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderStateStringValues;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityOpResult;
import com.qcadoo.model.api.validators.ErrorMessage;
import com.qcadoo.plugin.api.PluginUtils;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DetailedProductionCountingAndProgressListHooks {

    private static final Logger LOG = LoggerFactory.getLogger(DetailedProductionCountingAndProgressListHooks.class);

    private static final String L_ORDER = "order";

    @Autowired
    private OrderService orderService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ProductionProgressModifyLockHelper progressModifyLockHelper;

    @Autowired
    private ProductionTrackingUpdateService productionTrackingUpdateService;

    public void useReplacement(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent grid = (GridComponent) view
                .getComponentByReference(QcadooViewConstants.L_GRID);

        if (!grid.getSelectedEntitiesIds().isEmpty()
                && grid.getSelectedEntitiesIds().size() == 1) {

            Entity pcq = dataDefinitionService
                    .get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                            BasicProductionCountingConstants.MODEL_PRODUCTION_COUNTING_QUANTITY)
                    .get(grid.getSelectedEntitiesIds().stream().findFirst().get());

            Map<String, Object> parameters = Maps.newHashMap();
            parameters.put("form.productionCountingQuantity", pcq.getId());
            parameters.put("form.basicProduct", pcq
                    .getBelongsToField(ProductionCountingQuantityFields.PRODUCT).getId());

            String url = "/basicProductionCounting/useReplacement.html";
            view.openModal(url, parameters);
        }
    }

    public void performActionOnBack(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        if (PluginUtils.isEnabled("productionCounting")
                && view.getJsonContext().has("window.mainTab.basicProductionCounting.productionTrackingId")) {
            try {
                Long productionTrackingId = view.getJsonContext()
                        .getLong("window.mainTab.basicProductionCounting.productionTrackingId");
                productionTrackingUpdateService.updateProductionTracking(productionTrackingId);
            } catch (JSONException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Can't update production tracking.");
                }
            }
        }
    }

    public void setGridEditableDependsOfOrderState(final ViewDefinitionState view) {
        FormComponent orderForm = (FormComponent) view.getComponentByReference(L_ORDER);
        GridComponent grid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);

        Long orderId = orderForm.getEntityId();
        if (orderId == null) {
            return;
        }
        if (disable(orderService.getOrder(orderId))) {
            grid.setEnabled(false);
        } else {
            boolean isLocked = progressModifyLockHelper.isLocked(orderService.getOrder(orderId));
            grid.setEnabled(!isLocked);
        }
    }

    private boolean disable(Entity order) {
        if (order.getBelongsToField(OrderFields.TECHNOLOGY) == null) {
            return true;
        }

        String state = order.getStringField(OrderFields.STATE);

        return OrderStateStringValues.COMPLETED.equals(state) || OrderStateStringValues.DECLINED.equals(state)
                || OrderStateStringValues.ABANDONED
                .equals(state);
    }

    public void onRemoveSelectedProductionCountingQuantities(final ViewDefinitionState view, final ComponentState state,
                                                             final String[] args) {
        GridComponent grid = ((GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID));
        List<Entity> selectedEntities = grid.getSelectedEntities();
        List<Long> ids = new ArrayList<>();

        boolean deleteSuccessful = true;
        List<ErrorMessage> errors = Lists.newArrayList();
        for (Entity productionCountingQuantity : selectedEntities) {
            String typeOfMaterial = productionCountingQuantity.getStringField(ProductionCountingQuantityFields.TYPE_OF_MATERIAL);
            String role = productionCountingQuantity.getStringField(ProductionCountingQuantityFields.ROLE);

            if (GlobalTypeOfMaterial.FINAL_PRODUCT.getStringValue().equals(typeOfMaterial)) {
                state.addMessage("basicProductionCounting.productionCountingQuantity.error.cantDeleteFinal",
                        ComponentState.MessageType.INFO);
            } else if (GlobalTypeOfMaterial.INTERMEDIATE.getStringValue().equals(typeOfMaterial)
                    && ProductionCountingQuantityRole.PRODUCED.getStringValue().equals(role)) {
                state.addMessage("basicProductionCounting.productionCountingQuantity.error.cantDeleteIntermediate",
                        ComponentState.MessageType.INFO);
            } else {
                ids.add(productionCountingQuantity.getId());
                if (deleteSuccessful) {
                    EntityOpResult result = productionCountingQuantity.getDataDefinition()
                            .delete(productionCountingQuantity.getId());
                    if (!result.isSuccessfull()) {
                        deleteSuccessful = false;
                        errors.addAll(result.getMessagesHolder().getGlobalErrors());
                    }
                }
            }
        }

        if (ids.size() == 1 && deleteSuccessful) {
            state.addMessage("qcadooView.message.deleteMessage", ComponentState.MessageType.SUCCESS);

        } else if (ids.size() > 1 && deleteSuccessful) {
            state.addMessage("qcadooView.message.deleteMessages", ComponentState.MessageType.SUCCESS, String.valueOf(ids.size()));
        } else if (!deleteSuccessful) {
            errors.forEach(state::addMessage);
        }
    }

}
