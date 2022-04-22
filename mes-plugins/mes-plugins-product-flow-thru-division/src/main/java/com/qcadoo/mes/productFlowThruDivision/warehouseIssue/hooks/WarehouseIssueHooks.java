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
package com.qcadoo.mes.productFlowThruDivision.warehouseIssue.hooks;

import java.util.Date;
import java.util.Objects;

import com.qcadoo.mes.materialFlowResources.constants.DocumentFields;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.states.constants.WarehouseIssueStringValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.productFlowThruDivision.constants.ProductFlowThruDivisionConstants;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.WarehouseIssueGenerator;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.WarehouseIssueParameterService;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.constans.WarehouseIssueFields;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.states.constants.WarehouseIssueState;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.states.constants.WarehouseIssueStateChangeDescriber;
import com.qcadoo.mes.productionLines.constants.ProductionLineFields;
import com.qcadoo.mes.states.service.StateChangeEntityBuilder;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

@Service
public class WarehouseIssueHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private StateChangeEntityBuilder stateChangeEntityBuilder;

    @Autowired
    private WarehouseIssueStateChangeDescriber describer;

    @Autowired
    private WarehouseIssueParameterService warehouseIssueParameterService;

    @Autowired
    private WarehouseIssueGenerator warehouseIssueGenerator;

    public void onCreate(final DataDefinition warehouseIssueDD, final Entity warehouseIssue) {
        setInitialState(warehouseIssue);
    }

    void setInitialState(final Entity warehouseIssue) {
        stateChangeEntityBuilder.buildInitial(describer, warehouseIssue, WarehouseIssueState.DRAFT);
    }

    public void onSave(final DataDefinition warehouseIssueDD, final Entity warehouseIssue) {
        if (Objects.isNull(warehouseIssue.getId())) {
            warehouseIssue.setField(WarehouseIssueFields.DATE_OF_CREATION, new Date());
        }
        if (Objects.isNull(warehouseIssue.getField(WarehouseIssueFields.NUMBER))) {
            warehouseIssue.setField(WarehouseIssueFields.NUMBER, warehouseIssueGenerator.setNumberFromSequence());
        }
        if (warehouseIssueParameterService.issueForOrder()) {
            fillOrderFields(warehouseIssue);
        }
    }

    public boolean onDelete(final DataDefinition warehouseIssueDD, final Entity warehouseIssue) {
        if (WarehouseIssueStringValues.IN_PROGRESS.equals(warehouseIssue.getStringField(WarehouseIssueFields.STATE))) {
            warehouseIssue.addGlobalError("productFlowThruDivision.warehouseIssue.error.issueInProgress");
            return false;
        }
        return true;
    }

    private void fillOrderFields(Entity warehouseIssue) {
        Entity order = getOrderDD().get(warehouseIssue.getBelongsToField(WarehouseIssueFields.ORDER).getId());

        if (Objects.isNull(warehouseIssue.getDateField(WarehouseIssueFields.ORDER_START_DATE))) {
            warehouseIssue.setField(WarehouseIssueFields.ORDER_START_DATE, order.getDateField(OrderFields.START_DATE));
        }
        if (Objects.isNull(warehouseIssue.getStringField(WarehouseIssueFields.ORDER_PRODUCTION_LINE_NUMBER))) {
            Entity orderProductionLine = order.getBelongsToField(OrderFields.PRODUCTION_LINE);

            warehouseIssue.setField(WarehouseIssueFields.ORDER_PRODUCTION_LINE_NUMBER,
                    orderProductionLine.getStringField(ProductionLineFields.NUMBER));
        }
    }

    public void onCopy(final DataDefinition warehouseIssueDD, final Entity warehouseIssue) {
        setInitialState(warehouseIssue);
    }

    private DataDefinition getOrderDD() {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER);
    }

    public DataDefinition getWarehouseIssueDD() {
        return dataDefinitionService.get(ProductFlowThruDivisionConstants.PLUGIN_IDENTIFIER,
                ProductFlowThruDivisionConstants.MODEL_WAREHOUSE_ISSUE);
    }

}
