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
package com.qcadoo.mes.masterOrders.validators;

import com.qcadoo.mes.basic.constants.ProductFamilyElementType;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderProductFields;
import com.qcadoo.mes.masterOrders.util.MasterOrderOrdersDataProvider;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchProjections;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.plugin.api.PluginUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

import static com.qcadoo.model.api.search.SearchRestrictions.*;

@Service
public class MasterOrderProductValidators {

    @Autowired
    private MasterOrderOrdersDataProvider masterOrderOrdersDataProvider;

    public boolean onValidate(final DataDefinition masterOrderProductDD, final Entity masterOrderProduct) {
        boolean isValid = checkIfEntityAlreadyExistsForProductAndMasterOrder(masterOrderProductDD, masterOrderProduct);
        isValid = checkIfOrdersAssignedToMasterOrder(masterOrderProduct) && isValid;
        isValid = checkIfParticularProduct(masterOrderProduct) && isValid;
        isValid = checkIfCanChangeProduct(masterOrderProductDD, masterOrderProduct) && isValid;

        return isValid;
    }


    private boolean checkIfParticularProduct(final Entity masterOrderProduct) {
        Entity product = masterOrderProduct.getBelongsToField(MasterOrderProductFields.PRODUCT);
        if (ProductFamilyElementType.PRODUCTS_FAMILY.getStringValue().equals(product.getStringField(ProductFields.ENTITY_TYPE))) {
            masterOrderProduct.addGlobalError("orders.validate.global.error.product.differentProductsInDifferentSizes");
            return false;
        }
        return true;
    }

    private boolean checkIfEntityAlreadyExistsForProductAndMasterOrder(final DataDefinition masterOrderProductDD,
                                                                       final Entity masterOrderProduct) {
        if (PluginUtils.isEnabled("moldrew")) {
            return true;
        }
        SearchCriteriaBuilder searchCriteriaBuilder = masterOrderProductDD.find()
                .add(SearchRestrictions.belongsTo(MasterOrderProductFields.MASTER_ORDER,
                        masterOrderProduct.getBelongsToField(MasterOrderProductFields.MASTER_ORDER)))
                .add(SearchRestrictions.belongsTo(MasterOrderProductFields.PRODUCT,
                        masterOrderProduct.getBelongsToField(MasterOrderProductFields.PRODUCT)));

        // It decreases unnecessary mapping overhead
        searchCriteriaBuilder.setProjection(SearchProjections.alias(SearchProjections.id(), "id"));

        Long masterOrderProductId = masterOrderProduct.getId();

        if (masterOrderProductId != null) {
            searchCriteriaBuilder.add(SearchRestrictions.ne("id", masterOrderProductId));
        }

        if (searchCriteriaBuilder.setMaxResults(1).uniqueResult() == null) {
            return true;
        }

        masterOrderProduct.addError(masterOrderProductDD.getField(MasterOrderProductFields.PRODUCT),
                "masterOrders.masterOrderProduct.alreadyExistsForProductAndMasterOrder");

        return false;
    }

    private boolean checkIfOrdersAssignedToMasterOrder(final Entity masterOrderProduct) {
        Entity masterOrder = masterOrderProduct.getBelongsToField(MasterOrderProductFields.MASTER_ORDER);

        String query = "SELECT masterOrder.id AS masterOrderId, masterOrder.number AS masterOrderNumber, " +
                "(SELECT count(mproduct) FROM #masterOrders_masterOrderProduct mproduct WHERE mproduct.masterOrder.id = masterOrder.id) AS positions, " +
                "(SELECT count(morder) FROM #orders_order morder WHERE morder.masterOrder.id = masterOrder.id) AS orders  " +
                "FROM #masterOrders_masterOrder masterOrder " +
                "WHERE masterOrder.id = :oid";
        Entity masterOrderFromDB = masterOrder.getDataDefinition().find(query).setLong("oid", masterOrder.getId())
                .setMaxResults(1).uniqueResult();

        if (masterOrderFromDB.getLongField("orders") != 0l && masterOrderFromDB.getLongField("positions") == 0l) {
            masterOrderProduct.addGlobalError("masterOrders.masterOrderProduct.alreadyExistsOrdersAssignedToMasterOrder", false);

            return false;
        }

        return true;
    }

