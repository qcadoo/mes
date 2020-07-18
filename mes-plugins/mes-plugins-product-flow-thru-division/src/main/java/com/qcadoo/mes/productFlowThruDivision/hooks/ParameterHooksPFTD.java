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
package com.qcadoo.mes.productFlowThruDivision.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.productFlowThruDivision.constants.*;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class ParameterHooksPFTD {

    public void onCreate(final DataDefinition parameterDD, final Entity parameter) {
        setIfNull(parameter, ParameterFieldsPFTD.IGNORE_MISSING_COMPONENTS, true);
        setIfNull(parameter, ParameterFieldsPFTD.MOMENT_OF_VALIDATION, MomentOfValidation.ORDER_ACCEPTANCE.getStrValue());
        setIfNull(parameter, ParameterFieldsPFTD.WAREHOUSE_ISSUE_PRODUCTS_SOURCE,
                WarehouseIssueProductsSource.ORDER.getStrValue());
        setIfNull(parameter, ParameterFieldsPFTD.DRAWN_DOCUMENTS, DrawnDocuments.TRANSFER.getStrValue());
        setIfNull(parameter, ParameterFieldsPFTD.DOCUMENTS_STATUS, DocumentsStatus.ACCEPTED.getStrValue());
        setIfNull(parameter, ParameterFieldsPFTD.PRODUCTS_TO_ISSUE, ProductsToIssue.ALL_INPUT_PRODUCTS.getStrValue());

    }

    private void setIfNull(Entity entity, String name, Object value) {
        if (entity.getField(name) == null) {
            entity.setField(name, value);
        }
    }

    public void onSave(final DataDefinition dataDefinition, final Entity parameter) {
        if (parameter.getBooleanField(ParameterFieldsPFTD.GENERATE_WAREHOUSE_ISSUES_TO_ORDERS) == true) {
            addErrorIfNull(parameter, dataDefinition, ParameterFieldsPFTD.ISSUE_LOCATION,
                    "basic.parameter.error.issueLocation.isRequired");
            addErrorIfNull(parameter, dataDefinition, ParameterFieldsPFTD.DAYS_BEFORE_ORDER_START,
                    "basic.parameter.error.daysBeforeOrderStart.isRequired");
        }
        addErrorIfNull(parameter, dataDefinition, ParameterFieldsPFTD.WAREHOUSE_ISSUE_PRODUCTS_SOURCE,
                "basic.parameter.error.warehouseIssueProductsSource.isRequired");
        addErrorIfNull(parameter, dataDefinition, ParameterFieldsPFTD.PRODUCTS_TO_ISSUE,
                "qcadooView.validate.field.error.missing");
        addErrorIfNull(parameter, dataDefinition, ParameterFieldsPFTD.DRAWN_DOCUMENTS,
                "basic.parameter.error.drawndocuments.isRequired");
        addErrorIfNull(parameter, dataDefinition, ParameterFieldsPFTD.DOCUMENTS_STATUS,
                "basic.parameter.error.documentsStatus.isRequired");
    }

    private void addErrorIfNull(Entity entity, DataDefinition dataDefinition, String fieldName, String error) {
        if (entity.getField(fieldName) == null) {
            entity.addError(dataDefinition.getField(fieldName), error);
        }
    }
}
