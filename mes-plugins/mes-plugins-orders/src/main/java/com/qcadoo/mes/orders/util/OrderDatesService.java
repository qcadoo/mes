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
     * Compute start and finish dates from effective, corrected and planned dates (expressed as {@link DateRange})
     * 
     * @param order
     * @return {@link DateRange} representing start and finish dates
     * @since 1.2.1
     */
    DateRange getCalculatedDates(final Entity order);

    /**
     * Get start and finish dates from entity (expressed as {@link DateRange})
     * 
     * @param order
     * @return {@link DateRange} representing start and finish dates
     * @since 1.2.1
     */
    DateRange getExistingDates(final Entity order);

}
