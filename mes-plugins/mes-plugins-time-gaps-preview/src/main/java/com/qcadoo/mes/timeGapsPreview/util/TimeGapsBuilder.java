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

import java.util.Collection;

import org.joda.time.Interval;

public interface TimeGapsBuilder {

    /**
     * Add occupied interval (each of registered occupied times will be accommodate during gaps calculation).
     * 
     * @param interval
     *            occupied interval
     * 
     */
    void addOccupiedInterval(final Interval interval);

    /**
     * Add occupied intervals (each of registered occupied times will be accommodate during gaps calculation).
     * 
     * @param interval
     *            occupied intervals
     * 
     * @throws IllegalArgumentException
     *             if given intervals iterable is null.
     */
    void addOccupiedIntervals(final Iterable<Interval> interval);

    /**
     * Calculate gap intervals.
     * 
     * @return collection of gap intervals.
     */
    Collection<Interval> calculateGaps();

}
