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
package com.qcadoo.mes.productFlowThruDivision.warehouseIssue.validators;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialFlowResources.service.ResourceStockService;
import com.qcadoo.mes.productFlowThruDivision.constants.ProductFlowThruDivisionConstants;
import com.qcadoo.mes.productFlowThruDivision.reservation.ReservationsServiceForProductsToIssue;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.WarehouseIssueParameterService;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.constans.ProductsToIssueFields;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.constans.WarehouseIssueFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class ProductToIssueValidators {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private WarehouseIssueParameterService warehouseIssueParameterService;

    @Autowired
    private ReservationsServiceForProductsToIssue reservationsServiceForProductsToIssue;

    @Autowired
    private ResourceStockService resourceStockService;

    public boolean validate(final DataDefinition dataDefinition, final Entity productToIssue) {
        Entity warehouseIssue = productToIssue.getBelongsToField(ProductsToIssueFields.WAREHOUSE_ISSUE);

        if (productToIssue.getBelongsToField(ProductsToIssueFields.LOCATION).getId()
                .equals(warehouseIssue.getBelongsToField(WarehouseIssueFields.PLACE_OF_ISSUE).getId())) {
            productToIssue.addError(dataDefinition.getField(ProductsToIssueFields.LOCATION),
                    "productFlowThruDivision.issue.locationFromAndToIsEquals");
            return false;
        }
        if (!warehouseIssueParameterService.issueForOrder()) {
            if (!checkUnique(productToIssue)) {
                return false;
            }
        }

        return validateAvailableQuantity(dataDefinition, productToIssue);
    }

    public boolean validateAvailableQuantity(final DataDefinition productToIssueDD, final Entity productToIssue) {
        if (reservationsServiceForProductsToIssue.reservationsEnabledForProductsToIssue()) {
            BigDecimal availableQuantity = getAvailableQuantity(productToIssueDD, productToIssue);
            BigDecimal quantity = productToIssue.getDecimalField(ProductsToIssueFields.DEMAND_QUANTITY);
            if (quantity != null && quantity.compareTo(availableQuantity) > 0) {
                productToIssue.addError(productToIssueDD.getField(ProductsToIssueFields.DEMAND_QUANTITY),
                        "productFlowThruDivision.productToIssue.notEnoughResources");
                return false;
            }
        }
        return true;
    }

    private BigDecimal getAvailableQuantity(final DataDefinition productToIssueDD, final Entity productToIssue) {
        BigDecimal oldQuantity = BigDecimal.ZERO;
        if (productToIssue.getId() != null) {
            Entity productToIssueFromDB = productToIssueDD.get(productToIssue.getId());
            oldQuantity = productToIssueFromDB.getDecimalField(ProductsToIssueFields.DEMAND_QUANTITY);
        }
        Entity location = productToIssue.getBelongsToField(ProductsToIssueFields.WAREHOUSE_ISSUE)
                .getBelongsToField(WarehouseIssueFields.PLACE_OF_ISSUE);
        return resourceStockService
                .getResourceStockAvailableQuantity(productToIssue.getBelongsToField(ProductsToIssueFields.PRODUCT), location)
                .add(oldQuantity);
    }

    private boolean checkUnique(final Entity productToIssue) {
        boolean unique = true;
        Optional<Entity> optional = findProductToIssue(productToIssue);
        if (optional.isPresent()) {
            Entity sc = optional.get();

            if (productToIssue.getId() == null) {
                productToIssue.addGlobalError("productFlowThruDivision.productToIssue.notUnique");
                unique = false;
            } else if (sc.getId().equals(productToIssue.getId())) {
                unique = true;
            } else {
                productToIssue.addGlobalError("productFlowThruDivision.productToIssue.notUnique");
                unique = false;
            }

        } else {
            unique = true;
        }
        return unique;
    }

    private Optional<Entity> findProductToIssue(final Entity productToIssue) {

        SearchCriteriaBuilder scb = getProductToIssueDD().find();
        scb.add(SearchRestrictions.belongsTo(ProductsToIssueFields.PRODUCT,
                productToIssue.getBelongsToField(ProductsToIssueFields.PRODUCT)));
        scb.add(SearchRestrictions.belongsTo(ProductsToIssueFields.LOCATION,
                productToIssue.getBelongsToField(ProductsToIssueFields.LOCATION)));
        scb.add(SearchRestrictions.belongsTo(ProductsToIssueFields.WAREHOUSE_ISSUE,
                productToIssue.getBelongsToField(ProductsToIssueFields.WAREHOUSE_ISSUE)));

        return Optional.ofNullable(scb.setMaxResults(1).uniqueResult());
    }

    private DataDefinition getProductToIssueDD() {
        return dataDefinitionService.get(ProductFlowThruDivisionConstants.PLUGIN_IDENTIFIER,
                ProductFlowThruDivisionConstants.MODEL_PRODUCTS_TO_ISSUE);
    }

}
