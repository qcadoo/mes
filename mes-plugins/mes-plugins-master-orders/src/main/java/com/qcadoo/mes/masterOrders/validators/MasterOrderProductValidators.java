/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
package com.qcadoo.mes.masterOrders.validators;

import static com.qcadoo.mes.masterOrders.constants.MasterOrderFields.PRODUCT;
import static com.qcadoo.mes.masterOrders.constants.MasterOrderProductFields.MASTER_ORDER;
import static com.qcadoo.model.api.search.SearchProjections.alias;
import static com.qcadoo.model.api.search.SearchProjections.id;
import static com.qcadoo.model.api.search.SearchRestrictions.and;
import static com.qcadoo.model.api.search.SearchRestrictions.belongsTo;
import static com.qcadoo.model.api.search.SearchRestrictions.not;

import java.util.Collection;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.masterOrders.constants.MasterOrderFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderProductFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderType;
import com.qcadoo.mes.masterOrders.util.MasterOrderOrdersDataProvider;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class MasterOrderProductValidators {

    @Autowired
    private MasterOrderOrdersDataProvider masterOrderOrdersDataProvider;

    public boolean onValidate(final DataDefinition masterOrderDD, final Entity masterOrder) {
        return checkIfEntityAlreadyExistsForProductAndMasterOrder(masterOrderDD, masterOrder);
    }

    private boolean checkIfEntityAlreadyExistsForProductAndMasterOrder(final DataDefinition masterOrderProductDD,
            final Entity masterOrderProduct) {
        SearchCriteriaBuilder searchCriteriaBuilder = masterOrderProductDD.find()
                .add(belongsTo(MASTER_ORDER, masterOrderProduct.getBelongsToField(MASTER_ORDER)))
                .add(belongsTo(PRODUCT, masterOrderProduct.getBelongsToField(PRODUCT)));
        // It decreases unnecessary mapping overhead
        searchCriteriaBuilder.setProjection(alias(id(), "id"));

        Long masterOrderId = masterOrderProduct.getId();
        if (masterOrderId != null) {
            searchCriteriaBuilder.add(SearchRestrictions.ne("id", masterOrderId));
        }
        if (searchCriteriaBuilder.setMaxResults(1).uniqueResult() == null) {
            return true;
        }

        masterOrderProduct.addError(masterOrderProductDD.getField(PRODUCT),
                "masterOrders.masterOrderProduct.alreadyExistsForProductAndMasterOrder");
        return false;
    }

    public boolean checkIfCanChangeTechnology(final DataDefinition dataDefinition, final FieldDefinition fieldDefinition,
            final Entity masterOrderProduct, final Object fieldOldValue, final Object fieldNewValue) {
        if (masterOrderProduct.getId() == null) {
            return true;
        }
        Entity masterOrder = masterOrderProduct.getBelongsToField(MasterOrderProductFields.MASTER_ORDER);
        if (MasterOrderType.of(masterOrder) != MasterOrderType.MANY_PRODUCTS) {
            return true;
        }
        Entity oldTechnology = (Entity) fieldOldValue;
        Entity newTechnology = (Entity) fieldNewValue;
        if (isNullOrDoesNotChange(oldTechnology, newTechnology)) {
            return true;
        }

        Entity product = masterOrderProduct.getBelongsToField(MasterOrderFields.PRODUCT);
        Collection<String> unsupportedOrderNumbers = masterOrderOrdersDataProvider.findBelongingOrderNumbers(masterOrder,
                and(belongsTo(OrderFields.PRODUCT, product), not(belongsTo(OrderFields.TECHNOLOGY_PROTOTYPE, newTechnology))));
        if (unsupportedOrderNumbers.isEmpty()) {
            return true;
        }

        masterOrderProduct.addError(fieldDefinition, "masterOrders.masterOrder.technology.wrongTechnology",
                StringUtils.join(unsupportedOrderNumbers, ", "));
        return false;
    }

    public boolean checkIfCanChangeProduct(final DataDefinition dataDefinition, final FieldDefinition fieldDefinition,
            final Entity masterOrderProduct, final Object fieldOldValue, final Object fieldNewValue) {
        if (masterOrderProduct.getId() == null) {
            return true;
        }
        Entity masterOrder = masterOrderProduct.getBelongsToField(MasterOrderProductFields.MASTER_ORDER);
        if (MasterOrderType.of(masterOrder) != MasterOrderType.MANY_PRODUCTS) {
            return true;
        }
        Entity oldProductValue = (Entity) fieldOldValue;
        Entity newProductValue = (Entity) fieldNewValue;
        if (wasOrIsNullOrDoesNotChange(oldProductValue, newProductValue)) {
            return true;
        }

        Collection<String> unsupportedOrderNumbers = masterOrderOrdersDataProvider.findBelongingOrderNumbers(masterOrder,
                belongsTo(OrderFields.PRODUCT, oldProductValue));
        if (unsupportedOrderNumbers.isEmpty()) {
            return true;
        }

        masterOrderProduct.addError(fieldDefinition, "masterOrders.masterOrder.product.wrongProduct",
                StringUtils.join(unsupportedOrderNumbers, ", "));
        return false;
    }

    private boolean wasOrIsNullOrDoesNotChange(final Entity oldValue, final Entity newValue) {
        return oldValue == null || isNullOrDoesNotChange(oldValue, newValue);
    }

    private boolean isNullOrDoesNotChange(final Entity oldValue, final Entity newValue) {
        return newValue == null || (oldValue != null && ObjectUtils.equals(oldValue.getId(), newValue.getId()));
    }

}
