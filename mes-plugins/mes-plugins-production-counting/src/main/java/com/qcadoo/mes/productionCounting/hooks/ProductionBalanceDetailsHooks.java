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
package com.qcadoo.mes.productionCounting.hooks;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.OrderService;
import com.qcadoo.mes.productionCounting.ProductionBalanceService;
import com.qcadoo.mes.productionCounting.ProductionCountingService;
import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.constants.ProductionBalanceFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class ProductionBalanceDetailsHooks {

    private static final String L_FORM = "form";

    private static final String L_INPUT_PRODUCTS_GRID = "inputProductsGrid";

    private static final String L_OUTPUT_PRODUCTS_GRID = "outputProductsGrid";

    private static final String L_TIME_GRID_LAYOUT = "workGridLayout";

    private static final String L_LABOR_TIME_BORDER_LAYOUT = "laborTimeBorderLayout";

    private static final String L_MACHINE_TIME_BORDER_LAYOUT = "machineTimeBorderLayout";

    private static final String L_OPERATIONS_TIME_GRID = "operationsTimeGrid";

    private static final String L_OPERATIONS_PIECEWORK_GRID = "operationsPieceworkGrid";

    private static final List<String> L_FIELDS_AND_CHECKBOXES = Arrays.asList(ProductionBalanceFields.ORDER,
            ProductionBalanceFields.NAME, ProductionBalanceFields.DESCRIPTION, ProductionBalanceFields.PRINT_OPERATION_NORMS,
            ProductionBalanceFields.CALCULATE_OPERATION_COST_MODE, ProductionBalanceFields.INCLUDE_TPZ,
            ProductionBalanceFields.INCLUDE_ADDITIONAL_TIME);

    private static final List<String> L_FIELDS = L_FIELDS_AND_CHECKBOXES.subList(0, L_FIELDS_AND_CHECKBOXES.size() - 2);

    private static final List<String> L_GRIDS = Arrays.asList(L_INPUT_PRODUCTS_GRID, L_OUTPUT_PRODUCTS_GRID);

    private static final List<String> L_GRIDS_AND_LAYOUTS = Arrays.asList(L_INPUT_PRODUCTS_GRID, L_OUTPUT_PRODUCTS_GRID,
            L_TIME_GRID_LAYOUT, L_MACHINE_TIME_BORDER_LAYOUT, L_LABOR_TIME_BORDER_LAYOUT, L_OPERATIONS_TIME_GRID,
            L_OPERATIONS_PIECEWORK_GRID);

    @Autowired
    private ProductionCountingService productionCountingService;

    @Autowired
    private ProductionBalanceService productionBalanceService;

    @Autowired
    private OrderService orderService;

    public void changeFieldsAndGridsVisibility(final ViewDefinitionState view) {
        FormComponent productionBalanceForm = (FormComponent) view.getComponentByReference(L_FORM);

        FieldComponent generatedField = (FieldComponent) view.getComponentByReference(ProductionBalanceFields.GENERATED);
        FieldComponent orderLookup = (FieldComponent) view.getComponentByReference(ProductionBalanceFields.ORDER);
        FieldComponent calculateOperationCostModeField = (FieldComponent) view
                .getComponentByReference(ProductionBalanceFields.CALCULATE_OPERATION_COST_MODE);

        String generated = (String) generatedField.getFieldValue();

        if ((productionBalanceForm.getEntityId() == null) || "0".equals(generated)) {
            productionCountingService.setComponentsVisibility(view, L_GRIDS_AND_LAYOUTS, false, false);
        }

        Long orderId = (Long) orderLookup.getFieldValue();

        if (orderId == null) {
            productionCountingService.setComponentsVisibility(view, L_GRIDS_AND_LAYOUTS, false, false);

            return;
        }

        Entity order = orderService.getOrder(orderId);

        if ("1".equals(generated) && (calculateOperationCostModeField != null) && (order != null)) {
            if (order.getBooleanField(OrderFieldsPC.REGISTER_QUANTITY_IN_PRODUCT)) {
                view.getComponentByReference(L_INPUT_PRODUCTS_GRID).setVisible(true);
            }

            if (order.getBooleanField(OrderFieldsPC.REGISTER_QUANTITY_OUT_PRODUCT)) {
                view.getComponentByReference(L_OUTPUT_PRODUCTS_GRID).setVisible(true);
            }

            String calculateOperationCostMode = (String) calculateOperationCostModeField.getFieldValue();

            if (productionCountingService.isCalculateOperationCostModeHourly(calculateOperationCostMode)
                    && order.getBooleanField(OrderFieldsPC.REGISTER_PRODUCTION_TIME)) {
                view.getComponentByReference(L_TIME_GRID_LAYOUT).setVisible(true);

                if (productionCountingService.isTypeOfProductionRecordingForEach(order
                        .getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING))) {
                    view.getComponentByReference(L_MACHINE_TIME_BORDER_LAYOUT).setVisible(true);
                    view.getComponentByReference(L_LABOR_TIME_BORDER_LAYOUT).setVisible(true);
                    view.getComponentByReference(L_OPERATIONS_TIME_GRID).setVisible(true);
                } else if (productionCountingService.isTypeOfProductionRecordingCumulated(order
                        .getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING))) {
                    view.getComponentByReference(L_MACHINE_TIME_BORDER_LAYOUT).setVisible(true);
                    view.getComponentByReference(L_LABOR_TIME_BORDER_LAYOUT).setVisible(true);
                    view.getComponentByReference(L_OPERATIONS_TIME_GRID).setVisible(false);
                }

                view.getComponentByReference(L_OPERATIONS_PIECEWORK_GRID).setVisible(false);
            } else if (productionCountingService.isCalculateOperationCostModePiecework(calculateOperationCostMode)
                    && order.getBooleanField(OrderFieldsPC.REGISTER_PIECEWORK)) {
                view.getComponentByReference(L_TIME_GRID_LAYOUT).setVisible(false);

                view.getComponentByReference(L_MACHINE_TIME_BORDER_LAYOUT).setVisible(false);
                view.getComponentByReference(L_LABOR_TIME_BORDER_LAYOUT).setVisible(false);
                view.getComponentByReference(L_OPERATIONS_TIME_GRID).setVisible(false);

                view.getComponentByReference(L_OPERATIONS_PIECEWORK_GRID).setVisible(true);
            }
        }
    }

    public void disableCheckboxes(final ViewDefinitionState view) {
        productionBalanceService.disableCheckboxes(view);
    }

    public void disableFieldsAndGridsWhenGenerated(final ViewDefinitionState view) {
        FieldComponent generatedField = (FieldComponent) view.getComponentByReference(ProductionBalanceFields.GENERATED);

        String generated = (String) generatedField.getFieldValue();

        if ((generated != null) && "1".equals(generated)) {
            productionCountingService.setComponentsState(view, L_FIELDS_AND_CHECKBOXES, false, true);
            productionCountingService.setComponentsState(view, L_GRIDS, false, false);
        } else {
            productionCountingService.setComponentsState(view, L_FIELDS, true, true);
            productionCountingService.setComponentsState(view, L_GRIDS, true, false);
        }
    }

}
