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
package com.qcadoo.mes.ordersForSubproductsGeneration.productionScheduling.hooks;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.ordersForSubproductsGeneration.constants.CoverageForOrderFieldsOFSPG;
import com.qcadoo.mes.ordersForSubproductsGeneration.productionScheduling.criteriaModifiers.OrderTimeCalculationCM;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class OperationDurationInOrderDetailsHooksOFSPG {

    



    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ParameterService parameterService;

    public void onBeforeRender(final ViewDefinitionState view) {
        setEnableViewState(view);
        setCriteriaModifierParameters(view);
        setSaveButtonState(view);
    }

    private void setEnableViewState(final ViewDefinitionState view) {
        List<String> reference = Lists.newArrayList("includeOrdersForComponent");

        if (hasSubOrders(view)) {
            enable(view, true, reference);
        } else {
            enable(view, false, reference);
        }
    }

    public void enable(final ViewDefinitionState view, final boolean flag, List<String> references) {
        for (String reference : references) {
            FieldComponent fieldComponent = (FieldComponent) view.getComponentByReference(reference);
            fieldComponent.setEnabled(flag);
            fieldComponent.requestComponentUpdateState();
        }
    }

    private boolean hasSubOrders(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Long orderId = form.getEntityId();

        if (orderId == null) {
            return false;
        }

        if (getTechnologiesIdsForOrder(orderId).isEmpty()) {
            return false;
        }

        return true;
    }

    private DataDefinition getOrderDD() {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER);
    }

    private List<Long> getTechnologiesIdsForOrder(final Long orderID) {
        List<Long> ids = Lists.newArrayList();

        String sql = "SELECT otech.id AS techID FROM #orders_order AS o LEFT JOIN o.technology AS otech WHERE o.root = :orderID";

        List<Entity> entities = getOrderDD().find(sql).setLong("orderID", orderID).list().getEntities();

        for (Entity entity : entities) {
            ids.add((Long) entity.getField("techID"));
        }

        return ids;
    }

    private void setCriteriaModifierParameters(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Long orderId = form.getEntityId();

        if (orderId == null) {
            return;
        }

        GridComponent orderTimeCalculationsGrid = (GridComponent) view.getComponentByReference("orderTimeCalculationsGrid");

        FilterValueHolder gridHolder = orderTimeCalculationsGrid.getFilterValue();
        gridHolder.put(OrderTimeCalculationCM.PARENT_COMPONENTS_ORDER, orderId);
        orderTimeCalculationsGrid.setFilterValue(gridHolder);
    }

    private void setSaveButtonState(final ViewDefinitionState view) {
        WindowComponent windowComponent = ((WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW));
        RibbonActionItem ribbonActionItem = windowComponent.getRibbon().getGroupByName("subOrders")
                .getItemByName(CoverageForOrderFieldsOFSPG.SAVE_DATE_IN_SUBORDERS);
        String dateStart = (String) view.getComponentByReference(CoverageForOrderFieldsOFSPG.CALCULATED_START_ALL_ORDERS)
                .getFieldValue();
        String dateEnd = (String) view.getComponentByReference(CoverageForOrderFieldsOFSPG.CALCULATED_FINISH_ALL_ORDERS)
                .getFieldValue();

        if (dateStart.isEmpty() || dateEnd.isEmpty()) {
            ribbonActionItem.setEnabled(false);
        } else {
            ribbonActionItem.setEnabled(true);
        }

        ribbonActionItem.requestUpdate(true);
    }

}
