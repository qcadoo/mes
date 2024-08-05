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
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.UserFieldsB;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.productFlowThruDivision.constants.ParameterFieldsPFTD;
import com.qcadoo.mes.productFlowThruDivision.constants.WarehouseIssueProductsSource;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.WarehouseIssueParameterService;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.constans.CollectionProducts;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.constans.IssueFields;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.constans.WarehouseIssueFields;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.states.constants.WarehouseIssueStringValues;
import com.qcadoo.mes.productionLines.constants.ProductionLineFields;
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
import com.qcadoo.view.constants.QcadooViewConstants;
import com.qcadoo.view.constants.RowStyle;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Set;

@Service
public class WarehouseIssueDetailHooks {

    private static final String L_PRODUCTS_TO_ISSUES = "productsToIssues";

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
        Long orderId = (Long) view.getComponentByReference(WarehouseIssueFields.ORDER).getFieldValue();
        Entity order = null;

        if (Objects.nonNull(orderId)) {
            order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(orderId);
        }
        setCriteriaModifierParameters(view, order);
        fillWorkerWhoIssued(view);
        fillOrderFields(view, order);
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

        if (view.isViewAfterRedirect() && Objects.isNull(form.getEntityId())) {
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

        if (state.equals(WarehouseIssueStringValues.DISCARD) || state.equals(WarehouseIssueStringValues.COMPLETED)) {
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
            String state = (String) view.getComponentByReference(WarehouseIssueFields.STATE).getFieldValue();

            if (StringUtils.isNotEmpty(state) && !state.equals(WarehouseIssueStringValues.DRAFT)) {
                grid.setEditable(false);
            } else if (StringUtils.isNotEmpty(state) && state.equals(WarehouseIssueStringValues.DRAFT)) {
                grid.setEditable(true);
            }
        }
    }

