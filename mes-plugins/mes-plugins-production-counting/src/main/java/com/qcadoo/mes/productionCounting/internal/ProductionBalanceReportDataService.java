/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.3
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;

@Service
public class ProductionBalanceReportDataService {

    private static final String LABOR_TIME_BALANCE_FIELD = "laborTimeBalance";

    private static final String MACHINE_TIME_BALANCE_FIELD = "machineTimeBalance";

    private static final String OPERATION_FIELD = "operation";

    private static final String LABOR_TIME_FIELD = "laborTime";

    private static final String MACHINE_TIME_FIELD = "machineTime";

    private static final String NODE_NUMBER_FIELD = "nodeNumber";

    private static final String PLANNED_LABOR_TIME_FIELD = "plannedLaborTime";

    private static final String PLANNED_MACHINE_TIME_FIELD = "plannedMachineTime";

    private static final String BALANCE_FIELD = "balance";

    private static final String USED_QUANTITY_FIELD = "usedQuantity";

    private static final String PRODUCTION_RECORD_FIELD = "productionRecord";

    private static final String ORDER_OPERATION_COMPONENT_FIELD = "orderOperationComponent";

    private static final String PRODUCT_FIELD = "product";

    private static final String NUMBER_FIELD = "number";

    private static final String PLANNED_QUANTITY_FIELD = "plannedQuantity";

    @Autowired
    private NumberService numberService;

    public List<Entity> groupProductInOutComponentsByProduct(final List<Entity> productsList) {
        List<Entity> groupedProducts = new ArrayList<Entity>();

        Entity prevProduct = productsList.get(0);
        BigDecimal plannedQuantity = (BigDecimal) productsList.get(0).getField(PLANNED_QUANTITY_FIELD);
        BigDecimal usedQuantity = null;

        for (Entity product : productsList) {
            if (product.getBelongsToField(PRODUCT_FIELD).getStringField(NUMBER_FIELD)
                    .equals(prevProduct.getBelongsToField(PRODUCT_FIELD).getStringField(NUMBER_FIELD))) {
                if (product.getBelongsToField(PRODUCTION_RECORD_FIELD).getBelongsToField(ORDER_OPERATION_COMPONENT_FIELD) != null
                        && !product
                                .getBelongsToField(PRODUCTION_RECORD_FIELD)
                                .getBelongsToField(ORDER_OPERATION_COMPONENT_FIELD)
                                .getId()
                                .toString()
                                .equals(prevProduct.getBelongsToField(PRODUCTION_RECORD_FIELD)
                                        .getBelongsToField(ORDER_OPERATION_COMPONENT_FIELD).getId().toString())) {
                    plannedQuantity = plannedQuantity.add((BigDecimal) product.getField(PLANNED_QUANTITY_FIELD),
                            numberService.getMathContext());
                }
                if (product.getField(USED_QUANTITY_FIELD) != null) {
                    if (usedQuantity == null) {
                        usedQuantity = (BigDecimal) product.getField(USED_QUANTITY_FIELD);
                    } else {
                        usedQuantity = usedQuantity.add((BigDecimal) product.getField(USED_QUANTITY_FIELD),
                                numberService.getMathContext());
                    }
                }
            } else {
                prevProduct.setField(PLANNED_QUANTITY_FIELD, plannedQuantity);
                prevProduct.setField(USED_QUANTITY_FIELD, usedQuantity);
                if (usedQuantity == null) {
                    prevProduct.setField(BALANCE_FIELD, null);
                } else {
                    prevProduct.setField(BALANCE_FIELD, usedQuantity.subtract(plannedQuantity, numberService.getMathContext()));
                }
                groupedProducts.add(prevProduct);
                prevProduct = product;
                plannedQuantity = (BigDecimal) product.getField(PLANNED_QUANTITY_FIELD);
                usedQuantity = (BigDecimal) product.getField(USED_QUANTITY_FIELD);
            }
        }
        prevProduct.setField(PLANNED_QUANTITY_FIELD, plannedQuantity);
        prevProduct.setField(USED_QUANTITY_FIELD, usedQuantity);
        if (usedQuantity == null) {
            prevProduct.setField(BALANCE_FIELD, null);
        } else {
            prevProduct.setField(BALANCE_FIELD, usedQuantity.subtract(plannedQuantity, numberService.getMathContext()));
        }
        groupedProducts.add(prevProduct);

        return groupedProducts;
    }

