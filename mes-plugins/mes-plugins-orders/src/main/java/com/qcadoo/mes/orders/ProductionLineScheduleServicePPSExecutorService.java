package com.qcadoo.mes.orders;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.listeners.ProductionLinePositionNewData;
import com.qcadoo.model.api.Entity;
import com.qcadoo.plugin.api.PluginUtils;
import com.qcadoo.plugin.api.RunIfEnabled;

@Service
public class ProductionLineScheduleServicePPSExecutorService {

    @Autowired
    List<ProductionLineScheduleServicePPS> productionLineScheduleServicesPPS;

    public void createProductionLinePositionNewData(Map<Long, ProductionLinePositionNewData> orderProductionLinesPositionNewData,
                                                    Entity productionLine, Date startDate, Entity position, Entity technology, Entity previousOrder) {
        for (ProductionLineScheduleServicePPS service : productionLineScheduleServicesPPS) {
            if (canRun(service)) {
                service.createProductionLinePositionNewData(orderProductionLinesPositionNewData, productionLine, startDate, position, technology, previousOrder);
            }
        }
    }

    private <M extends Object & ProductionLineScheduleServicePPS> boolean canRun(M service) {
        RunIfEnabled runIfEnabled = service.getClass().getAnnotation(RunIfEnabled.class);
        if (runIfEnabled == null) {
            return true;
        }
        for (String pluginIdentifier : runIfEnabled.value()) {
            if (!PluginUtils.isEnabled(pluginIdentifier)) {
                return false;
            }
        }
        return true;
    }

    public void savePosition(Entity position, ProductionLinePositionNewData productionLinePositionNewData) {
        for (ProductionLineScheduleServicePPS service : productionLineScheduleServicesPPS) {
            if (canRun(service)) {
                service.savePosition(position, productionLinePositionNewData);
            }
        }
    }

    public void copyPPS(Entity productionLineSchedule, Entity order, Entity productionLine) {
        for (ProductionLineScheduleServicePPS service : productionLineScheduleServicesPPS) {
            if (canRun(service)) {
                service.copyPPS(productionLineSchedule, order, productionLine);
            }
        }
    }
}
