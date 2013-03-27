package com.qcadoo.mes.orders.util;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.commons.dateTime.DateRange;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.model.api.Entity;

@Service
public final class OrderDatesServiceImpl implements OrderDatesService {

    private static final List<String> START_DATE_FIELDS = Lists.newArrayList(OrderFields.EFFECTIVE_DATE_FROM,
            OrderFields.CORRECTED_DATE_FROM, OrderFields.DATE_FROM);

    private static final List<String> FINISH_DATE_FIELDS = Lists.newArrayList(OrderFields.EFFECTIVE_DATE_TO,
            OrderFields.CORRECTED_DATE_TO, OrderFields.DATE_TO);

    @Override
    public DateRange getDates(final Entity order) {
        return new DateRange(getStartDate(order), getFinishDate(order));
    }

    private Date getStartDate(final Entity order) {
        return findFirstNonEmptyDate(order, START_DATE_FIELDS);
    }

    private Date getFinishDate(final Entity order) {
        return findFirstNonEmptyDate(order, FINISH_DATE_FIELDS);
    }

    private Date findFirstNonEmptyDate(final Entity entity, final Iterable<String> fieldNames) {
        for (String fieldName : fieldNames) {
            Date fieldValue = entity.getDateField(fieldName);
            if (fieldValue != null) {
                return fieldValue;
            }
        }
        return null;
    }

    private Date getDate(final Entity entity, final String fieldName) {
        Date fieldValue = entity.getDateField(fieldName);
        if (fieldValue != null) {
            return fieldValue;
        }

        return null;
    }

    @Override
    public DateRange getDatesFromAndTo(Entity order) {
        return new DateRange(getDate(order, OrderFields.START_DATE), getDate(order, OrderFields.FINISH_DATE));
    }

}
