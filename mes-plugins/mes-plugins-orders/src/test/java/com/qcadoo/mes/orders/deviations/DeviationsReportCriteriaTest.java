package com.qcadoo.mes.orders.deviations;

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
