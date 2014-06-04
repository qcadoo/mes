/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
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
