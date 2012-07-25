package com.qcadoo.mes.productionPerShift.hooks;

import java.util.Date;

import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionPerShift.constants.ProgressForDayFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class ProgressForDayHooks {

    public void saveDateOfDay(final DataDefinition dataDefinition, final Entity entity) {
        Entity order = entity.getBelongsToField(ProgressForDayFields.TECH_INST_OPER_COMP).getBelongsToField("order");
        Integer day = (Integer) entity.getField(ProgressForDayFields.DAY);
        DateTime orderStartDate = new DateTime((Date) order.getField(OrderFields.START_DATE));
        Date dayOfDay = orderStartDate.plusDays(day - 1).toDate();
        entity.setField(ProgressForDayFields.DATE_OF_DAY, dayOfDay);
    }
}
