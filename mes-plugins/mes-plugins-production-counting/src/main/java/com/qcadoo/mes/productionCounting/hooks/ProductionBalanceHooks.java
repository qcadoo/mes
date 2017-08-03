/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
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
package com.qcadoo.mes.productionCounting.hooks;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionCounting.ProductionCountingService;
import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.constants.ProductionBalanceFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class ProductionBalanceHooks {

    private static final List<String> L_PRODUCTION_BALANCE_TIME_FIELDS = Arrays.asList(
            ProductionBalanceFields.PLANNED_MACHINE_TIME, ProductionBalanceFields.MACHINE_TIME,
            ProductionBalanceFields.MACHINE_TIME_BALANCE, ProductionBalanceFields.PLANNED_LABOR_TIME,
            ProductionBalanceFields.LABOR_TIME, ProductionBalanceFields.LABOR_TIME_BALANCE);

    @Autowired
    private ProductionCountingService productionCountingService;

    private static final List<String> L_PRODUCTION_BALANCE_COST_FIELDS = Arrays.asList(
            ProductionBalanceFields.PLANNED_COMPONENTS_COSTS, ProductionBalanceFields.COMPONENTS_COSTS,
            ProductionBalanceFields.COMPONENTS_COSTS_BALANCE, ProductionBalanceFields.PLANNED_MACHINE_COSTS,
            ProductionBalanceFields.MACHINE_COSTS, ProductionBalanceFields.MACHINE_COSTS_BALANCE,
            ProductionBalanceFields.PLANNED_LABOR_COSTS, ProductionBalanceFields.LABOR_COSTS,
            ProductionBalanceFields.LABOR_COSTS_BALANCE, ProductionBalanceFields.PLANNED_CYCLES_COSTS,
            ProductionBalanceFields.CYCLES_COSTS, ProductionBalanceFields.CYCLES_COSTS_BALANCE,
            ProductionBalanceFields.REGISTERED_TOTAL_TECHNICAL_PRODUCTION_COSTS,
            ProductionBalanceFields.REGISTERED_TOTAL_TECHNICAL_PRODUCTION_COST_PER_UNIT,
            ProductionBalanceFields.TOTAL_TECHNICAL_PRODUCTION_COSTS,
            ProductionBalanceFields.TOTAL_TECHNICAL_PRODUCTION_COST_PER_UNIT,
            ProductionBalanceFields.BALANCE_TECHNICAL_PRODUCTION_COSTS,
            ProductionBalanceFields.BALANCE_TECHNICAL_PRODUCTION_COST_PER_UNIT,
            ProductionBalanceFields.PRODUCTION_COST_MARGIN_VALUE, ProductionBalanceFields.MATERIAL_COST_MARGIN_VALUE,
            ProductionBalanceFields.ADDITIONAL_OVERHEAD_VALUE, ProductionBalanceFields.TOTAL_OVERHEAD,
            ProductionBalanceFields.TOTAL_COSTS, ProductionBalanceFields.TOTAL_COST_PER_UNIT);

    private void clearGeneratedCosts(final Entity productionBalance) {
        for (String fieldName : L_PRODUCTION_BALANCE_COST_FIELDS) {
            if (ProductionBalanceFields.PRODUCTION_COST_MARGIN_VALUE.equals(fieldName)
                    || ProductionBalanceFields.MATERIAL_COST_MARGIN_VALUE.equals(fieldName)
                    || ProductionBalanceFields.ADDITIONAL_OVERHEAD_VALUE.equals(fieldName)) {
                productionBalance.setField(fieldName, BigDecimal.ZERO);
            } else {
                productionBalance.setField(fieldName, null);
            }
        }
    }

    public void onSave(final DataDefinition productionBalanceDD, final Entity productionBalance) {
        updateTrackingsNumber(productionBalance);
    }

    public void onCopy(final DataDefinition productionBalanceDD, final Entity productionBalance) {
        clearGeneratedOnCopy(productionBalance);
        clearGeneratedTimes(productionBalance);
        clearGeneratedCosts(productionBalance);

    }

    private void updateTrackingsNumber(final Entity productionBalance) {
        Entity order = productionBalance.getBelongsToField(ProductionBalanceFields.ORDER);

        if ((order != null)
                && !productionCountingService.isTypeOfProductionRecordingBasic(order
                        .getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING))) {
            int trackingsNumber = productionCountingService.getProductionTrackingsForOrder(order).size();

            productionBalance.setField(ProductionBalanceFields.TRACKINGS_NUMBER, trackingsNumber);
        }
    }

    private void clearGeneratedOnCopy(final Entity productionBalance) {
        productionBalance.setField(ProductionBalanceFields.FILE_NAME, null);
        productionBalance.setField(ProductionBalanceFields.GENERATED, false);
        productionBalance.setField(ProductionBalanceFields.DATE, null);
        productionBalance.setField(ProductionBalanceFields.WORKER, null);
    }

    private void clearGeneratedTimes(final Entity productionBalance) {
        for (String fieldName : L_PRODUCTION_BALANCE_TIME_FIELDS) {
            productionBalance.setField(fieldName, BigDecimal.ZERO);
        }
    }

}
