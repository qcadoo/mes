package com.qcadoo.mes.productionPerShift.services;

import com.qcadoo.mes.basicProductionCounting.BasicProductionCountingService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftFields;
import com.qcadoo.mes.productionPerShift.domain.ProgressForDaysContainer;
import com.qcadoo.model.api.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;

@Service
public class AutomaticPpsTechNormAndWorkersService implements AutomaticPpsService {

    @Autowired
    private BasicProductionCountingService basicProductionCountingService;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void generateProgressForDays(ProgressForDaysContainer progressForDaysContainer, Entity productionPerShift) {
        Entity order = productionPerShift.getBelongsToField(ProductionPerShiftFields.ORDER);
        if (progressForDaysContainer.getOrder() != null) {
            order = progressForDaysContainer.getOrder();
        }
        Date orderStartDate = order.getDateField(OrderFields.START_DATE);
        BigDecimal plannedQuantity = order.getDecimalField(OrderFields.PLANNED_QUANTITY);
        if (order.getBooleanField(OrderFields.FINAL_PRODUCTION_TRACKING)) {
            plannedQuantity = basicProductionCountingService.getProducedQuantityFromBasicProductionCountings(order);
        }


    }
}
