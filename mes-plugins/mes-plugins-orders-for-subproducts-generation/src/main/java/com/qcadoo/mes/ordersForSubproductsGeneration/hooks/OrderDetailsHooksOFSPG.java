/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
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
package com.qcadoo.mes.ordersForSubproductsGeneration.hooks;

import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.util.OrderDetailsRibbonHelper;
import com.qcadoo.mes.ordersForSubproductsGeneration.constants.OrderFieldsOFSPG;
import com.qcadoo.mes.ordersForSubproductsGeneration.constants.OrdersForSubproductsGenerationConstans;
import com.qcadoo.mes.ordersForSubproductsGeneration.constants.RelatedOrderDtoFields;
import com.qcadoo.mes.ordersForSubproductsGeneration.constants.RelativePosition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderDetailsHooksOFSPG {

    



    @Autowired
    private OrderDetailsRibbonHelper orderDetailsRibbonHelper;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public final void onBeforeRender(final ViewDefinitionState view) {
        disableNumberFieldForSubOrder(view);
        orderDetailsRibbonHelper.setButtonEnabled(view, "ordersForSubproducts", "ordersForSubproducts",
                OrderDetailsRibbonHelper.HAS_CHECKED_OR_ACCEPTED_TECHNOLOGY::test);
        fillRelatedOrders(view);
    }

    private void disableNumberFieldForSubOrder(final ViewDefinitionState view) {
        FormComponent orderForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Long orderId = orderForm.getEntityId();
        if (orderId == null) {
            return;
        }
        Entity order = orderForm.getPersistedEntityWithIncludedFormValues();

        if (order.getBelongsToField(OrderFieldsOFSPG.PARENT) != null) {
            FieldComponent number = (FieldComponent) view.getComponentByReference(OrderFields.NUMBER);
            number.setEnabled(false);

        }

    }

    private void fillRelatedOrders(final ViewDefinitionState view) {

        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity baseOrder = form.getPersistedEntityWithIncludedFormValues();
        if (baseOrder.getId() != null && baseOrder.getHasManyField(OrderFieldsOFSPG.RELATED_ORDERS).isEmpty()) {
            List<Entity> relatedOrders;
            Entity rootOrder = baseOrder.getBelongsToField(OrderFieldsOFSPG.ROOT);
            if (rootOrder == null) {
                relatedOrders = dataDefinitionService
                        .get(OrdersForSubproductsGenerationConstans.PLUGIN_IDENTIFIER,
                                OrdersForSubproductsGenerationConstans.MODEL_RELATED_ORDER_DTO).find()
                        .add(SearchRestrictions.idNe(baseOrder.getId()))
                        .add(SearchRestrictions.eq(RelatedOrderDtoFields.ROOT_ID, baseOrder.getId().intValue())).list()
                        .getEntities();
            } else {
                relatedOrders = dataDefinitionService
                        .get(OrdersForSubproductsGenerationConstans.PLUGIN_IDENTIFIER,
                                OrdersForSubproductsGenerationConstans.MODEL_RELATED_ORDER_DTO)
                        .find()
                        .add(SearchRestrictions.idNe(baseOrder.getId()))
                        .add(SearchRestrictions.or(
                                SearchRestrictions.eq(RelatedOrderDtoFields.ROOT_ID, rootOrder.getId().intValue()),
                                SearchRestrictions.eq("id", rootOrder.getId()))).list().getEntities();
            }
            fillRelativePositions(relatedOrders, baseOrder);

            GridComponent relatedOrdersGrid = (GridComponent) view.getComponentByReference(OrderFieldsOFSPG.RELATED_ORDERS);
            relatedOrdersGrid.setEntities(relatedOrders);
        }
    }

    private void fillRelativePositions(List<Entity> relatedOrders, final Entity baseOrder) {
        Integer baseLevel = baseOrder.getIntegerField(OrderFieldsOFSPG.LEVEL);
        if (baseLevel == null) {
            baseLevel = 0;
        }
        for (Entity relatedOrder : relatedOrders) {
            Integer relatedLevel = relatedOrder.getIntegerField(OrderFieldsOFSPG.LEVEL);

            if (relatedLevel != null) {
                relatedOrder.setField(RelatedOrderDtoFields.RELATIVE_POSITION,
                        RelativePosition.getComparedPosition(relatedLevel.compareTo(baseLevel)).getStringValue());
            }
        }
    }
}