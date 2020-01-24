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

    public void onSave(final DataDefinition dataDefinition, final Entity entity) {
        Date dateFrom = entity.getDateField(StoppageFields.DATE_FROM);
        Date dateTo = entity.getDateField(StoppageFields.DATE_TO);
        Entity entityFromDB = null;
        if (entity.getId() != null) {
            entityFromDB = dataDefinition.get(entity.getId());
        }
        if (dateFrom != null && dateTo != null
                && (entity.getId() == null || entityFromDB != null && (entityFromDB.getDateField(StoppageFields.DATE_FROM) == null
                        || entityFromDB.getDateField(StoppageFields.DATE_TO) == null
                        || dateFrom.compareTo(entityFromDB.getDateField(StoppageFields.DATE_FROM)) != 0
                        || dateTo.compareTo(entityFromDB.getDateField(StoppageFields.DATE_TO)) != 0))) {
            entity.setField(StoppageFields.DURATION, (int) (dateTo.getTime() - dateFrom.getTime()) / 1000);
        }
    }
}
