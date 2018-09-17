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

import com.qcadoo.mes.materialFlowResources.MaterialFlowResourcesService;
import com.qcadoo.mes.productFlowThruDivision.service.WarehouseIssueService;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.WarehouseIssueParameterService;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.constans.IssueFields;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.constans.ProductsToIssueFields;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.constans.WarehouseIssueFields;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class IssueValidators {

    @Autowired
    private MaterialFlowResourcesService materialFlowResourcesService;

    @Autowired
    private WarehouseIssueParameterService warehouseIssueParameterService;

    @Autowired
    private WarehouseIssueService warehouseIssueService;

    @Autowired
    private NumberService numberService;

    public boolean validate(final DataDefinition dataDefinition, final Entity issue) {

        Entity warehouseIssue = issue.getBelongsToField(IssueFields.WAREHOUSE_ISSUE);

        if (issue.getBelongsToField(IssueFields.LOCATION).getId()
                .equals(warehouseIssue.getBelongsToField(WarehouseIssueFields.PLACE_OF_ISSUE).getId())) {
            issue.addError(dataDefinition.getField(IssueFields.LOCATION),
                    "productFlowThruDivision.issue.locationFromAndToIsEquals");
        }
        if (!issue.isValid()) {
            return false;
        }
        return true;
    }

    public boolean onDelete(final DataDefinition issueDD, final Entity issue) {
        if (issue.getBooleanField(IssueFields.ISSUED)) {
            issue.addGlobalError("productFlowThruDivision.issue.canNotRemoveIssuedEntity");
            return false;
        }
        return true;
    }

    public void onSave(final DataDefinition productToIssueDD, final Entity issue) {
        if(issue.getField(IssueFields.ISSUED) == null){
            issue.setField(IssueFields.ISSUED, false);
        }
    }

    public boolean checkIfCanIssueQuantity(final List<Entity> issues) {
        List<Entity> filteredIssues = issues.stream().filter(i -> !i.getBooleanField(IssueFields.ISSUED))
                .collect(Collectors.toList());
        for (Entity issue : filteredIssues) {
            if (!checkIfCanIssueQuantity(issue)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkIfCanIssueQuantity(final Entity issue) {
        if (warehouseIssueParameterService.isIssuedQuantityUpToNeed()) {
            if (warehouseIssueParameterService.issueForOrder()) {
                return checkIfCanIssueQuantityForOrder(issue);
            } else {
                return checkIfCanIssueQuantityForManual(issue);

            }
        }
        return true;
    }

    private boolean checkIfCanIssueQuantityForManual(final Entity issue) {
        BigDecimal alreadyIssued = BigDecimal.ZERO;
        Optional<Entity> optionalValue = warehouseIssueService.findProductForIssue(issue);
        if (optionalValue.isPresent()) {
            alreadyIssued = alreadyIssued.add(
                    BigDecimalUtils.convertNullToZero(optionalValue.get().getDecimalField(ProductsToIssueFields.ISSUE_QUANTITY)),
                    numberService.getMathContext());
            BigDecimal toIssue = BigDecimalUtils.convertNullToZero(issue.getDecimalField(IssueFields.ISSUE_QUANTITY));
            BigDecimal summaryIssue = alreadyIssued.add(toIssue, numberService.getMathContext());
            if (summaryIssue.compareTo(BigDecimalUtils.convertNullToZero(optionalValue.get().getDecimalField(
                    ProductsToIssueFields.DEMAND_QUANTITY))) == 1) {
                return false;

            }
        }
        return true;

    }

    private boolean checkIfCanIssueQuantityForOrder(final Entity issue) {
        Entity wi = issue.getBelongsToField(IssueFields.WAREHOUSE_ISSUE);
        Entity product = issue.getBelongsToField(IssueFields.PRODUCT);
        BigDecimal alreadyIssued = BigDecimal.ZERO;
        alreadyIssued = warehouseIssueService.getIssuedQuantityForProductAndOrder(wi, product);
        BigDecimal toIssue = BigDecimalUtils.convertNullToZero(issue.getDecimalField(IssueFields.ISSUE_QUANTITY));
        BigDecimal summaryIssue = alreadyIssued.add(toIssue, numberService.getMathContext());
        if (summaryIssue.compareTo(BigDecimalUtils.convertNullToZero(issue.getDecimalField(IssueFields.DEMAND_QUANTITY))) == 1) {
            return false;
        }
        return true;
    }
}
