/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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
package com.qcadoo.mes.productionCounting.internal;

import static com.qcadoo.mes.basic.constants.BasicConstants.MODEL_PRODUCT;
import static com.qcadoo.mes.orders.constants.OrderFields.NAME;
import static com.qcadoo.mes.orders.constants.OrdersConstants.FIELD_NUMBER;
import static com.qcadoo.mes.productionCounting.internal.constants.CalculateOperationCostsMode.HOURLY;
import static com.qcadoo.mes.productionCounting.internal.constants.CalculateOperationCostsMode.PIECEWORK;
import static com.qcadoo.mes.productionCounting.internal.constants.OrderFieldsPC.REGISTER_PIECEWORK;
import static com.qcadoo.mes.productionCounting.internal.constants.OrderFieldsPC.REGISTER_PRODUCTION_TIME;
import static com.qcadoo.mes.productionCounting.internal.constants.OrderFieldsPC.REGISTER_QUANTITY_IN_PRODUCT;
import static com.qcadoo.mes.productionCounting.internal.constants.OrderFieldsPC.REGISTER_QUANTITY_OUT_PRODUCT;
import static com.qcadoo.mes.productionCounting.internal.constants.OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.CALCULATE_OPERATION_COST_MODE;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.DESCRIPTION;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.GENERATED;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.INCLUDE_ADDITIONAL_TIME;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.INCLUDE_TPZ;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.ORDER;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.PRINT_OPERATION_NORMS;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.PRODUCT;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.RECORDS_NUMBER;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionRecordFields.STATE;
import static com.qcadoo.mes.productionCounting.internal.constants.TypeOfProductionRecording.CUMULATED;
import static com.qcadoo.mes.productionCounting.internal.constants.TypeOfProductionRecording.FOR_EACH;
import static com.qcadoo.mes.productionCounting.states.constants.ProductionRecordState.ACCEPTED;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class ProductionBalanceViewService {

    private static final String L_INPUT_PRODUCTS_GRID = "inputProductsGrid";

    private static final String L_OUTPUT_PRODUCTS_GRID = "outputProductsGrid";

    private static final String L_TIME_GRID_LAYOUT = "workGridLayout";

    private static final String L_LABOR_TIME_BORDER_LAYOUT = "laborTimeBorderLayout";

    private static final String L_MACHINE_TIME_BORDER_LAYOUT = "machineTimeBorderLayout";

    private static final String L_OPERATIONS_TIME_GRID = "operationsTimeGrid";

    private static final String L_OPERATIONS_PIECEWORK_GRID = "operationsPieceworkGrid";

    private static final String L_EMPTY_NUMBER = "";

    private static final List<String> FIELDS_AND_CHECKBOXES = Arrays.asList(ORDER, NAME, DESCRIPTION, PRINT_OPERATION_NORMS,
            CALCULATE_OPERATION_COST_MODE, INCLUDE_TPZ, INCLUDE_ADDITIONAL_TIME);

    private static final List<String> FIELDS = FIELDS_AND_CHECKBOXES.subList(0, FIELDS_AND_CHECKBOXES.size() - 2);

    private static final List<String> GRIDS = Arrays.asList(L_INPUT_PRODUCTS_GRID, L_OUTPUT_PRODUCTS_GRID);

    private static final List<String> GRIDS_AND_LAYOUTS = Arrays.asList(L_INPUT_PRODUCTS_GRID, L_OUTPUT_PRODUCTS_GRID,
            L_TIME_GRID_LAYOUT, L_MACHINE_TIME_BORDER_LAYOUT, L_LABOR_TIME_BORDER_LAYOUT, L_OPERATIONS_TIME_GRID,
            L_OPERATIONS_PIECEWORK_GRID);

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ProductionBalanceService productionBalanceService;

    @Autowired
    private TranslationService translationService;

    public void changeFieldsAndGridsVisibility(final ViewDefinitionState viewDefinitionState) {
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");

        FieldComponent generated = (FieldComponent) viewDefinitionState.getComponentByReference(GENERATED);

        if ((form == null) || (form.getEntityId() == null) || (generated == null) || "0".equals(generated.getFieldValue())) {
            setComponentsVisibility(viewDefinitionState, GRIDS_AND_LAYOUTS, false, false);
        }

        FieldComponent calculateOperationCostMode = (FieldComponent) viewDefinitionState
                .getComponentByReference(CALCULATE_OPERATION_COST_MODE);

        FieldComponent orderLookup = (FieldComponent) viewDefinitionState.getComponentByReference(ORDER);

        Long orderId = (Long) orderLookup.getFieldValue();

        if (orderId == null) {
            setComponentsVisibility(viewDefinitionState, GRIDS_AND_LAYOUTS, false, false);

            return;
        }

        Entity order = productionBalanceService.getOrderFromDB(orderId);

        if ("1".equals(generated.getFieldValue()) && (calculateOperationCostMode != null) && (order != null)) {
            if (order.getBooleanField(REGISTER_QUANTITY_IN_PRODUCT)) {
                viewDefinitionState.getComponentByReference(L_INPUT_PRODUCTS_GRID).setVisible(true);
            }

            if (order.getBooleanField(REGISTER_QUANTITY_OUT_PRODUCT)) {
                viewDefinitionState.getComponentByReference(L_OUTPUT_PRODUCTS_GRID).setVisible(true);
            }

            if (HOURLY.getStringValue().equals(calculateOperationCostMode.getFieldValue())
                    && order.getBooleanField(REGISTER_PRODUCTION_TIME)) {
                viewDefinitionState.getComponentByReference(L_TIME_GRID_LAYOUT).setVisible(true);

                if (FOR_EACH.getStringValue().equals(order.getStringField(TYPE_OF_PRODUCTION_RECORDING))) {
                    viewDefinitionState.getComponentByReference(L_MACHINE_TIME_BORDER_LAYOUT).setVisible(true);
                    viewDefinitionState.getComponentByReference(L_LABOR_TIME_BORDER_LAYOUT).setVisible(true);
                    viewDefinitionState.getComponentByReference(L_OPERATIONS_TIME_GRID).setVisible(true);
                } else if (CUMULATED.getStringValue().equals(order.getStringField(TYPE_OF_PRODUCTION_RECORDING))) {
                    viewDefinitionState.getComponentByReference(L_MACHINE_TIME_BORDER_LAYOUT).setVisible(true);
                    viewDefinitionState.getComponentByReference(L_LABOR_TIME_BORDER_LAYOUT).setVisible(true);
                    viewDefinitionState.getComponentByReference(L_OPERATIONS_TIME_GRID).setVisible(false);
                }

                viewDefinitionState.getComponentByReference(L_OPERATIONS_PIECEWORK_GRID).setVisible(false);
            } else if (PIECEWORK.getStringValue().equals(calculateOperationCostMode.getFieldValue())
                    && order.getBooleanField(REGISTER_PIECEWORK)) {
                viewDefinitionState.getComponentByReference(L_TIME_GRID_LAYOUT).setVisible(false);

                viewDefinitionState.getComponentByReference(L_MACHINE_TIME_BORDER_LAYOUT).setVisible(false);
                viewDefinitionState.getComponentByReference(L_LABOR_TIME_BORDER_LAYOUT).setVisible(false);
                viewDefinitionState.getComponentByReference(L_OPERATIONS_TIME_GRID).setVisible(false);

                viewDefinitionState.getComponentByReference(L_OPERATIONS_PIECEWORK_GRID).setVisible(true);
            }
        }
    }

    public void disableCheckboxes(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        disableCheckboxes(viewDefinitionState);
    }

    public void disableCheckboxes(final ViewDefinitionState viewDefinitionState) {
        FieldComponent calculateOperationCostsMode = (FieldComponent) viewDefinitionState
                .getComponentByReference(CALCULATE_OPERATION_COST_MODE);

        FieldComponent includeTPZ = (FieldComponent) viewDefinitionState.getComponentByReference(INCLUDE_TPZ);
        FieldComponent includeAdditionalTime = (FieldComponent) viewDefinitionState
                .getComponentByReference(INCLUDE_ADDITIONAL_TIME);

        if (PIECEWORK.getStringValue().equals(calculateOperationCostsMode.getFieldValue())) {
            includeTPZ.setFieldValue(false);
            includeTPZ.setEnabled(false);
            includeTPZ.requestComponentUpdateState();

            includeAdditionalTime.setFieldValue(false);
            includeAdditionalTime.setEnabled(false);
            includeAdditionalTime.requestComponentUpdateState();
        } else {
            includeTPZ.setEnabled(true);
            includeTPZ.requestComponentUpdateState();

            includeAdditionalTime.setEnabled(true);
            includeAdditionalTime.requestComponentUpdateState();
        }
    }

    public void disableFieldsAndGridsWhenGenerated(final ViewDefinitionState viewDefinitionState) {
        FieldComponent generated = (FieldComponent) viewDefinitionState.getComponentByReference(GENERATED);

        if ((generated != null) && (generated.getFieldValue() != null) && "1".equals(generated.getFieldValue())) {
            setComponentsState(viewDefinitionState, FIELDS_AND_CHECKBOXES, false, true);
            setComponentsState(viewDefinitionState, GRIDS, false, false);
        } else {
            setComponentsState(viewDefinitionState, FIELDS, true, true);
            setComponentsState(viewDefinitionState, GRIDS, true, false);
        }
    }

    public void fillProductAndRecordsNumber(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        FieldComponent orderLookup = (FieldComponent) viewDefinitionState.getComponentByReference(ORDER);

        Long orderId = (Long) viewDefinitionState.getComponentByReference(ORDER).getFieldValue();

        if (orderId == null) {
            clearProductAndRecordsNumber(viewDefinitionState);

            return;
        }

        Entity order = productionBalanceService.getOrderFromDB(orderId);

        if (order == null) {
            clearProductAndRecordsNumber(viewDefinitionState);
            return;
        }

        if (productionBalanceService.isTypeOfProductionRecordingBasic(order)) {
            clearProductAndRecordsNumber(viewDefinitionState);

            orderLookup.addMessage("productionCounting.productionBalance.report.error.orderWithoutRecordingType",
                    ComponentState.MessageType.FAILURE);

            return;
        }

        fillProductAndRecordsNumber(viewDefinitionState, order);
    }

    private void fillProductAndRecordsNumber(final ViewDefinitionState viewDefinitionState, final Entity order) {
        FieldComponent productField = (FieldComponent) viewDefinitionState.getComponentByReference(PRODUCT);
        FieldComponent recordsNumberField = (FieldComponent) viewDefinitionState.getComponentByReference(RECORDS_NUMBER);

        Entity product = order.getBelongsToField(MODEL_PRODUCT);
        Integer recordsNumber = dataDefinitionService
                .get(ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_PRODUCTION_RECORD).find()
                .add(SearchRestrictions.eq(STATE, ACCEPTED.getStringValue())).add(SearchRestrictions.belongsTo(ORDER, order))
                .list().getEntities().size();

        productField.setFieldValue(product.getId());
        recordsNumberField.setFieldValue(recordsNumber);
    }

    private void clearProductAndRecordsNumber(final ViewDefinitionState viewDefinitionState) {
        FieldComponent productField = (FieldComponent) viewDefinitionState.getComponentByReference(PRODUCT);
        FieldComponent recordsNumberField = (FieldComponent) viewDefinitionState.getComponentByReference(RECORDS_NUMBER);

        productField.setFieldValue(null);
        recordsNumberField.setFieldValue(null);
    }

    public void setComponentsState(final ViewDefinitionState viewDefinitionState, final List<String> componentReferences,
            final boolean isEnabled, final boolean requestComponentUpdateState) {
        for (String componentReference : componentReferences) {
            viewDefinitionState.getComponentByReference(componentReference).setEnabled(isEnabled);

            if (requestComponentUpdateState) {
                ((FieldComponent) viewDefinitionState.getComponentByReference(componentReference)).requestComponentUpdateState();
            }
        }

    }

    public void setComponentsVisibility(final ViewDefinitionState viewDefinitionState, final List<String> componentReferences,
            final boolean isVisible, final boolean requestComponentUpdateState) {
        for (String componentReference : componentReferences) {
            viewDefinitionState.getComponentByReference(componentReference).setVisible(isVisible);

            if (requestComponentUpdateState) {
                ((FieldComponent) viewDefinitionState.getComponentByReference(componentReference)).requestComponentUpdateState();
            }
        }
    }

    public void setDefaultNameUsingOrder(final ViewDefinitionState view, final ComponentState component, final String[] args) {
        if (!(component instanceof FieldComponent)) {
            return;
        }

        LookupComponent orderLookup = (LookupComponent) view.getComponentByReference(ORDER);
        FieldComponent nameField = (FieldComponent) view.getComponentByReference(NAME);

        Entity order = orderLookup.getEntity();

        if (order == null) {
            return;
        }

        String name = (String) nameField.getFieldValue();
        Locale locale = component.getLocale();
        String defaultName = makeDefaultName(order, locale);

        if (StringUtils.isEmpty(name) || !defaultName.equals(name)) {
            nameField.setFieldValue(defaultName);
        }
    }

    public String makeDefaultName(final Entity orderEntity, final Locale locale) {

        String orderNumber = L_EMPTY_NUMBER;
        if (orderEntity != null) {
            orderNumber = orderEntity.getStringField(FIELD_NUMBER);
        }

        Calendar cal = Calendar.getInstance(locale);
        cal.setTime(new Date());

        return translationService.translate("productionCounting.productionBalance.name.default", locale, orderNumber,
                cal.get(Calendar.YEAR) + "." + (cal.get(Calendar.MONTH) + 1) + "." + cal.get(Calendar.DAY_OF_MONTH));
    }

}
