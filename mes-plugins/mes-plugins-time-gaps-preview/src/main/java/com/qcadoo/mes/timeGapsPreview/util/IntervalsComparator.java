/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
 * Version: 1.4
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
package com.qcadoo.mes.timeGapsPreview.util;

import java.util.Comparator;

import org.joda.time.Interval;

public enum IntervalsComparator implements Comparator<Interval> {

    START_DATE_ASC_AND_DURATION_DESC {

        @Override
        public int compare(final Interval first, final Interval second) {
            int startDatesCompareResult = first.getStart().compareTo(second.getStart());
            if (startDatesCompareResult == 0) {
                return BY_DURATION.compare(first, second) * -1;
            }
            return startDatesCompareResult;
        }
    },

    BY_DURATION {

        @Override
        public int compare(final Interval first, final Interval second) {
            return first.toDuration().compareTo(second.toDuration());
        }
    };

    public abstract int compare(final Interval first, final Interval second);

}
