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
        BigDecimal usedQuantity = (BigDecimal) productsList.get(0).getField("usedQuantity");

        for (Entity product : productsList) {
            if (product.getBelongsToField("product").getStringField("number")
                    .equals(prevProduct.getBelongsToField("product").getStringField("number"))) {
                if (product.getField("usedQuantity") != null)
                    if (usedQuantity != null)
                        usedQuantity = usedQuantity.add((BigDecimal) product.getField("usedQuantity"));
                    else
                        usedQuantity = (BigDecimal) product.getField("usedQuantity");
            } else {
                prevProduct.setField("plannedQuantity", plannedQuantity);
                prevProduct.setField("usedQuantity", usedQuantity);
                if (usedQuantity != null)
                    prevProduct.setField("balance", usedQuantity.subtract(plannedQuantity));
                groupedProducts.add(prevProduct);
                prevProduct = product;
                plannedQuantity = (BigDecimal) product.getField("plannedQuantity");
                usedQuantity = (BigDecimal) product.getField("usedQuantity");
            }
        }
        prevProduct.setField("plannedQuantity", plannedQuantity);
        prevProduct.setField("usedQuantity", usedQuantity);
        if (usedQuantity != null)
            prevProduct.setField("balance", usedQuantity.subtract(plannedQuantity));
        else
            prevProduct.setField("balance", null);
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
                            .getStringField("number"))) {
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
