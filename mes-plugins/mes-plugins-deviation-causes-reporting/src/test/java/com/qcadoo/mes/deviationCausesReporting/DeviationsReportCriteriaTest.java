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
package com.qcadoo.mes.deviationCausesReporting;

import junit.framework.Assert;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Test;

import com.google.common.base.Optional;

public class DeviationsReportCriteriaTest {

    @Test
    public final void shouldBuildCriteria() {
        // given
        DateTime fromDateTime = new DateTime(2014, 7, 10, 14, 35);
        DateTime toDateTime = new DateTime(2014, 7, 12, 14, 35);

        // when
        DeviationsReportCriteria reportCriteria = DeviationsReportCriteria.forDates(fromDateTime, Optional.of(toDateTime));

        // then
        Assert.assertEquals(new DateTime(2014, 7, 10, 0, 0, 0, 0), reportCriteria.getSearchInterval().getStart());
        Assert.assertEquals(new DateTime(2014, 7, 12, 23, 59, 59, 999), reportCriteria.getSearchInterval().getEnd());
    }

    @Test
    public final void shouldBuildCriteriaForOneDay() {
        // given
        DateTime fromDateTime = new DateTime(2014, 7, 10, 14, 35);
        DateTime toDateTime = new DateTime(2014, 7, 10, 14, 35);

        // when
        DeviationsReportCriteria reportCriteria = DeviationsReportCriteria.forDates(fromDateTime, Optional.of(toDateTime));

        // then
        Assert.assertEquals(new DateTime(2014, 7, 10, 0, 0, 0, 0), reportCriteria.getSearchInterval().getStart());
        Assert.assertEquals(new DateTime(2014, 7, 10, 23, 59, 59, 999), reportCriteria.getSearchInterval().getEnd());
    }

    @Test
    public final void shouldBuildCriteriaWithOnlyBeginDatePassed() {
        LocalDate fromDateTime = new LocalDate(2014, 7, 10);

        // when
        DeviationsReportCriteria reportCriteria = DeviationsReportCriteria.forDates(fromDateTime, Optional.<LocalDate> absent());

        // then
        Assert.assertEquals(new DateTime(2014, 7, 10, 0, 0, 0, 0), reportCriteria.getSearchInterval().getStart());
        Assert.assertEquals(LocalDate.now().plusDays(1).toDateTimeAtStartOfDay().minusMillis(1), reportCriteria
                .getSearchInterval().getEnd());
    }

    @Test
    public final void shouldThrowExceptionIfArgumentsAreIncorrect() {
        try {
            DeviationsReportCriteria.forDates(null, Optional.of(DateTime.now()));
            Assert.fail();
        } catch (IllegalArgumentException iae) {
            // success
        }

        try {
            DeviationsReportCriteria.forDates(DateTime.now(), null);
            Assert.fail();
        } catch (IllegalArgumentException iae) {
            // success
        }

        try {
            DeviationsReportCriteria.forDates(DateTime.now().plusDays(1), Optional.of(DateTime.now()));
            Assert.fail();
        } catch (IllegalArgumentException iae) {
            // success
        }
    }

    @Test
    public final void shouldUseConsistentDefaultDateTo() {
        // given
        DateTime endOfCurrentDay = DateTime.now().withTimeAtStartOfDay().plusDays(1).minusMillis(1);

        // when
        DeviationsReportCriteria criteria = DeviationsReportCriteria.forDates(DateTime.now(), Optional.<DateTime> absent());

        // then
        Assert.assertEquals(endOfCurrentDay, criteria.getSearchInterval().getEnd());
        Assert.assertEquals(endOfCurrentDay, DeviationsReportCriteria.getDefaultDateTo());
        Assert.assertEquals(DeviationsReportCriteria.getDefaultDateTo(), criteria.getSearchInterval().getEnd());
    }

}
