/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited Project: Qcadoo Framework Version: 1.4
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Affero General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 * ***************************************************************************
 */
package com.qcadoo.mes.productFlowThruDivision.warehouseIssue.hooks;

import com.google.common.collect.Sets;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.UserFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.productFlowThruDivision.constants.ParameterFieldsPFTD;
import com.qcadoo.mes.productFlowThruDivision.constants.ProductFlowThruDivisionConstants;
import com.qcadoo.mes.productFlowThruDivision.constants.WarehouseIssueProductsSource;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.WarehouseIssueParameterService;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.constans.CollectionProducts;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.constans.IssueFields;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.constans.WarehouseIssueFields;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.states.constants.WarehouseIssueStringValues;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.security.api.UserService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.*;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.api.utils.NumberGeneratorService;
import com.qcadoo.view.constants.QcadooViewConstants;
import com.qcadoo.view.constants.RowStyle;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Set;

@Service
public class WarehouseIssueDetailHooks {

    private static final String L_PRODUCTS_TO_ISSUES = "productsToIssues";

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private UserService userService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private WarehouseIssueParameterService warehouseIssueParameterService;

    public void onBeforeRender(final ViewDefinitionState view) {
        setViewState(view);
        setCriteriaModifierParameters(view);
        generateIssueNumber(view);
        // fillOrderListDtoLookup(view);
        fillWorkerWhoIssued(view);
        hideOrderFields(view);
        disableProductsToIssueGrid(view);
        updateRibbonState(view);
        processProductsToIssueMode(view);
    }

    private void processProductsToIssueMode(final ViewDefinitionState view) {
        FieldComponent component = (FieldComponent) view.getComponentByReference(WarehouseIssueFields.PRODUCTS_TO_ISSUE_MODE);
        if (!warehouseIssueParameterService.issueForOrder()) {
            component.setVisible(false);
            component.requestComponentUpdateState();
            return;
        }
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        if (view.isViewAfterRedirect() && form.getEntityId() == null) {
            component.setFieldValue(warehouseIssueParameterService.getProductsToIssue().getStrValue());
        }
    }

    private void updateRibbonState(final ViewDefinitionState view) {
        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
        Ribbon ribbon = window.getRibbon();
        RibbonGroup group = ribbon.getGroupByName("issue");
        RibbonActionItem productsToIssue = group.getItemByName("productsToIssue");
        RibbonGroup groupCopyProductsToIssue = ribbon.getGroupByName("copyissue");
        RibbonActionItem copyProductsToIssue = groupCopyProductsToIssue.getItemByName("copyProductsToIssue");

        if (!warehouseIssueParameterService.issueForOrder()) {
            productsToIssue.setEnabled(false);
            productsToIssue.setMessage("productFlowThruDivision.issue.activeWhenIssueForOrder");
            productsToIssue.requestUpdate(true);
            window.requestRibbonRender();
        }
        String state = (String) view.getComponentByReference(WarehouseIssueFields.STATE).getFieldValue();
        if (state.equals(WarehouseIssueStringValues.DISCARD ) || state.equals(WarehouseIssueStringValues.COMPLETED)) {
            copyProductsToIssue.setEnabled(false);
            copyProductsToIssue.requestUpdate(true);
            window.requestRibbonRender();
        }
    }

    private void disableProductsToIssueGrid(final ViewDefinitionState view) {
        GridComponent grid = (GridComponent) view.getComponentByReference(L_PRODUCTS_TO_ISSUES);

        if (warehouseIssueParameterService.issueForOrder()) {
            grid.setEditable(false);
        } else {
            FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
            String state = (String) view.getComponentByReference(WarehouseIssueFields.STATE).getFieldValue();
            if (StringUtils.isNotEmpty(state) && !state.equals(WarehouseIssueStringValues.DRAFT)) {
                grid.setEditable(false);
            } else if(StringUtils.isNotEmpty(state) && state.equals(WarehouseIssueStringValues.DRAFT)) {
                grid.setEditable(true);
            }
        }
    }

    public Set<String> fillRowStyles(final Entity issue) {
        final Set<String> rowStyles = Sets.newHashSet();
        if (issue.getDecimalField(IssueFields.ISSUE_QUANTITY) != null) {
            if (BigDecimalUtils.convertNullToZero(issue.getDecimalField(IssueFields.ISSUE_QUANTITY))
                    .compareTo(BigDecimal.ZERO) == 0) {
                rowStyles.add(RowStyle.RED_BACKGROUND);
            }
        }
        return rowStyles;
    }