    public List<Entity> groupProductionRecordsByOperation(final List<Entity> productionRecords) {
        List<Entity> groupedProducts = new ArrayList<Entity>();

        Entity prevRecord = productionRecords.get(0);
        Integer plannedMachineTime = (Integer) productionRecords.get(0).getField(PLANNED_MACHINE_TIME_FIELD);
        Integer registeredMachineTime = 0;
        Integer plannedLaborTime = (Integer) productionRecords.get(0).getField(PLANNED_LABOR_TIME_FIELD);
        Integer registeredLaborTime = 0;

        for (Entity record : productionRecords) {
            if (record
                    .getBelongsToField(ORDER_OPERATION_COMPONENT_FIELD)
                    .getBelongsToField(OPERATION_FIELD)
                    .getStringField(NUMBER_FIELD)
                    .equals(prevRecord.getBelongsToField(ORDER_OPERATION_COMPONENT_FIELD).getBelongsToField(OPERATION_FIELD)
                            .getStringField(NUMBER_FIELD))
                    && record
                            .getBelongsToField(ORDER_OPERATION_COMPONENT_FIELD)
                            .getStringField(NODE_NUMBER_FIELD)
                            .equals(prevRecord.getBelongsToField(ORDER_OPERATION_COMPONENT_FIELD).getStringField(
                                    NODE_NUMBER_FIELD))) {
                registeredMachineTime += (Integer) record.getField(MACHINE_TIME_FIELD);
                registeredLaborTime += (Integer) record.getField(LABOR_TIME_FIELD);
            } else {
                prevRecord.setField(PLANNED_MACHINE_TIME_FIELD, plannedMachineTime);
                prevRecord.setField(MACHINE_TIME_FIELD, registeredMachineTime);
                prevRecord.setField(MACHINE_TIME_BALANCE_FIELD, registeredMachineTime - plannedMachineTime);
                prevRecord.setField(PLANNED_LABOR_TIME_FIELD, plannedLaborTime);
                prevRecord.setField(LABOR_TIME_FIELD, registeredLaborTime);
                prevRecord.setField(LABOR_TIME_BALANCE_FIELD, registeredLaborTime - plannedLaborTime);
                groupedProducts.add(prevRecord);
                prevRecord = record;
                plannedMachineTime = (Integer) record.getField(PLANNED_MACHINE_TIME_FIELD);
                registeredMachineTime = (Integer) record.getField(MACHINE_TIME_FIELD);
                plannedLaborTime = (Integer) record.getField(PLANNED_LABOR_TIME_FIELD);
                registeredLaborTime = (Integer) record.getField(LABOR_TIME_FIELD);
            }
        }
        prevRecord.setField(PLANNED_MACHINE_TIME_FIELD, plannedMachineTime);
        prevRecord.setField(MACHINE_TIME_FIELD, registeredMachineTime);
        prevRecord.setField(MACHINE_TIME_BALANCE_FIELD, registeredMachineTime - plannedMachineTime);
        prevRecord.setField(PLANNED_LABOR_TIME_FIELD, plannedLaborTime);
        prevRecord.setField(LABOR_TIME_FIELD, registeredLaborTime);
        prevRecord.setField(LABOR_TIME_BALANCE_FIELD, registeredLaborTime - plannedLaborTime);
        groupedProducts.add(prevRecord);

        return groupedProducts;
    }
}
