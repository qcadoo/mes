package com.qcadoo.mes.producedProducts;

/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.4.2
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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialRequirements.api.MaterialRequirementReportDataService;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class ProducedProductsService {

    @Autowired
    private MaterialRequirementReportDataService materialRequirementReportDataService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void updateProducedProductsForOrder(final Entity order) {

        List<Entity> orderList = Arrays.asList(order);

        Map<Entity, BigDecimal> products = materialRequirementReportDataService.getQuantitiesForOrdersTechnologyProducts(
                orderList, true);

        List<Entity> producedproducts = dataDefinitionService.get("producedProducts", "producedProducts").find()
                .add(SearchRestrictions.belongsTo("order", order)).list().getEntities();

        for (Entry<Entity, BigDecimal> product : products.entrySet()) {
            Entity found = null;

            for (Entity producedproduct : producedproducts) {
                if (producedproduct.getBelongsToField("product").equals(product.getKey())) {
                    found = producedproduct;
                    break;
                }
            }

            if (found == null) {
                Entity newEntry = dataDefinitionService.get("producedProducts", "producedProducts").create();
                newEntry.setField("order", order);
                newEntry.setField("product", product.getKey());
                newEntry.setField("plannedQuantity", product.getValue());
                dataDefinitionService.get("producedProducts", "producedProducts").save(newEntry);
            } else {
                BigDecimal currentPlannedQuantity = (BigDecimal) found.getField("plannedQuantity");
                if (currentPlannedQuantity != product.getValue()) {
                    found.setField("plannedQuantity", product.getValue());
                    dataDefinitionService.get("producedProducts", "producedProducts").save(found);
                }
            }
        }

        producedproducts = dataDefinitionService.get("producedProducts", "producedProducts").find()
                .add(SearchRestrictions.belongsTo("order", order)).list().getEntities();
        for (Entity producedproduct : producedproducts) {
            boolean found = false;
            for (Entry<Entity, BigDecimal> product : products.entrySet()) {
                if (producedproduct.getBelongsToField("product").equals(product.getKey())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                dataDefinitionService.get("producedProducts", "producedProducts").delete(producedproduct.getId());
            }
        }

    }

    public void showProducedProducts(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState,
            final String[] args) {

        Long orderId = (Long) triggerState.getFieldValue();

        if (orderId != null) {
            Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(orderId);
            updateProducedProductsForOrder(order);

            String url = "../page/producedProducts/producedProducts.html?context={\"order.id\":\"" + orderId + "\"}";

            viewDefinitionState.openModal(url);
        }
    }

    public void disablePlannedQuantity(final ViewDefinitionState state) {
        ComponentState form = state.getComponentByReference("form");
        if (form.getFieldValue() != null) {
            FieldComponent plannedQuantity = (FieldComponent) state.getComponentByReference("plannedQuantity");
            plannedQuantity.setEnabled(false);
        }
    }

}