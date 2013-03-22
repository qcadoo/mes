package com.qcadoo.mes.orders.util;

import com.qcadoo.commons.dateTime.DateRange;
import com.qcadoo.model.api.Entity;

/**
 * Order dates service.
 * 
 * @since 1.2.1
 */
public interface OrderDatesService {

    /**
     * Get start and finish dates (expressed as {@link DateRange})
     * 
     * @param order
     * @return {@link DateRange} representing start and finish dates
     * @since 1.2.1
     */
    DateRange getDates(final Entity order);

}
