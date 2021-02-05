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

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.WarehouseIssueParameterService;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.constans.CollectionProducts;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.constans.WarehouseIssueFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class WarehouseIssueValidators {

    private static final String L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_MISSING = "qcadooView.validate.field.error.missing";

    @Autowired
    private WarehouseIssueParameterService warehouseIssueParameterService;

    public boolean checkRequiredFields(final DataDefinition dataDefinition, final Entity warehouseIssue) {

        if (warehouseIssueParameterService.issueForOrder()
                && warehouseIssue.getBelongsToField(WarehouseIssueFields.ORDER) == null) {
            warehouseIssue.addError(dataDefinition.getField(WarehouseIssueFields.ORDER),
                    L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_MISSING);
        }

        if (warehouseIssueParameterService.issueForOrder()
                && StringUtils.isEmpty(warehouseIssue.getStringField(WarehouseIssueFields.PRODUCTS_TO_ISSUE_MODE))) {
            warehouseIssue.addError(dataDefinition.getField(WarehouseIssueFields.PRODUCTS_TO_ISSUE_MODE),
                    L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_MISSING);
        }

        String collectionProducts = warehouseIssue.getStringField(WarehouseIssueFields.COLLECTION_PRODUCTS);

        if (collectionProducts.equals(CollectionProducts.ON_DIVISION.getStringValue())) {
            if (warehouseIssue.getBelongsToField(WarehouseIssueFields.DIVISION) == null) {
                warehouseIssue.addError(dataDefinition.getField(WarehouseIssueFields.DIVISION),
                        L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_MISSING);
            }
        } else if (collectionProducts.equals(CollectionProducts.ON_OPERATION.getStringValue())) {
            if (warehouseIssue.getBelongsToField(WarehouseIssueFields.TECHNOLOGY_OPERATION_COMPONENT) == null) {
                warehouseIssue.addError(dataDefinition.getField(WarehouseIssueFields.TECHNOLOGY_OPERATION_COMPONENT),
                        L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_MISSING);
            }
        }
        if (!warehouseIssue.isValid()) {
            return false;
        }
        return true;
    }

}
