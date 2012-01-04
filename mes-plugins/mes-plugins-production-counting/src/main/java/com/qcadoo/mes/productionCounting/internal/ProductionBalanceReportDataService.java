/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.1
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

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.Entity;

@Service
public class ProductionBalanceReportDataService {

    public List<Entity> groupProductInOutComponentsByProduct(final List<Entity> productsList) {
        List<Entity> groupedProducts = new ArrayList<Entity>();

        Entity prevProduct = productsList.get(0);
        BigDecimal plannedQuantity = (BigDecimal) productsList.get(0).getField("plannedQuantity");
        BigDecimal usedQuantity = null;

        for (Entity product : productsList) {
            if (product.getBelongsToField("product").getStringField("number")
                    .equals(prevProduct.getBelongsToField("product").getStringField("number"))) {
                if (product.getBelongsToField("productionRecord").getBelongsToField("orderOperationComponent") != null
                        && !product
                                .getBelongsToField("productionRecord")
                                .getBelongsToField("orderOperationComponent")
                                .getId()
                                .toString()
                                .equals(prevProduct.getBelongsToField("productionRecord")
                                        .getBelongsToField("orderOperationComponent").getId().toString())) {
                    plannedQuantity = plannedQuantity.add((BigDecimal) product.getField("plannedQuantity"));
                }
                if (product.getField("usedQuantity") != null) {
                    if (usedQuantity == null) {
                        usedQuantity = (BigDecimal) product.getField("usedQuantity");
                    } else {
                        usedQuantity = usedQuantity.add((BigDecimal) product.getField("usedQuantity"));
                    }
                }
            } else {
                prevProduct.setField("plannedQuantity", plannedQuantity);
                prevProduct.setField("usedQuantity", usedQuantity);
                if (usedQuantity == null) {
                    prevProduct.setField("balance", null);
                } else {
                    prevProduct.setField("balance", usedQuantity.subtract(plannedQuantity));
                }
                groupedProducts.add(prevProduct);
                prevProduct = product;
                plannedQuantity = (BigDecimal) product.getField("plannedQuantity");
                usedQuantity = (BigDecimal) product.getField("usedQuantity");
            }
        }
        prevProduct.setField("plannedQuantity", plannedQuantity);
        prevProduct.setField("usedQuantity", usedQuantity);
        if (usedQuantity == null) {
            prevProduct.setField("balance", null);
        } else {
            prevProduct.setField("balance", usedQuantity.subtract(plannedQuantity));
        }
        groupedProducts.add(prevProduct);

        return groupedProducts;
    }

    public List<Entity> groupProductionRecordsByOperation(final List<Entity> productionRecords) {
        List<Entity> groupedProducts = new ArrayList<Entity>();

        Entity prevRecord = productionRecords.get(0);
        Integer plannedMachineTime = (Integer) productionRecords.get(0).getField("plannedMachineTime");
        Integer registeredMachineTime = 0;
        Integer plannedLaborTime = (Integer) productionRecords.get(0).getField("plannedLaborTime");
        Integer registeredLaborTime = 0;

        for (Entity record : productionRecords) {
            if (record
                    .getBelongsToField("orderOperationComponent")
                    .getBelongsToField("operation")
                    .getStringField("number")
                    .equals(prevRecord.getBelongsToField("orderOperationComponent").getBelongsToField("operation")
                            .getStringField("number"))
                    && record.getBelongsToField("orderOperationComponent").getStringField("nodeNumber")
                            .equals(prevRecord.getBelongsToField("orderOperationComponent").getStringField("nodeNumber"))) {
                registeredMachineTime += (Integer) record.getField("machineTime");
                registeredLaborTime += (Integer) record.getField("laborTime");
            } else {
                prevRecord.setField("plannedMachineTime", plannedMachineTime);
                prevRecord.setField("machineTime", registeredMachineTime);
                prevRecord.setField("machineTimeBalance", registeredMachineTime - plannedMachineTime);
                prevRecord.setField("plannedLaborTime", plannedLaborTime);
                prevRecord.setField("laborTime", registeredLaborTime);
                prevRecord.setField("laborTimeBalance", registeredLaborTime - plannedLaborTime);
                groupedProducts.add(prevRecord);
                prevRecord = record;
                plannedMachineTime = (Integer) record.getField("plannedMachineTime");
                registeredMachineTime = (Integer) record.getField("machineTime");
                plannedLaborTime = (Integer) record.getField("plannedLaborTime");
                registeredLaborTime = (Integer) record.getField("laborTime");
            }
        }
        prevRecord.setField("plannedMachineTime", plannedMachineTime);
        prevRecord.setField("machineTime", registeredMachineTime);
        prevRecord.setField("machineTimeBalance", registeredMachineTime - plannedMachineTime);
        prevRecord.setField("plannedLaborTime", plannedLaborTime);
        prevRecord.setField("laborTime", registeredLaborTime);
        prevRecord.setField("laborTimeBalance", registeredLaborTime - plannedLaborTime);
        groupedProducts.add(prevRecord);

        return groupedProducts;
    }
}