    public boolean checkIfCanChangeTechnology(final DataDefinition masterOrderProductDD,
                                              final FieldDefinition fieldDefinition,
                                              final Entity masterOrderProduct, final Object fieldOldValue,
                                              final Object fieldNewValue) {
        if (masterOrderProduct.getId() == null) {
            return true;
        }

        if (masterOrderProduct.getBooleanField("isUpdateTechnologiesOnPendingOrders")) {
            return true;
        }

        Entity masterOrder = masterOrderProduct.getBelongsToField(MasterOrderProductFields.MASTER_ORDER);

        Entity oldTechnology = (Entity) fieldOldValue;
        Entity newTechnology = (Entity) fieldNewValue;

        if (isNullOrDoesNotChange(oldTechnology, newTechnology)) {
            return true;
        }

        Entity product = masterOrderProduct.getBelongsToField(MasterOrderProductFields.PRODUCT);

        Collection<String> unsupportedOrderNumbers = masterOrderOrdersDataProvider.findBelongingOrderNumbers(masterOrder,
                and(belongsTo(OrderFields.PRODUCT, product), not(belongsTo(OrderFields.TECHNOLOGY, newTechnology))));

        if (unsupportedOrderNumbers.isEmpty()) {
            return true;
        }

        masterOrderProduct.addError(fieldDefinition, "masterOrders.masterOrder.technology.wrongTechnology",
                StringUtils.join(unsupportedOrderNumbers, ", "));

        return false;
    }

    private boolean checkIfCanChangeProduct(final DataDefinition masterOrderProductDD, final Entity masterOrderProduct) {
        if (masterOrderProduct.getId() == null) {
            return true;
        }

        Entity dbMasterOrderProduct = masterOrderProductDD.get(masterOrderProduct.getId());

        Entity masterOrder = masterOrderProduct.getBelongsToField(MasterOrderProductFields.MASTER_ORDER);

        Entity oldProductValue = dbMasterOrderProduct.getBelongsToField(MasterOrderProductFields.PRODUCT);
        Entity newProductValue = masterOrderProduct.getBelongsToField(MasterOrderProductFields.PRODUCT);
        String oldVendorInfo = dbMasterOrderProduct.getStringField(MasterOrderProductFields.VENDOR_INFO);
        String newVendorInfo = masterOrderProduct.getStringField(MasterOrderProductFields.VENDOR_INFO);

        if (wasOrIsNullOrDoesNotChange(oldProductValue, newProductValue) && (oldVendorInfo == null && newVendorInfo == null
                || StringUtils.equals(oldVendorInfo, newVendorInfo))) {
            return true;
        }

        SearchCriterion searchCriterion;
        if (oldVendorInfo != null) {
            searchCriterion = and(SearchRestrictions.belongsTo(OrderFields.PRODUCT, oldProductValue), SearchRestrictions.eq(OrderFields.VENDOR_INFO, oldVendorInfo));
        } else {
            searchCriterion = and(SearchRestrictions.belongsTo(OrderFields.PRODUCT, oldProductValue), SearchRestrictions.isNull(OrderFields.VENDOR_INFO));
        }

        Collection<String> unsupportedOrderNumbers = masterOrderOrdersDataProvider.findBelongingOrderNumbers(masterOrder,
                searchCriterion);
        if (unsupportedOrderNumbers.isEmpty()) {
            return true;
        }

        if (oldVendorInfo != null) {
            masterOrderProduct.addError(masterOrderProductDD.getField(MasterOrderProductFields.PRODUCT), "masterOrders.masterOrder.product.wrongProductOrVendorInfo",
                    StringUtils.join(unsupportedOrderNumbers, ", "));
        } else {
            masterOrderProduct.addError(masterOrderProductDD.getField(MasterOrderProductFields.PRODUCT), "masterOrders.masterOrder.product.wrongProduct",
                    StringUtils.join(unsupportedOrderNumbers, ", "));
        }

        return false;
    }

    private boolean wasOrIsNullOrDoesNotChange(final Entity oldValue, final Entity newValue) {
        return oldValue == null || isNullOrDoesNotChange(oldValue, newValue);
    }

    private boolean isNullOrDoesNotChange(final Entity oldValue, final Entity newValue) {
        return newValue == null || (oldValue != null && ObjectUtils.equals(oldValue.getId(), newValue.getId()));
    }

}
