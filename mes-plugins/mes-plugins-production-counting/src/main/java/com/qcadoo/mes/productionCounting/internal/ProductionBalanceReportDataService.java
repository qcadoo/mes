/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.5
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

import static com.qcadoo.mes.basic.constants.ProductFields.NUMBER;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionRecordFields.EXECUTED_OPERATION_CYCLES;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionRecordFields.LABOR_TIME;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionRecordFields.MACHINE_TIME;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionRecordFields.TECHNOLOGY_INSTANCE_OPERATION_COMPONENT;
import static com.qcadoo.mes.technologies.constants.TechnologyInstanceOperCompFields.NODE_NUMBER;
import static com.qcadoo.mes.technologies.constants.TechnologyInstanceOperCompFields.OPERATION;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionCounting.internal.print.utils.EntityProductInOutComparator;
import com.qcadoo.mes.productionCounting.internal.print.utils.EntityProductionRecordOperationComparator;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;

@Service
public class ProductionBalanceReportDataService {

    private static final String L_USED_QUANTITY = "usedQuantity";

    private static final String L_PLANNED_QUANTITY = "plannedQuantity";

    private static final String L_BALANCE = "balance";

    private static final String L_PRODUCTION_RECORD = "productionRecord";

    private static final String L_PRODUCT = "product";

    @Autowired
    private NumberService numberService;

    public List<Entity> groupProductInOutComponentsByProduct(final List<Entity> products) {
        Collections.sort(products, new EntityProductInOutComparator());

        List<Entity> groupedProducts = new ArrayList<Entity>();

        Entity prevProduct = products.get(0);

        BigDecimal plannedQuantity = products.get(0).getDecimalField(L_PLANNED_QUANTITY);
        BigDecimal usedQuantity = (products.get(0).getField(L_USED_QUANTITY) == null) ? null : products.get(0).getDecimalField(
                L_USED_QUANTITY);

        if (!products.isEmpty()) {
            for (Entity product : products) {
                if (checkIfProductHasChanged(prevProduct, product) || (products.indexOf(product) == (products.size() - 1))) {
                    prevProduct.setField(L_PLANNED_QUANTITY, plannedQuantity);
                    prevProduct.setField(L_USED_QUANTITY, usedQuantity);
                    prevProduct.setField(
                            L_BALANCE,
                            (usedQuantity == null) ? null
                                    : usedQuantity.subtract(plannedQuantity, numberService.getMathContext()));

                    groupedProducts.add(prevProduct);

                    prevProduct = product;

                    plannedQuantity = product.getDecimalField(L_PLANNED_QUANTITY);
                    usedQuantity = (product.getField(L_USED_QUANTITY) == null) ? null : product.getDecimalField(L_USED_QUANTITY);
                } else {
                    if (checkIfTechnologyInstanceOperationComponentHasChanged(prevProduct, product)) {
                        plannedQuantity = plannedQuantity.add(product.getDecimalField(L_PLANNED_QUANTITY),
                                numberService.getMathContext());
                    }
                    if (product.getField(L_USED_QUANTITY) != null) {
                        usedQuantity = (usedQuantity == null) ? product.getDecimalField(L_USED_QUANTITY) : usedQuantity.add(
                                product.getDecimalField(L_USED_QUANTITY), numberService.getMathContext());
                    }
                }
            }
        }

        return groupedProducts;
    }