    public Set<String> fillRowStyles(final Entity issue) {
        final Set<String> rowStyles = Sets.newHashSet();

        if (Objects.nonNull(issue.getDecimalField(IssueFields.ISSUE_QUANTITY))) {
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
        LookupComponent operationLookup = (LookupComponent) view
                .getComponentByReference(WarehouseIssueFields.TECHNOLOGY_OPERATION_COMPONENT);
        LookupComponent divisionLookup = (LookupComponent) view.getComponentByReference(WarehouseIssueFields.DIVISION);

        String state = (String) view.getComponentByReference(WarehouseIssueFields.STATE).getFieldValue();

        if (state.equals(WarehouseIssueStringValues.DRAFT)) {
            form.setFormEnabled(true);
        }

        if (collectionProductsField.getFieldValue().equals(CollectionProducts.ON_ORDER.getStringValue())) {
            operationLookup.setVisible(false);
            operationLookup.setRequired(false);
            divisionLookup.setVisible(false);
            divisionLookup.setRequired(false);
        } else if (collectionProductsField.getFieldValue().equals(CollectionProducts.ON_DIVISION.getStringValue())) {
            operationLookup.setVisible(false);
            operationLookup.setRequired(false);
            divisionLookup.setVisible(true);
            divisionLookup.setRequired(true);
        } else if (collectionProductsField.getFieldValue().equals(CollectionProducts.ON_OPERATION.getStringValue())) {
            if (Objects.nonNull(view.getComponentByReference(WarehouseIssueFields.ORDER).getFieldValue())) {
                operationLookup.setVisible(true);
                operationLookup.setRequired(true);
            }
            divisionLookup.setVisible(false);
            divisionLookup.setRequired(false);
        }

        divisionLookup.requestComponentUpdateState();
        operationLookup.requestComponentUpdateState();

        if (state.equals(WarehouseIssueStringValues.IN_PROGRESS) || state.equals(WarehouseIssueStringValues.DISCARD)
                || state.equals(WarehouseIssueStringValues.COMPLETED)) {
            form.setFormEnabled(false);

            GridComponent grid = (GridComponent) view.getComponentByReference("issues");

            grid.setEnabled(state.equals(WarehouseIssueStringValues.IN_PROGRESS));
        }
    }

    private void setCriteriaModifierParameters(final ViewDefinitionState view, final Entity order) {
        LookupComponent technologyOperationComponentLookup = (LookupComponent) view
                .getComponentByReference(WarehouseIssueFields.TECHNOLOGY_OPERATION_COMPONENT);
        LookupComponent divisionLookup = (LookupComponent) view.getComponentByReference(WarehouseIssueFields.DIVISION);

        if (Objects.nonNull(order)) {
            Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

            if (Objects.nonNull(technology)) {
                FilterValueHolder filterValueHolder = technologyOperationComponentLookup.getFilterValue();
                filterValueHolder.put(OrderFields.TECHNOLOGY, technology.getId());

                technologyOperationComponentLookup.setFilterValue(filterValueHolder);

                FilterValueHolder divisionFilterValueHolder = divisionLookup.getFilterValue();
                divisionFilterValueHolder.put(OrderFields.TECHNOLOGY, technology.getId());

                divisionLookup.setFilterValue(divisionFilterValueHolder);
            }
        }
    }

    private void fillWorkerWhoIssued(final ViewDefinitionState view) {
        FieldComponent workerWhoIssued = (FieldComponent) view.getComponentByReference(WarehouseIssueFields.WORKER_WHO_ISSUED);

        if (Objects.isNull(workerWhoIssued.getFieldValue())) {
            Entity currentUserStaff = userService.getCurrentUserEntity().getBelongsToField(UserFieldsB.STAFF);

            if (Objects.nonNull(currentUserStaff)) {
                workerWhoIssued.setFieldValue(currentUserStaff.getId());
            }
        }
    }

    private void fillOrderFields(final ViewDefinitionState view, final Entity order) {
        FieldComponent orderStartDateComponent = (FieldComponent) view
                .getComponentByReference(WarehouseIssueFields.ORDER_START_DATE);
        FieldComponent orderProductionLineComponent = (FieldComponent) view
                .getComponentByReference(WarehouseIssueFields.ORDER_PRODUCTION_LINE_NUMBER);

        if (order != null) {
            String orderStartDate = DateUtils.toDateTimeString(order.getDateField(OrderFields.START_DATE));
            orderStartDateComponent.setFieldValue(orderStartDate);
            orderStartDateComponent.requestComponentUpdateState();

            Entity orderProductionLine = order.getBelongsToField(OrderFields.PRODUCTION_LINE);
            orderProductionLineComponent.setFieldValue(orderProductionLine.getStringField(ProductionLineFields.NUMBER));
            orderProductionLineComponent.requestComponentUpdateState();
        } else {
            orderStartDateComponent.setFieldValue(null);
            orderStartDateComponent.requestComponentUpdateState();
            orderProductionLineComponent.setFieldValue(null);
            orderProductionLineComponent.requestComponentUpdateState();
        }
    }

    private void hideOrderFields(final ViewDefinitionState view) {
        String productsSource = parameterService.getParameter()
                .getStringField(ParameterFieldsPFTD.WAREHOUSE_ISSUE_PRODUCTS_SOURCE);

        if (Objects.nonNull(productsSource)) {
            WarehouseIssueProductsSource warehouseIssueProductsSource = WarehouseIssueProductsSource.parseString(productsSource);

            if (WarehouseIssueProductsSource.MANUAL.equals(warehouseIssueProductsSource)) {
                hideComponents(view, WarehouseIssueFields.ORDER, WarehouseIssueFields.ORDER_START_DATE,
                        WarehouseIssueFields.ORDER_PRODUCTION_LINE_NUMBER, WarehouseIssueFields.COLLECTION_PRODUCTS);
            }
        }
    }

    private void hideComponents(final ViewDefinitionState view, final String... names) {
        for (String name : names) {
            ComponentState component = view.getComponentByReference(name);
            component.setVisible(false);
        }
    }

}
