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
package com.qcadoo.mes.productFlowThruDivision.warehouseIssue.states.constants;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.productFlowThruDivision.constants.ProductFlowThruDivisionConstants;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.constans.WarehouseIssueFields;
import com.qcadoo.mes.states.AbstractStateChangeDescriber;
import com.qcadoo.mes.states.StateEnum;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;

@Service
public class WarehouseIssueStateChangeDescriber extends AbstractStateChangeDescriber {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    public DataDefinition getDataDefinition() {
        return dataDefinitionService
                .get(ProductFlowThruDivisionConstants.PLUGIN_IDENTIFIER, "warehouseIssueStateChange");
    }

    @Override
    public StateEnum parseStateEnum(final String stringValue) {
        return WarehouseIssueState.parseString(stringValue);
    }

    @Override
    public String getOwnerFieldName() {
        return WarehouseIssueStateChangeFields.WAREHOUSE_ISSUE;
    }

    @Override
    public DataDefinition getOwnerDataDefinition() {
        return dataDefinitionService
                .get(ProductFlowThruDivisionConstants.PLUGIN_IDENTIFIER, ProductFlowThruDivisionConstants.MODEL_WAREHOUSE_ISSUE);
    }

    @Override
    public String getOwnerStateChangesFieldName() {
        return WarehouseIssueFields.STATE_CHANGES;
    }

    ;

}
