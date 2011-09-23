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
        BigDecimal plannedQuantity = BigDecimal.ZERO;
        BigDecimal usedQuantity = BigDecimal.ZERO;

        for (Entity product : productsList) {
            if (product.getStringField("number").equals(prevProduct.getStringField("number"))) {
                plannedQuantity = plannedQuantity.add((BigDecimal) product.getField("plannedQuantity"));
                usedQuantity = usedQuantity.add((BigDecimal) product.getField("usedQuantity"));
            } else {
                prevProduct.setField("plannedQuantity", plannedQuantity);
                prevProduct.setField("usedQuantity", usedQuantity);
                prevProduct.setField("balance", usedQuantity.subtract(plannedQuantity));
                groupedProducts.add(prevProduct);
                prevProduct = product;
                plannedQuantity = (BigDecimal) product.getField("plannedQuantity");
                usedQuantity = (BigDecimal) product.getField("usedQuantity");
            }
        }
        prevProduct.setField("plannedQuantity", plannedQuantity);
        prevProduct.setField("usedQuantity", usedQuantity);
        prevProduct.setField("balance", usedQuantity.subtract(plannedQuantity));
        groupedProducts.add(prevProduct);

        return groupedProducts;
    }

    public List<Entity> groupProductionRecordsByOperation(final List<Entity> productionRecords) {
        List<Entity> groupedProducts = new ArrayList<Entity>();

        Entity prevRecord = productionRecords.get(0);
        BigDecimal plannedMachineTime = BigDecimal.ZERO;
        BigDecimal registeredMachineTime = BigDecimal.ZERO;
        BigDecimal plannedLaborTime = BigDecimal.ZERO;
        BigDecimal registeredLaborTime = BigDecimal.ZERO;

        for (Entity record : productionRecords) {
            if (record
                    .getBelongsToField("orderOperationComponent")
                    .getBelongsToField("operation")
                    .getStringField("number")
                    .equals(prevRecord.getBelongsToField("orderOperationComponent").getBelongsToField("operation")
                            .getStringField("number"))) {
                plannedMachineTime = plannedMachineTime.add((BigDecimal) record.getField("plannedMachineTime"));
                registeredMachineTime = registeredMachineTime.add((BigDecimal) record.getField("machineTime"));
                plannedLaborTime = plannedLaborTime.add((BigDecimal) record.getField("plannedLaborTime"));
                registeredLaborTime = registeredLaborTime.add((BigDecimal) record.getField("laborTime"));
            } else {
                prevRecord.setField("plannedMachineTime", plannedMachineTime);
                prevRecord.setField("machineTime", registeredMachineTime);
                prevRecord.setField("machineTimeBalance", registeredMachineTime.subtract(plannedMachineTime));
                prevRecord.setField("plannedLaborTime", plannedLaborTime);
                prevRecord.setField("laborTime", registeredLaborTime);
                prevRecord.setField("laborTimeBalance", registeredLaborTime.subtract(plannedLaborTime));
                groupedProducts.add(prevRecord);
                prevRecord = record;
                plannedMachineTime = (BigDecimal) record.getField("plannedMachineTime");
                registeredMachineTime = (BigDecimal) record.getField("machineTime");
                plannedLaborTime = (BigDecimal) record.getField("plannedLaborTime");
                registeredLaborTime = (BigDecimal) record.getField("laborTime");
            }
        }
        prevRecord.setField("plannedMachineTime", plannedMachineTime);
        prevRecord.setField("machineTime", registeredMachineTime);
        prevRecord.setField("machineTimeBalance", registeredMachineTime.subtract(plannedMachineTime));
        prevRecord.setField("plannedLaborTime", plannedLaborTime);
        prevRecord.setField("laborTime", registeredLaborTime);
        prevRecord.setField("laborTimeBalance", registeredLaborTime.subtract(plannedLaborTime));
        groupedProducts.add(prevRecord);

        return groupedProducts;
    }

}
