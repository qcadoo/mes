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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.productFlowThruDivision.constants.ParameterFieldsPFTD;
import com.qcadoo.mes.productFlowThruDivision.constants.ProductFlowThruDivisionConstants;
import com.qcadoo.mes.productFlowThruDivision.listeners.SupplyParametersListenersPFTD;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.constans.WarehouseIssueFields;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.states.constants.WarehouseIssueState;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;

@Service
public class SupplyParametersHooksPFTD {

    @Autowired
    SupplyParametersListenersPFTD supplyParametersListenersPFTD;

    @Autowired
    DataDefinitionService dataDefinitionService;

    public void onBeforeRender(final ViewDefinitionState view) {
        ComponentState warehouseIssueProductsSource = view
                .getComponentByReference(ParameterFieldsPFTD.WAREHOUSE_ISSUE_PRODUCTS_SOURCE);
        supplyParametersListenersPFTD.toggleGenerateIssuesTo(view, warehouseIssueProductsSource, null);
        toggleStateReservation(view);
    }

    private void toggleStateReservation(ViewDefinitionState view) {
        CheckBoxComponent warehouseIssuesReserveStatesCheckbox = (CheckBoxComponent) view
                .getComponentByReference(ParameterFieldsPFTD.WAREHOUSE_ISSUES_RESERVE_STATES);
        boolean shouldBeEnabled = draftOrInProgressWarehouseIssuesDoesntExist();
        warehouseIssuesReserveStatesCheckbox.setEnabled(shouldBeEnabled);
        warehouseIssuesReserveStatesCheckbox.requestComponentUpdateState();
    }

    private boolean draftOrInProgressWarehouseIssuesDoesntExist() {
        DataDefinition dd = dataDefinitionService.get(ProductFlowThruDivisionConstants.PLUGIN_IDENTIFIER,
                ProductFlowThruDivisionConstants.MODEL_WAREHOUSE_ISSUE);
        SearchResult result = dd.find().add(SearchRestrictions.or(
                        SearchRestrictions.eq(WarehouseIssueFields.STATE, WarehouseIssueState.DRAFT.getStringValue()),
                        SearchRestrictions.eq(WarehouseIssueFields.STATE, WarehouseIssueState.IN_PROGRESS.getStringValue()))).list();
        return result.getTotalNumberOfEntities() == 0;
    }


}