    public List<Entity> groupProductionRecordsByOperation(final List<Entity> productionRecords) {
        Collections.sort(productionRecords, new EntityProductionRecordOperationComparator());

        List<Entity> groupedProductionRecords = new ArrayList<Entity>();

        Entity prevProductionRecord = productionRecords.get(0);

        Integer machineTime = (Integer) productionRecords.get(0).getField(MACHINE_TIME);
        Integer laborTime = (Integer) productionRecords.get(0).getField(LABOR_TIME);

        BigDecimal executedOperationCycles = (productionRecords.get(0).getField(EXECUTED_OPERATION_CYCLES) == null) ? null
                : productionRecords.get(0).getDecimalField(EXECUTED_OPERATION_CYCLES);

        if (!productionRecords.isEmpty()) {
            for (Entity productionRecord : productionRecords) {
                if (checkIfOperationHasChanged(prevProductionRecord, productionRecord)
                        || (productionRecords.indexOf(productionRecord) == (productionRecords.size() - 1))) {
                    prevProductionRecord.setField(MACHINE_TIME, machineTime);
                    prevProductionRecord.setField(LABOR_TIME, laborTime);
                    prevProductionRecord.setField(EXECUTED_OPERATION_CYCLES, executedOperationCycles);

                    groupedProductionRecords.add(prevProductionRecord);

                    prevProductionRecord = productionRecord;

                    machineTime = (Integer) productionRecord.getField(MACHINE_TIME);
                    laborTime = (Integer) productionRecord.getField(LABOR_TIME);

                    executedOperationCycles = (productionRecord.getField(EXECUTED_OPERATION_CYCLES) == null) ? null
                            : productionRecord.getDecimalField(EXECUTED_OPERATION_CYCLES);
                } else {
                    machineTime += (Integer) productionRecord.getField(MACHINE_TIME);
                    laborTime += (Integer) productionRecord.getField(LABOR_TIME);

                    if (productionRecord.getField(EXECUTED_OPERATION_CYCLES) != null) {
                        executedOperationCycles = (executedOperationCycles == null) ? productionRecord
                                .getDecimalField(EXECUTED_OPERATION_CYCLES) : executedOperationCycles.add(
                                productionRecord.getDecimalField(EXECUTED_OPERATION_CYCLES), numberService.getMathContext());
                    }
                }
            }
        }

        return groupedProductionRecords;
    }

    private boolean checkIfProductHasChanged(final Entity prevProduct, final Entity product) {
        return (!product.getBelongsToField(L_PRODUCT).getStringField(NUMBER)
                .equals(prevProduct.getBelongsToField(L_PRODUCT).getStringField(NUMBER)));
    }

    private boolean checkIfTechnologyInstanceOperationComponentHasChanged(final Entity prevProduct, final Entity product) {
        return (product.getBelongsToField(L_PRODUCTION_RECORD).getBelongsToField(TECHNOLOGY_INSTANCE_OPERATION_COMPONENT) != null && !product
                .getBelongsToField(L_PRODUCTION_RECORD)
                .getBelongsToField(TECHNOLOGY_INSTANCE_OPERATION_COMPONENT)
                .getId()
                .equals(prevProduct.getBelongsToField(L_PRODUCTION_RECORD)
                        .getBelongsToField(TECHNOLOGY_INSTANCE_OPERATION_COMPONENT).getId()));
    }

    private boolean checkIfOperationHasChanged(final Entity prevProductionRecord, final Entity productionRecord) {
        if ((productionRecord.getBelongsToField(TECHNOLOGY_INSTANCE_OPERATION_COMPONENT) == null)
                && (prevProductionRecord.getBelongsToField(TECHNOLOGY_INSTANCE_OPERATION_COMPONENT) == null)) {
            return false;
        } else if ((productionRecord.getBelongsToField(TECHNOLOGY_INSTANCE_OPERATION_COMPONENT) == null)
                || (prevProductionRecord.getBelongsToField(TECHNOLOGY_INSTANCE_OPERATION_COMPONENT) == null)) {
            return true;
        } else {
            return (!productionRecord
                    .getBelongsToField(TECHNOLOGY_INSTANCE_OPERATION_COMPONENT)
                    .getBelongsToField(OPERATION)
                    .getStringField(NUMBER)
                    .equals(prevProductionRecord.getBelongsToField(TECHNOLOGY_INSTANCE_OPERATION_COMPONENT)
                            .getBelongsToField(OPERATION).getStringField(NUMBER)) || !productionRecord
                    .getBelongsToField(TECHNOLOGY_INSTANCE_OPERATION_COMPONENT)
                    .getStringField(NODE_NUMBER)
                    .equals(prevProductionRecord.getBelongsToField(TECHNOLOGY_INSTANCE_OPERATION_COMPONENT).getStringField(
                            NODE_NUMBER)));
        }
    }
}