    public void setViewState(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        FieldComponent collectionProductsField = (FieldComponent) view
                .getComponentByReference(WarehouseIssueFields.COLLECTION_PRODUCTS);

        LookupComponent operation = (LookupComponent) view
                .getComponentByReference(WarehouseIssueFields.TECHNOLOGY_OPERATION_COMPONENT);
        LookupComponent division = (LookupComponent) view.getComponentByReference(WarehouseIssueFields.DIVISION);

        String state = (String) view.getComponentByReference(WarehouseIssueFields.STATE).getFieldValue();
        if (state.equals(WarehouseIssueStringValues.DRAFT)) {
            form.setFormEnabled(true);
        }

        if (collectionProductsField.getFieldValue().equals(CollectionProducts.ON_ORDER.getStringValue())) {
            operation.setVisible(false);
            operation.setRequired(false);
            division.setVisible(false);
            division.setRequired(false);
            division.requestComponentUpdateState();
            operation.requestComponentUpdateState();
        } else if (collectionProductsField.getFieldValue().equals(CollectionProducts.ON_DIVISION.getStringValue())) {
            operation.setVisible(false);
            operation.setRequired(false);
            division.setVisible(true);
            division.setRequired(true);
            division.requestComponentUpdateState();
            operation.requestComponentUpdateState();
        } else if (collectionProductsField.getFieldValue().equals(CollectionProducts.ON_OPERATION.getStringValue())) {
            if (view.getComponentByReference(WarehouseIssueFields.ORDER).getFieldValue() != null) {
                operation.setVisible(true);
                operation.setRequired(true);
            }

            division.setVisible(false);
            division.setRequired(false);
            division.requestComponentUpdateState();
            operation.requestComponentUpdateState();
        }

        if (state.equals(WarehouseIssueStringValues.IN_PROGRESS) || state.equals(WarehouseIssueStringValues.DISCARD)
                || state.equals(WarehouseIssueStringValues.COMPLETED)) {

            form.setFormEnabled(false);
            GridComponent grid = (GridComponent) view.getComponentByReference("issues");
            if (state.equals(WarehouseIssueStringValues.IN_PROGRESS)) {
                grid.setEnabled(true);
            } else {
                grid.setEnabled(false);
            }
        }

    }

    public void setCriteriaModifierParameters(final ViewDefinitionState view) {
        Long orderId = (Long) view.getComponentByReference(WarehouseIssueFields.ORDER).getFieldValue();
        Entity order = null;
        if (orderId != null) {
            order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(orderId);
        }
        setCriteriaModifierParameters(view, order);
    }

    public void setCriteriaModifierParameters(final ViewDefinitionState view, final Entity order) {
        LookupComponent technologyOperationComponentLookup = (LookupComponent) view
                .getComponentByReference(WarehouseIssueFields.TECHNOLOGY_OPERATION_COMPONENT);

        LookupComponent divisionLookup = (LookupComponent) view.getComponentByReference(WarehouseIssueFields.DIVISION);

        if (order != null) {
            Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

            if (technology != null) {
                FilterValueHolder filterValueHolder = technologyOperationComponentLookup.getFilterValue();
                filterValueHolder.put(OrderFields.TECHNOLOGY, technology.getId());

                technologyOperationComponentLookup.setFilterValue(filterValueHolder);

                FilterValueHolder divisionFilterValueHolder = divisionLookup.getFilterValue();
                divisionFilterValueHolder.put(OrderFields.TECHNOLOGY, technology.getId());

                divisionLookup.setFilterValue(divisionFilterValueHolder);
            }

        }
    }

    public void generateIssueNumber(final ViewDefinitionState view) {
        numberGeneratorService.generateAndInsertNumber(view, ProductFlowThruDivisionConstants.PLUGIN_IDENTIFIER,
                ProductFlowThruDivisionConstants.MODEL_WAREHOUSE_ISSUE, QcadooViewConstants.L_FORM, WarehouseIssueFields.NUMBER);
    }

    private void fillWorkerWhoIssued(final ViewDefinitionState view) {
        FieldComponent workerWhoIssued = (FieldComponent) view.getComponentByReference(WarehouseIssueFields.WORKER_WHO_ISSUED);
        if (null == workerWhoIssued.getFieldValue()) {
            Entity currentUserStaff = userService.getCurrentUserEntity().getBelongsToField(UserFields.STAFF);
            if (currentUserStaff != null) {
                workerWhoIssued.setFieldValue(currentUserStaff.getId());
            }
        }
    }

    private void hideOrderFields(ViewDefinitionState view) {
        String productsSource = parameterService.getParameter().getStringField(
                ParameterFieldsPFTD.WAREHOUSE_ISSUE_PRODUCTS_SOURCE);

        if (productsSource != null) {
            WarehouseIssueProductsSource warehouseIssueProductsSource = WarehouseIssueProductsSource.parseString(productsSource);
            if (WarehouseIssueProductsSource.MANUAL.equals(warehouseIssueProductsSource)) {
                hideComponents(view, WarehouseIssueFields.ORDER, WarehouseIssueFields.ORDER_START_DATE,
                        WarehouseIssueFields.ORDER_PRODUCTION_LINE_NUMBER, WarehouseIssueFields.COLLECTION_PRODUCTS);
            }
        }
    }

    private void hideComponents(ViewDefinitionState view, String... names) {
        for (String name : names) {
            ComponentState component = view.getComponentByReference(name);
            component.setVisible(false);
        }
    }
}
