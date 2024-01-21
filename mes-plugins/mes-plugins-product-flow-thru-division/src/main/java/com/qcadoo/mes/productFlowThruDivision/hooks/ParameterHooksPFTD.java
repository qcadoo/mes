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

import com.qcadoo.mes.materialFlow.constants.ParameterFieldsMF;
import com.qcadoo.mes.materialFlow.constants.WhatToShowOnDashboard;
import com.qcadoo.mes.productFlowThruDivision.constants.*;
import com.qcadoo.mes.technologies.constants.ParameterFieldsT;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.springframework.stereotype.Service;

import java.util.Objects;

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

    private void setIfNull(final Entity entity, final String name, final Object value) {
        if (Objects.isNull(entity.getField(name))) {
            entity.setField(name, value);
        }
    }

    public void onSave(final DataDefinition parameterDD, final Entity parameter) {
        if (parameter.getBooleanField(ParameterFieldsPFTD.GENERATE_WAREHOUSE_ISSUES_TO_ORDERS)) {
            addErrorIfNull(parameterDD, parameter, ParameterFieldsPFTD.ISSUE_LOCATION,
                    "basic.parameter.error.issueLocation.isRequired");
            addErrorIfNull(parameterDD, parameter, ParameterFieldsPFTD.DAYS_BEFORE_ORDER_START,
                    "basic.parameter.error.daysBeforeOrderStart.isRequired");
        }

        addErrorIfNull(parameterDD, parameter, ParameterFieldsPFTD.WAREHOUSE_ISSUE_PRODUCTS_SOURCE,
                "basic.parameter.error.warehouseIssueProductsSource.isRequired");
        addErrorIfNull(parameterDD, parameter, ParameterFieldsPFTD.PRODUCTS_TO_ISSUE,
                "qcadooView.validate.field.error.missing");
        addErrorIfNull(parameterDD, parameter, ParameterFieldsPFTD.DRAWN_DOCUMENTS,
                "basic.parameter.error.drawndocuments.isRequired");
        addErrorIfNull(parameterDD, parameter, ParameterFieldsPFTD.MOMENT_OF_VALIDATION,
                "basic.parameter.error.momentofvalidation.isRequired");
        addErrorIfNull(parameterDD, parameter, ParameterFieldsPFTD.DOCUMENTS_STATUS,
                "basic.parameter.error.documentsStatus.isRequired");

        clearOperationAndLocations(parameter);
    }

    private void addErrorIfNull(final DataDefinition dataDefinition, final Entity entity, final String fieldName, final String error) {
        if (Objects.isNull(entity.getField(fieldName))) {
            entity.addError(dataDefinition.getField(fieldName), error);
        }
    }

    private void clearOperationAndLocations(final Entity parameter) {
        String whatToShowOnDashboard = parameter.getStringField(ParameterFieldsMF.WHAT_TO_SHOW_ON_DASHBOARD);

        if (!WhatToShowOnDashboard.ORDERS.getStringValue().equals(whatToShowOnDashboard)) {
            parameter.setField(ParameterFieldsT.DASHBOARD_OPERATION, null);
        }
    }

}
