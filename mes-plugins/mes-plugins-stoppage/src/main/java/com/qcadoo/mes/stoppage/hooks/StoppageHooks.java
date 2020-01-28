package com.qcadoo.mes.stoppage.hooks;

import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.stoppage.constants.StoppageFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class StoppageHooks {

    public boolean validatesWith(final DataDefinition dataDefinition, final Entity entity) {
        Date dateFrom = entity.getDateField(StoppageFields.DATE_FROM);
        Date dateTo = entity.getDateField(StoppageFields.DATE_TO);

        if (dateFrom == null || dateTo == null || dateTo.after(dateFrom)) {
            return true;
        }

        entity.addError(dataDefinition.getField(OrderFields.DATE_TO), "stoppage.validate.global.error.datesStoppage");

        return false;
    }
}
