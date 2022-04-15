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

import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.mes.masterOrders.constants.MasterOrderPositionDtoFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrdersConstants;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.constants.QcadooViewConstants;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MasterOrderPositionsListHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    private static final String L_ORDERS = "orders";

    private static final String L_CREATE_ORDER = "createOrder";

    public void disableButton(final ViewDefinitionState view) {
        GridComponent masterOrderPositionComponent = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);

        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
        RibbonGroup ordersRibbonGroup = window.getRibbon().getGroupByName(L_ORDERS);
        RibbonGroup groupingRibbonGroup = window.getRibbon().getGroupByName("grouping");
        RibbonActionItem createOrderRibbonActionItem = ordersRibbonGroup.getItemByName(L_CREATE_ORDER);
        RibbonActionItem showGroupedByProduct = groupingRibbonGroup.getItemByName("showGroupedByProduct");
        RibbonActionItem showGroupedByProductAndDate = groupingRibbonGroup.getItemByName("showGroupedByProductAndDate");
        RibbonActionItem generateOrders = ordersRibbonGroup.getItemByName("generateOrders");
        generateOrders.setMessage("qcadooView.ribbon.orders.generateOrders.message");

        List<Entity> selectedEntities = masterOrderPositionComponent.getSelectedEntities();

        if (selectedEntities.isEmpty()) {
            generateOrders.setEnabled(false);
            showGroupedByProduct.setEnabled(false);
            showGroupedByProductAndDate.setEnabled(false);
        } else {
            generateOrders.setEnabled(true);
            showGroupedByProduct.setEnabled(true);
            showGroupedByProductAndDate.setEnabled(true);
        }

        generateOrders.requestUpdate(true);
        showGroupedByProduct.setMessage("masterOrders.masterOrderPositionsList.window.ribbon.grouping.showGroupedByProduct.description");
        showGroupedByProduct.requestUpdate(true);
        showGroupedByProductAndDate.setMessage("masterOrders.masterOrderPositionsList.window.ribbon.grouping.showGroupedByProductAndDate.description");
        showGroupedByProductAndDate.requestUpdate(true);
        boolean isEnabled = (selectedEntities.size() == 1);
        createOrderRibbonActionItem.setEnabled(isEnabled);

        createOrderRibbonActionItem.requestUpdate(true);
        window.requestRibbonRender();
        createOrderRibbonActionItem.setMessage("masterOrders.masterOrder.masterOrdersPosition.lessEntitiesSelectedThanAllowed");
    }

    public void groupByProduct(final ViewDefinitionState view) throws JSONException {
        String positionsIds = view.getJsonContext().get("window.mainTab.positionsIds").toString();
        Set<Long> ids = Lists.newArrayList(positionsIds.split(",")).stream().map(Long::valueOf).collect(Collectors.toSet());
        List<Entity> positionDtos = dataDefinitionService.get(MasterOrdersConstants.PLUGIN_IDENTIFIER, MasterOrdersConstants.MODEL_MASTER_ORDER_POSITION_DTO)
                .find().add(SearchRestrictions.in("id", ids)).list().getEntities();
        Map<Integer, Entity> positions = Maps.newHashMap();
        for (Entity position : positionDtos) {
            Integer productId = position.getIntegerField(MasterOrderPositionDtoFields.PRODUCT_ID);
            if (positions.containsKey(productId)) {
                Entity existingPosition = positions.get(productId);
                sumPositionsValues(position, existingPosition);
            } else {
                positions.put(productId, position);
            }
        }
        GridComponent grid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);
        grid.setEntities(positions.values().stream().sorted(Comparator
                .comparing(e -> e.getStringField(MasterOrderPositionDtoFields.PRODUCT_NUMBER))).collect(Collectors.toList()));
    }

    public void groupByProductAndDate(final ViewDefinitionState view) throws JSONException {
        String positionsIds = view.getJsonContext().get("window.mainTab.positionsIds").toString();
        Set<Long> ids = Lists.newArrayList(positionsIds.split(",")).stream().map(Long::valueOf).collect(Collectors.toSet());
        List<Entity> positionDtos = dataDefinitionService.get(MasterOrdersConstants.PLUGIN_IDENTIFIER, MasterOrdersConstants.MODEL_MASTER_ORDER_POSITION_DTO)
                .find().add(SearchRestrictions.in("id", ids)).list().getEntities();
        Map<Integer, Map<Date, Entity>> positions = Maps.newHashMap();
        Map<Integer, Entity> positionsWithoutDeadline = Maps.newHashMap();
        for (Entity position : positionDtos) {
            Integer productId = position.getIntegerField(MasterOrderPositionDtoFields.PRODUCT_ID);
            Date deadline = position.getDateField(MasterOrderPositionDtoFields.DEADLINE);
            groupPosition(positions, positionsWithoutDeadline, position, productId, deadline);
        }
        List<Entity> summedPositions = positions.values().stream().map(Map::values).flatMap(Collection::stream).collect(Collectors.toList());
        summedPositions.addAll(positionsWithoutDeadline.values());
        GridComponent grid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);
        grid.setEntities(summedPositions.stream().sorted(Comparator
                .comparing((Entity e) -> e.getStringField(MasterOrderPositionDtoFields.PRODUCT_NUMBER))
                .thenComparing((Entity e) -> e.getDateField(MasterOrderPositionDtoFields.DEADLINE), nullsFirst(naturalOrder()))).collect(Collectors.toList()));
    }

    private void groupPosition(Map<Integer, Map<Date, Entity>> positions, Map<Integer, Entity> positionsWithoutDeadline, Entity position, Integer productId, Date deadline) {
        if (deadline != null) {
            if (positions.containsKey(productId)) {
                Map<Date, Entity> productPositions = positions.get(productId);
                if (productPositions.containsKey(deadline)) {
                    Entity existingPosition = productPositions.get(deadline);
                    sumPositionsValues(position, existingPosition);
                } else {
                    productPositions.put(deadline, position);
                }
            } else {
                Map<Date, Entity> productPositions = Maps.newHashMap();
                productPositions.put(deadline, position);
                positions.put(productId, productPositions);
            }
        } else {
            if (positionsWithoutDeadline.containsKey(productId)) {
                Entity existingPosition = positionsWithoutDeadline.get(productId);
                sumPositionsValues(position, existingPosition);
            } else {
                positionsWithoutDeadline.put(productId, position);
            }
        }
    }

    private void sumPositionsValues(Entity position, Entity existingPosition) {
        existingPosition.setField(MasterOrderPositionDtoFields.MASTER_ORDER_QUANTITY,
                existingPosition.getDecimalField(MasterOrderPositionDtoFields.MASTER_ORDER_QUANTITY)
                        .add(position.getDecimalField(MasterOrderPositionDtoFields.MASTER_ORDER_QUANTITY)));
        existingPosition.setField(MasterOrderPositionDtoFields.CUMULATED_MASTER_ORDER_QUANTITY,
                existingPosition.getDecimalField(MasterOrderPositionDtoFields.CUMULATED_MASTER_ORDER_QUANTITY)
                        .add(position.getDecimalField(MasterOrderPositionDtoFields.CUMULATED_MASTER_ORDER_QUANTITY)));
        existingPosition.setField(MasterOrderPositionDtoFields.PRODUCED_ORDER_QUANTITY,
                existingPosition.getDecimalField(MasterOrderPositionDtoFields.PRODUCED_ORDER_QUANTITY)
                        .add(position.getDecimalField(MasterOrderPositionDtoFields.PRODUCED_ORDER_QUANTITY)));
        existingPosition.setField(MasterOrderPositionDtoFields.QUANTITY_TAKEN_FROM_WAREHOUSE,
                Optional.ofNullable(existingPosition.getDecimalField(MasterOrderPositionDtoFields.QUANTITY_TAKEN_FROM_WAREHOUSE)).orElse(BigDecimal.ZERO)
                        .add(Optional.ofNullable(position.getDecimalField(MasterOrderPositionDtoFields.QUANTITY_TAKEN_FROM_WAREHOUSE)).orElse(BigDecimal.ZERO)));
        existingPosition.setField(MasterOrderPositionDtoFields.QUANTITY_REMAINING_TO_ORDER,
                existingPosition.getDecimalField(MasterOrderPositionDtoFields.QUANTITY_REMAINING_TO_ORDER)
                        .add(position.getDecimalField(MasterOrderPositionDtoFields.QUANTITY_REMAINING_TO_ORDER)));
        existingPosition.setField(MasterOrderPositionDtoFields.LEFT_TO_RELEASE,
                existingPosition.getDecimalField(MasterOrderPositionDtoFields.LEFT_TO_RELEASE)
                        .add(position.getDecimalField(MasterOrderPositionDtoFields.LEFT_TO_RELEASE)));
    }
}
