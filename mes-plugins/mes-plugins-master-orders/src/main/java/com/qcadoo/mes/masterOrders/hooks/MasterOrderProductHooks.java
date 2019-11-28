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

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.masterOrders.constants.MasterOrderFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderProductFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderState;
import com.qcadoo.mes.masterOrders.constants.MasterOrderType;
import com.qcadoo.mes.masterOrders.constants.OrderFieldsMO;
import com.qcadoo.mes.masterOrders.util.MasterOrderOrdersDataProvider;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.DictionaryService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.constants.DictionaryItemFields;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MasterOrderProductHooks {

    private static final String L_MASTER_ORDER_POSITION_STATUS = "masterOrderPositionStatus";

    @Autowired
    private MasterOrderOrdersDataProvider masterOrderOrdersDataProvider;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private DictionaryService dictionaryService;

    @Autowired
    private ParameterService parameterService;

    public void onSave(final DataDefinition masterOrderProductDD, final Entity masterOrderProduct) {

        setMasterOrderPositionStatus(masterOrderProduct);

        if (Objects.nonNull(masterOrderProduct.getBelongsToField(MasterOrderProductFields.TECHNOLOGY))) {
            List<Entity> entities = dataDefinitionService
                    .get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER)
                    .find()
                    .add(SearchRestrictions.belongsTo(OrderFieldsMO.MASTER_ORDER,
                            masterOrderProduct.getBelongsToField(MasterOrderProductFields.MASTER_ORDER)))
                    .add(SearchRestrictions.belongsTo(OrderFields.PRODUCT,
                            masterOrderProduct.getBelongsToField(MasterOrderProductFields.PRODUCT))).list().getEntities();
            entities = entities
                    .stream()
                    .filter(o -> Objects.nonNull(o.getBelongsToField(OrderFields.TECHNOLOGY))
                            && !o.getBelongsToField(OrderFields.TECHNOLOGY).getId()
                                    .equals(masterOrderProduct.getBelongsToField(MasterOrderProductFields.TECHNOLOGY).getId()))
                    .collect(Collectors.toList());
            if (!entities.isEmpty()) {
                masterOrderProduct.addGlobalMessage(
                        "masterOrders.masterOrderProduct.info.needToChangeTechnologyForExistingProductionOrders", false, false);
            }
        }

        masterOrderProduct.setField(MasterOrderProductFields.QUANTITY_TAKEN_FROM_WAREHOUSE, BigDecimalUtils
                .convertNullToZero(masterOrderProduct.getDecimalField(MasterOrderProductFields.QUANTITY_TAKEN_FROM_WAREHOUSE)));
        Entity masterOrder = masterOrderProduct.getBelongsToField(MasterOrderProductFields.MASTER_ORDER);
        if (MasterOrderState.NEW.getStringValue().equals(masterOrder.getStringField(MasterOrderFields.STATE))
                && masterOrderProduct.getDecimalField(MasterOrderProductFields.QUANTITY_TAKEN_FROM_WAREHOUSE).compareTo(
                        BigDecimal.ZERO) > 0) {
            masterOrder.setField(MasterOrderFields.STATE, MasterOrderState.IN_EXECUTION.getStringValue());
            masterOrder = masterOrder.getDataDefinition().save(masterOrder);
        }
        if (parameterService.getParameter().getBooleanField("completeMasterOrderAfterOrderingPositions")
                && !MasterOrderState.COMPLETED.getStringValue().equals(masterOrder.getStringField(MasterOrderFields.STATE))
                && MasterOrderPositionStatus.ORDERED.getText().equals(
                        masterOrderProduct.getStringField(MasterOrderProductFields.MASTER_ORDER_POSITION_STATUS))
                && isAllOrdered(masterOrderProduct, masterOrder)) {
            masterOrder.setField(MasterOrderFields.STATE, MasterOrderState.COMPLETED.getStringValue());
            masterOrder = masterOrder.getDataDefinition().save(masterOrder);
        }
    }

    private boolean isAllOrdered(final Entity masterOrderProduct, final Entity masterOrder) {
        List<Entity> newPositions = masterOrder
                .getHasManyField(MasterOrderFields.MASTER_ORDER_PRODUCTS)
                .stream()
                .filter(mop -> MasterOrderPositionStatus.NEW.getText().equals(
                        mop.getStringField(MasterOrderProductFields.MASTER_ORDER_POSITION_STATUS)))
                .filter(mop -> !mop.getId().equals(masterOrderProduct.getId())).collect(Collectors.toList());
        return newPositions.isEmpty();
    }

    private void setMasterOrderPositionStatus(final Entity masterOrderProduct) {
        if (Objects.isNull(masterOrderProduct.getId())) {
            Entity item = dictionaryService.getItemEntityByTechnicalCode(L_MASTER_ORDER_POSITION_STATUS,
                    MasterOrderPositionStatus.NEW.getStringValue());
            if (Objects.nonNull(item)) {
                masterOrderProduct.setField(MasterOrderProductFields.MASTER_ORDER_POSITION_STATUS,
                        item.getStringField(DictionaryItemFields.NAME));
            }
        }
    }

    public boolean onDelete(final DataDefinition masterOrderProductDD, final Entity masterOrderProduct) {
        return checkAssignedOrder(masterOrderProduct);
    }

    private boolean checkAssignedOrder(final Entity masterOrderProduct) {
        if (masterOrderProduct.getId() == null) {
            return true;
        }
        Entity masterOrder = masterOrderProduct.getBelongsToField(MasterOrderProductFields.MASTER_ORDER);
        if (MasterOrderType.of(masterOrder) != MasterOrderType.MANY_PRODUCTS) {
            return true;
        }
        Entity product = masterOrderProduct.getBelongsToField(MasterOrderProductFields.PRODUCT);
        long numOfBelongingOrdersMatchingProduct = masterOrderOrdersDataProvider.countBelongingOrders(masterOrder,
                SearchRestrictions.belongsTo(OrderFields.PRODUCT, product));

        if (numOfBelongingOrdersMatchingProduct > 0) {
            masterOrderProduct.addGlobalError("masterOrders.masterOrderProduct.delete.existsAssignedOrder");
            return false;
        }
        return true;
    }
}
