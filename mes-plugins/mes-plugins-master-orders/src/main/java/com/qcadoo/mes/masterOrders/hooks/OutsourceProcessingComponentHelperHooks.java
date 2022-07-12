package com.qcadoo.mes.masterOrders.hooks;

import com.qcadoo.mes.masterOrders.constants.OutsourceProcessingComponentHelperFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;

@Service
public class OutsourceProcessingComponentHelperHooks {

    public boolean validateDates(final DataDefinition outsourceProcessingComponentHelperDD, final Entity outsourceProcessingComponentHelper) {
        Date dateFrom = outsourceProcessingComponentHelper.getDateField(OutsourceProcessingComponentHelperFields.DATE_FROM);
        Date dateTo = outsourceProcessingComponentHelper.getDateField(OutsourceProcessingComponentHelperFields.DATE_TO);

        if (Objects.isNull(dateFrom) || Objects.isNull(dateTo) || dateTo.after(dateFrom)) {
            return true;
        }

        outsourceProcessingComponentHelper.addError(outsourceProcessingComponentHelperDD.getField(OutsourceProcessingComponentHelperFields.DATE_TO), "orders.validate.global.error.datesOrder");

        return false;
    }

}
