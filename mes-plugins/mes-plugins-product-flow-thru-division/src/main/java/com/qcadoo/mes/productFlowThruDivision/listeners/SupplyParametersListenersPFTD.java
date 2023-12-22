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
package com.qcadoo.mes.productFlowThruDivision.listeners;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.productFlowThruDivision.constants.ParameterFieldsPFTD;
import com.qcadoo.mes.productFlowThruDivision.constants.WarehouseIssueProductsSource;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class SupplyParametersListenersPFTD {

    public void onGenerateWarehouseIssuesToOrdersChange(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        CheckBoxComponent generateWarehouseIssues = (CheckBoxComponent) componentState;
        toggleDaysBeforeOrderStart(generateWarehouseIssues.isChecked(), view);
        toggleIssueLocation(generateWarehouseIssues.isChecked(), view);
        toggleAutomaticReleaseAfterGeneration(generateWarehouseIssues.isChecked(), view);
    }

    private void toggleIssueLocation(final boolean lock, final ViewDefinitionState view) {
        FieldComponent issueLocation = (FieldComponent) view.getComponentByReference(ParameterFieldsPFTD.ISSUE_LOCATION);
        if (lock) {
            issueLocation.setEnabled(true);
        } else {
            issueLocation.setEnabled(false);
            issueLocation.setFieldValue(null);
        }
        issueLocation.requestComponentUpdateState();
    }

    private void toggleAutomaticReleaseAfterGeneration(boolean checked, ViewDefinitionState view) {
        CheckBoxComponent automaticReleaseAfterGeneration = (CheckBoxComponent) view.getComponentByReference("automaticReleaseAfterGeneration");
        if(checked) {
            automaticReleaseAfterGeneration.setEnabled(true);
        } else {
            automaticReleaseAfterGeneration.setChecked(Boolean.FALSE);
            automaticReleaseAfterGeneration.setEnabled(false);
        }
        automaticReleaseAfterGeneration.requestComponentUpdateState();
    }

    private void toggleDaysBeforeOrderStart(final boolean lock, final ViewDefinitionState view) {
        FieldComponent daysBeforeOrderStart = (FieldComponent) view
                .getComponentByReference(ParameterFieldsPFTD.DAYS_BEFORE_ORDER_START);
        if (lock) {
            daysBeforeOrderStart.setEnabled(true);
        } else {
            daysBeforeOrderStart.setEnabled(false);
            daysBeforeOrderStart.setFieldValue(null);
        }
        daysBeforeOrderStart.requestComponentUpdateState();
    }

    public void toggleGenerateIssuesTo(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        String value = (String) componentState.getFieldValue();
        if (StringUtils.isNotEmpty(value)) {
            WarehouseIssueProductsSource warehouseIssueProductsSource = WarehouseIssueProductsSource.parseString(value);
                       CheckBoxComponent generateWarehouseIssuesToOrders = (CheckBoxComponent) view
                    .getComponentByReference(ParameterFieldsPFTD.GENERATE_WAREHOUSE_ISSUES_TO_ORDERS);
            if (WarehouseIssueProductsSource.MANUAL.equals(warehouseIssueProductsSource)) {
                uncheckAndDisable(generateWarehouseIssuesToOrders);
            } else if (WarehouseIssueProductsSource.ORDER.equals(warehouseIssueProductsSource)) {
                generateWarehouseIssuesToOrders.setEnabled(true);
                onGenerateWarehouseIssuesToOrdersChange(view, generateWarehouseIssuesToOrders, args);
            }
        }
    }

    private void uncheckAndDisable(CheckBoxComponent checkbox) {
        checkbox.setChecked(false);
        checkbox.setEnabled(false);
    }
}
