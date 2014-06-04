/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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

    public void onSave(final DataDefinition productionBalanceDD, final Entity productionBalance) {
        updateTrackingsNumber(productionBalance);
    }

    public void onCopy(final DataDefinition productionBalanceDD, final Entity productionBalance) {
        clearGeneratedOnCopy(productionBalance);
        clearGeneratedTimes(productionBalance);
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
