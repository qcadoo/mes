package com.qcadoo.mes.productionScheduling;

import java.util.Date;
import java.util.Optional;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.ShiftsService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.model.api.Entity;

@Service
public class ProductionSchedulingService {

    @Autowired
    private ShiftsService shiftsService;

    public Date getFinishDate(Entity order, Date orderStartDate, long seconds) {
        return shiftsService.findDateToForProductionLine(orderStartDate, seconds,
                order.getBelongsToField(OrderFields.PRODUCTION_LINE));
    }

    public Date getStartDate(Entity order, Date orderStartDate, Integer offset) {
        if (offset == 0) {
            Date dateFrom = null;
            Optional<DateTime> maybeDate = shiftsService.getNearestWorkingDate(new DateTime(orderStartDate),
                    order.getBelongsToField(OrderFields.PRODUCTION_LINE));
            if (maybeDate.isPresent()) {
                dateFrom = maybeDate.get().toDate();
            }
            return dateFrom;
        } else {
            return shiftsService.findDateToForProductionLine(orderStartDate, offset,
                    order.getBelongsToField(OrderFields.PRODUCTION_LINE));
        }
    }
}
