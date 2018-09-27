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
package com.qcadoo.mes.timeGapsPreview.provider;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;

import java.util.Collection;
import java.util.List;

import junit.framework.Assert;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.Minutes;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.qcadoo.mes.lineChangeoverNorms.ChangeoverNormsSearchService;
import com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields;
import com.qcadoo.mes.lineChangeoverNormsForOrders.constants.OrderFieldsLCNFO;
import com.qcadoo.mes.timeGapsPreview.TimeGapsContext;
import com.qcadoo.mes.timeGapsPreview.provider.helper.OrderIntervalsModelHelper;
import com.qcadoo.model.api.Entity;
import com.qcadoo.testing.model.EntityTestUtils;

public class OrderAndChangeoverIntervalsProviderTest {

    private static final Long TECH_1_ID = 1L;

    private static final Long TECH_2_ID = 2L;

    private static final Long TECH_3_ID = 3L;

    private static final Long TECH_GROUP_1_ID = 101L;

    private static final Long TECH_GROUP_2_ID = 102L;

    private static final Long LINE_1_ID = 201L;

    private static final Long LINE_2_ID = 202L;

    private static final DateTime SOME_DATE = new DateTime(2014, 1, 10, 0, 0, 0);

    private static final Interval SEARCH_DOMAIN_INTERVAL = new Interval(SOME_DATE.minusDays(1), SOME_DATE.plusDays(1));

    private static final TimeGapsContext DEFAULT_CONTEXT = new TimeGapsContext(SEARCH_DOMAIN_INTERVAL, Sets.newHashSet(1L, 2L,
            3L, 4L, 5L), Duration.ZERO);

    private OrderAndChangeoverIntervalsProvider orderAndChangeoverIntervalsProvider;

    @Mock
    private OrderIntervalsModelHelper orderIntervalsModelHelper;

    @Mock
    private ChangeoverNormsSearchService changeoverNormsSearchService;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        orderAndChangeoverIntervalsProvider = new OrderAndChangeoverIntervalsProvider();

        ReflectionTestUtils.setField(orderAndChangeoverIntervalsProvider, "orderIntervalsModelHelper", orderIntervalsModelHelper);
        ReflectionTestUtils.setField(orderAndChangeoverIntervalsProvider, "changeoverNormsSearchService",
                changeoverNormsSearchService);

        Entity defaultChangeover = mockChangeoverEntity(60 * 60 * 24);
        given(changeoverNormsSearchService.findBestMatching(anyLong(), anyLong(), anyLong(), anyLong(), anyLong())).willReturn(
                defaultChangeover);
    }

    private Entity mockOrderProjection(final Interval interval, final Long productionLineId, final Integer ownChangeoverDuration,
            final Long techId, final Long techGroupId) {
        Entity orderProjection = EntityTestUtils.mockEntity();
        EntityTestUtils.stubDateField(orderProjection, OrderIntervalsModelHelper.DATE_FROM_ALIAS, interval.getStart().toDate());
        EntityTestUtils.stubDateField(orderProjection, OrderIntervalsModelHelper.DATE_TO_ALIAS, interval.getEnd().toDate());
        EntityTestUtils.stubIntegerField(orderProjection, OrderFieldsLCNFO.OWN_LINE_CHANGEOVER_DURATION, ownChangeoverDuration);
        EntityTestUtils.stubField(orderProjection, OrderIntervalsModelHelper.PRODUCTION_LINE_ID_ALIAS, productionLineId);
        EntityTestUtils.stubField(orderProjection, OrderIntervalsModelHelper.TECHNOLOGY_ID_ALIAS, techId);
        EntityTestUtils.stubField(orderProjection, OrderIntervalsModelHelper.TECHNOLOGY_GROUP_ID_ALIAS, techGroupId);
        return orderProjection;
    }

    private void stubModelHelper(final Entity... orderProjections) {
        given(orderIntervalsModelHelper.getOrdersProjection(Mockito.any(TimeGapsContext.class))).willAnswer(
                new Answer<List<Entity>>() {

                    @Override
                    public List<Entity> answer(InvocationOnMock invocation) throws Throwable {
                        return Lists.newArrayList(orderProjections);
                    }
                });
    }

    private Entity mockChangeoverEntity(final Integer duration) {
        Entity changeover = EntityTestUtils.mockEntity();
        EntityTestUtils.stubIntegerField(changeover, LineChangeoverNormsFields.DURATION, duration);
        return changeover;
    }

    private void stubChangeoverSearchResults(final Long fromTech, final Long fromTechGroup, final Long toTech,
            final Long toTechGroup, final Long productionLine, final Integer duration) {
        Entity changeoverMock = mockChangeoverEntity(duration);
        given(changeoverNormsSearchService.findBestMatching(fromTech, fromTechGroup, toTech, toTechGroup, productionLine))
                .willReturn(changeoverMock);
    }

    private static int minutesAsSeconds(final int minutes) {
        return Minutes.minutes(minutes).toStandardSeconds().getSeconds();
    }

    private Interval changeoverBefore(final Interval orderInterval, final int durationInMinutes) {
        return new Interval(orderInterval.getStart().minusMinutes(durationInMinutes), orderInterval.getStart());
    }

    @Test
    public void shouldAggregateIntervalsByProductionLineIdAndFindChangeovers() {
        // given
        Interval firstOrderInterval = new Interval(SOME_DATE, SOME_DATE.plusHours(6));
        Entity firstOrder = mockOrderProjection(firstOrderInterval, LINE_1_ID, null, TECH_1_ID, TECH_GROUP_1_ID);

        Interval secondOrderInterval = new Interval(SOME_DATE.plusHours(8), SOME_DATE.plusHours(12));
        Entity secondOrder = mockOrderProjection(secondOrderInterval, LINE_1_ID, null, TECH_2_ID, TECH_GROUP_2_ID);

        Interval thirdOrderInterval = new Interval(SOME_DATE.plusHours(3), SOME_DATE.plusHours(6));
        Entity thirdOrder = mockOrderProjection(thirdOrderInterval, LINE_2_ID, null, TECH_3_ID, TECH_GROUP_2_ID);

        stubModelHelper(firstOrder, secondOrder, thirdOrder);

        stubChangeoverSearchResults(TECH_1_ID, TECH_GROUP_1_ID, TECH_2_ID, TECH_GROUP_2_ID, LINE_1_ID, minutesAsSeconds(30));

        // when
        Multimap<Long, Interval> resMap = orderAndChangeoverIntervalsProvider.getIntervalsPerProductionLine(DEFAULT_CONTEXT);

        // then
        Assert.assertEquals(2, resMap.keySet().size());
        Assert.assertTrue(resMap.containsKey(LINE_1_ID));
        Assert.assertTrue(resMap.containsKey(LINE_2_ID));

        Collection<Interval> line1intervals = resMap.get(LINE_1_ID);
        Assert.assertEquals(3, line1intervals.size());
        Assert.assertTrue(line1intervals.contains(firstOrderInterval));
        Assert.assertTrue(line1intervals.contains(secondOrderInterval));
        Assert.assertTrue(line1intervals.contains(changeoverBefore(secondOrderInterval, 30)));
        Collection<Interval> line2intervals = resMap.get(LINE_2_ID);
        Assert.assertEquals(1, line2intervals.size());
        Assert.assertTrue(line2intervals.contains(thirdOrderInterval));
    }

    @Test
    public void shouldUseOwnOderChangeoverDurationIfExist() {
        // given
        Interval firstOrderInterval = new Interval(SOME_DATE, SOME_DATE.plusHours(6));
        Entity firstOrder = mockOrderProjection(firstOrderInterval, LINE_1_ID, null, TECH_1_ID, TECH_GROUP_1_ID);

        Interval secondOrderInterval = new Interval(SOME_DATE.plusHours(8), SOME_DATE.plusHours(12));
        Entity secondOrder = mockOrderProjection(secondOrderInterval, LINE_1_ID, minutesAsSeconds(10), TECH_2_ID, TECH_GROUP_2_ID);

        stubModelHelper(firstOrder, secondOrder);

        stubChangeoverSearchResults(TECH_1_ID, TECH_GROUP_1_ID, TECH_2_ID, TECH_GROUP_2_ID, LINE_1_ID, minutesAsSeconds(30));

        // when
        Multimap<Long, Interval> resMap = orderAndChangeoverIntervalsProvider.getIntervalsPerProductionLine(DEFAULT_CONTEXT);

        // then
        verify(changeoverNormsSearchService, never()).findBestMatching(anyLong(), anyLong(), anyLong(), anyLong(), anyLong());

        Assert.assertEquals(1, resMap.keySet().size());
        Assert.assertTrue(resMap.containsKey(LINE_1_ID));

        Collection<Interval> line1intervals = resMap.get(LINE_1_ID);
        Assert.assertEquals(3, line1intervals.size());
        Assert.assertTrue(line1intervals.contains(firstOrderInterval));
        Assert.assertTrue(line1intervals.contains(secondOrderInterval));
        Assert.assertTrue(line1intervals.contains(changeoverBefore(secondOrderInterval, 10)));
    }

    @Test
    public void shouldNotAppendChangeoversForOrdersCollision() {
        // given
        Interval firstOrderInterval = new Interval(SOME_DATE, SOME_DATE.plusHours(6));
        Entity firstOrder = mockOrderProjection(firstOrderInterval, LINE_1_ID, null, TECH_1_ID, TECH_GROUP_1_ID);

        Interval secondOrderInterval = new Interval(SOME_DATE.plusHours(5), SOME_DATE.plusHours(12));
        Entity secondOrder = mockOrderProjection(secondOrderInterval, LINE_1_ID, minutesAsSeconds(10), TECH_2_ID, TECH_GROUP_2_ID);

        stubModelHelper(firstOrder, secondOrder);

        stubChangeoverSearchResults(TECH_1_ID, TECH_GROUP_1_ID, TECH_2_ID, TECH_GROUP_2_ID, LINE_1_ID, minutesAsSeconds(30));

        // when
        Multimap<Long, Interval> resMap = orderAndChangeoverIntervalsProvider.getIntervalsPerProductionLine(DEFAULT_CONTEXT);

        // then
        verify(changeoverNormsSearchService, never()).findBestMatching(anyLong(), anyLong(), anyLong(), anyLong(), anyLong());

        Collection<Interval> line1intervals = resMap.get(LINE_1_ID);
        Assert.assertEquals(2, line1intervals.size());
        Assert.assertTrue(line1intervals.contains(firstOrderInterval));
        Assert.assertTrue(line1intervals.contains(secondOrderInterval));
    }

    @Test
    public void shouldNotAppendChangeoversForAbutsOrders() {
        // given
        Interval firstOrderInterval = new Interval(SOME_DATE, SOME_DATE.plusHours(6));
        Entity firstOrder = mockOrderProjection(firstOrderInterval, LINE_1_ID, null, TECH_1_ID, TECH_GROUP_1_ID);

        Interval secondOrderInterval = new Interval(SOME_DATE.plusHours(6), SOME_DATE.plusHours(12));
        Entity secondOrder = mockOrderProjection(secondOrderInterval, LINE_1_ID, minutesAsSeconds(10), TECH_2_ID, TECH_GROUP_2_ID);

        stubModelHelper(firstOrder, secondOrder);

        stubChangeoverSearchResults(TECH_1_ID, TECH_GROUP_1_ID, TECH_2_ID, TECH_GROUP_2_ID, LINE_1_ID, minutesAsSeconds(30));

        // when
        Multimap<Long, Interval> resMap = orderAndChangeoverIntervalsProvider.getIntervalsPerProductionLine(DEFAULT_CONTEXT);

        // then
        verify(changeoverNormsSearchService, never()).findBestMatching(anyLong(), anyLong(), anyLong(), anyLong(), anyLong());

        Collection<Interval> line1intervals = resMap.get(LINE_1_ID);
        Assert.assertEquals(2, line1intervals.size());
        Assert.assertTrue(line1intervals.contains(firstOrderInterval));
        Assert.assertTrue(line1intervals.contains(secondOrderInterval));
    }

    @Test
    public void shouldFillGapBetweenManyOrdersWithTheSameEndDateAndOneFollowingOrder() {
        // given
        Interval firstOrderInterval = new Interval(SOME_DATE.minusHours(6), SOME_DATE);
        Entity firstOrder = mockOrderProjection(firstOrderInterval, LINE_1_ID, null, TECH_1_ID, TECH_GROUP_1_ID);

        Interval secondOrderInterval = new Interval(SOME_DATE.minusHours(2), SOME_DATE);
        Entity secondOrder = mockOrderProjection(secondOrderInterval, LINE_1_ID, null, TECH_2_ID, TECH_GROUP_2_ID);

        Interval thirdOrderInterval = new Interval(SOME_DATE.minusHours(5), SOME_DATE);
        Entity thirdOrder = mockOrderProjection(thirdOrderInterval, LINE_1_ID, null, TECH_3_ID, TECH_GROUP_2_ID);

        Interval fourthOrderInterval = new Interval(SOME_DATE.plusHours(2), SOME_DATE.plusHours(6));
        Entity fourthOrder = mockOrderProjection(fourthOrderInterval, LINE_1_ID, null, TECH_1_ID, TECH_GROUP_1_ID);

        stubModelHelper(firstOrder, secondOrder, thirdOrder, fourthOrder);

        stubChangeoverSearchResults(TECH_1_ID, TECH_GROUP_1_ID, TECH_1_ID, TECH_GROUP_1_ID, LINE_1_ID, minutesAsSeconds(30));
        stubChangeoverSearchResults(TECH_2_ID, TECH_GROUP_2_ID, TECH_1_ID, TECH_GROUP_1_ID, LINE_1_ID, minutesAsSeconds(60));
        stubChangeoverSearchResults(TECH_3_ID, TECH_GROUP_2_ID, TECH_1_ID, TECH_GROUP_1_ID, LINE_1_ID, minutesAsSeconds(90));

        // when
        Multimap<Long, Interval> resMap = orderAndChangeoverIntervalsProvider.getIntervalsPerProductionLine(DEFAULT_CONTEXT);

        // then
        verify(changeoverNormsSearchService).findBestMatching(TECH_1_ID, TECH_GROUP_1_ID, TECH_1_ID, TECH_GROUP_1_ID, LINE_1_ID);
        verify(changeoverNormsSearchService).findBestMatching(TECH_2_ID, TECH_GROUP_2_ID, TECH_1_ID, TECH_GROUP_1_ID, LINE_1_ID);
        verify(changeoverNormsSearchService).findBestMatching(TECH_3_ID, TECH_GROUP_2_ID, TECH_1_ID, TECH_GROUP_1_ID, LINE_1_ID);
        verifyNoMoreInteractions(changeoverNormsSearchService);

        Collection<Interval> line1intervals = resMap.get(LINE_1_ID);
        Assert.assertEquals(7, line1intervals.size());
        Assert.assertTrue(line1intervals.contains(firstOrderInterval));
        Assert.assertTrue(line1intervals.contains(secondOrderInterval));
        Assert.assertTrue(line1intervals.contains(thirdOrderInterval));
        Assert.assertTrue(line1intervals.contains(fourthOrderInterval));

        Assert.assertTrue(line1intervals.contains(changeoverBefore(fourthOrderInterval, 30)));
        Assert.assertTrue(line1intervals.contains(changeoverBefore(fourthOrderInterval, 60)));
        Assert.assertTrue(line1intervals.contains(changeoverBefore(fourthOrderInterval, 90)));
    }

    @Test
    public void shouldFillGapBetweenOrderAndManyFollowingOrderWithTheSameStartDates() {
        // given
        Interval firstOrderInterval = new Interval(SOME_DATE.minusHours(4), SOME_DATE.minusHours(2));
        Entity firstOrder = mockOrderProjection(firstOrderInterval, LINE_1_ID, null, TECH_1_ID, TECH_GROUP_1_ID);

        Interval secondOrderInterval = new Interval(SOME_DATE, SOME_DATE.plusHours(2));
        Entity secondOrder = mockOrderProjection(secondOrderInterval, LINE_1_ID, null, TECH_2_ID, TECH_GROUP_2_ID);

        Interval thirdOrderInterval = new Interval(SOME_DATE, SOME_DATE.plusHours(4));
        Entity thirdOrder = mockOrderProjection(thirdOrderInterval, LINE_1_ID, null, TECH_3_ID, TECH_GROUP_2_ID);

        Interval fourthOrderInterval = new Interval(SOME_DATE, SOME_DATE.plusHours(6));
        Entity fourthOrder = mockOrderProjection(fourthOrderInterval, LINE_1_ID, null, TECH_1_ID, TECH_GROUP_1_ID);

        stubModelHelper(firstOrder, secondOrder, thirdOrder, fourthOrder);

        stubChangeoverSearchResults(TECH_1_ID, TECH_GROUP_1_ID, TECH_2_ID, TECH_GROUP_2_ID, LINE_1_ID, minutesAsSeconds(30));
        stubChangeoverSearchResults(TECH_1_ID, TECH_GROUP_1_ID, TECH_3_ID, TECH_GROUP_2_ID, LINE_1_ID, minutesAsSeconds(60));
        stubChangeoverSearchResults(TECH_1_ID, TECH_GROUP_1_ID, TECH_1_ID, TECH_GROUP_1_ID, LINE_1_ID, minutesAsSeconds(90));

        // when
        Multimap<Long, Interval> resMap = orderAndChangeoverIntervalsProvider.getIntervalsPerProductionLine(DEFAULT_CONTEXT);

        // then
        verify(changeoverNormsSearchService).findBestMatching(TECH_1_ID, TECH_GROUP_1_ID, TECH_1_ID, TECH_GROUP_1_ID, LINE_1_ID);
        verify(changeoverNormsSearchService).findBestMatching(TECH_1_ID, TECH_GROUP_1_ID, TECH_2_ID, TECH_GROUP_2_ID, LINE_1_ID);
        verify(changeoverNormsSearchService).findBestMatching(TECH_1_ID, TECH_GROUP_1_ID, TECH_3_ID, TECH_GROUP_2_ID, LINE_1_ID);
        verifyNoMoreInteractions(changeoverNormsSearchService);

        Collection<Interval> line1intervals = resMap.get(LINE_1_ID);
        Assert.assertEquals(7, line1intervals.size());
        Assert.assertTrue(line1intervals.contains(firstOrderInterval));
        Assert.assertTrue(line1intervals.contains(secondOrderInterval));
        Assert.assertTrue(line1intervals.contains(thirdOrderInterval));
        Assert.assertTrue(line1intervals.contains(fourthOrderInterval));

        Assert.assertTrue(line1intervals.contains(changeoverBefore(secondOrderInterval, 30)));
        Assert.assertTrue(line1intervals.contains(changeoverBefore(secondOrderInterval, 60)));
        Assert.assertTrue(line1intervals.contains(changeoverBefore(secondOrderInterval, 90)));
    }

    @Test
    public void shouldFillGapBetweenManyOrdersWithTheSameEndDateAndManyFollowingOrderWithTheameStartDates() {
        // given
        Interval firstOrderInterval = new Interval(SOME_DATE.minusHours(4), SOME_DATE.minusHours(2));
        Entity firstOrder = mockOrderProjection(firstOrderInterval, LINE_1_ID, null, TECH_1_ID, TECH_GROUP_1_ID);

        Interval secondOrderInterval = new Interval(SOME_DATE.minusHours(6), SOME_DATE.minusHours(2));
        Entity secondOrder = mockOrderProjection(secondOrderInterval, LINE_1_ID, null, TECH_2_ID, TECH_GROUP_2_ID);

        Interval thirdOrderInterval = new Interval(SOME_DATE, SOME_DATE.plusHours(4));
        Entity thirdOrder = mockOrderProjection(thirdOrderInterval, LINE_1_ID, null, TECH_3_ID, TECH_GROUP_2_ID);

        Interval fourthOrderInterval = new Interval(SOME_DATE, SOME_DATE.plusHours(6));
        Entity fourthOrder = mockOrderProjection(fourthOrderInterval, LINE_1_ID, null, TECH_1_ID, TECH_GROUP_1_ID);

        stubModelHelper(firstOrder, secondOrder, thirdOrder, fourthOrder);

        stubChangeoverSearchResults(TECH_1_ID, TECH_GROUP_1_ID, TECH_3_ID, TECH_GROUP_2_ID, LINE_1_ID, minutesAsSeconds(30));
        stubChangeoverSearchResults(TECH_1_ID, TECH_GROUP_1_ID, TECH_1_ID, TECH_GROUP_1_ID, LINE_1_ID, minutesAsSeconds(60));
        stubChangeoverSearchResults(TECH_2_ID, TECH_GROUP_2_ID, TECH_3_ID, TECH_GROUP_2_ID, LINE_1_ID, minutesAsSeconds(90));
        stubChangeoverSearchResults(TECH_2_ID, TECH_GROUP_2_ID, TECH_1_ID, TECH_GROUP_1_ID, LINE_1_ID, minutesAsSeconds(10));

        // when
        Multimap<Long, Interval> resMap = orderAndChangeoverIntervalsProvider.getIntervalsPerProductionLine(DEFAULT_CONTEXT);

        // then
        verify(changeoverNormsSearchService).findBestMatching(TECH_1_ID, TECH_GROUP_1_ID, TECH_3_ID, TECH_GROUP_2_ID, LINE_1_ID);
        verify(changeoverNormsSearchService).findBestMatching(TECH_1_ID, TECH_GROUP_1_ID, TECH_1_ID, TECH_GROUP_1_ID, LINE_1_ID);
        verify(changeoverNormsSearchService).findBestMatching(TECH_2_ID, TECH_GROUP_2_ID, TECH_3_ID, TECH_GROUP_2_ID, LINE_1_ID);
        verify(changeoverNormsSearchService).findBestMatching(TECH_2_ID, TECH_GROUP_2_ID, TECH_1_ID, TECH_GROUP_1_ID, LINE_1_ID);
        verifyNoMoreInteractions(changeoverNormsSearchService);

        Collection<Interval> line1intervals = resMap.get(LINE_1_ID);
        Assert.assertEquals(8, line1intervals.size());
        Assert.assertTrue(line1intervals.contains(firstOrderInterval));
        Assert.assertTrue(line1intervals.contains(secondOrderInterval));
        Assert.assertTrue(line1intervals.contains(thirdOrderInterval));
        Assert.assertTrue(line1intervals.contains(fourthOrderInterval));

        Assert.assertTrue(line1intervals.contains(changeoverBefore(thirdOrderInterval, 10)));
        Assert.assertTrue(line1intervals.contains(changeoverBefore(thirdOrderInterval, 30)));
        Assert.assertTrue(line1intervals.contains(changeoverBefore(thirdOrderInterval, 60)));
        Assert.assertTrue(line1intervals.contains(changeoverBefore(thirdOrderInterval, 90)));
    }

    @Test
    public void shouldAppendChangeoverBeforeFirstOrderButCutOutOrderEndingBeforeSearchIntervalBegins() {
        // given
        Interval orderBeforeInterval = new Interval(SEARCH_DOMAIN_INTERVAL.getStart().minusHours(3), SEARCH_DOMAIN_INTERVAL
                .getStart().minusHours(1));
        Entity orderBefore = mockOrderProjection(orderBeforeInterval, LINE_1_ID, null, TECH_2_ID, TECH_GROUP_2_ID);

        Interval firstOrderInterval = new Interval(SOME_DATE.minusHours(4), SOME_DATE.minusHours(2));
        Entity firstOrder = mockOrderProjection(firstOrderInterval, LINE_1_ID, null, TECH_1_ID, TECH_GROUP_1_ID);

        Interval secondOrderInterval = new Interval(SOME_DATE, SOME_DATE.plusHours(2));
        Entity secondOrder = mockOrderProjection(secondOrderInterval, LINE_1_ID, null, TECH_2_ID, TECH_GROUP_2_ID);

        stubModelHelper(orderBefore, firstOrder, secondOrder);

        stubChangeoverSearchResults(TECH_1_ID, TECH_GROUP_1_ID, TECH_2_ID, TECH_GROUP_2_ID, LINE_1_ID, minutesAsSeconds(30));
        stubChangeoverSearchResults(TECH_2_ID, TECH_GROUP_2_ID, TECH_1_ID, TECH_GROUP_1_ID, LINE_1_ID, minutesAsSeconds(60));

        // when
        Multimap<Long, Interval> resMap = orderAndChangeoverIntervalsProvider.getIntervalsPerProductionLine(DEFAULT_CONTEXT);

        // then
        verify(changeoverNormsSearchService).findBestMatching(TECH_1_ID, TECH_GROUP_1_ID, TECH_2_ID, TECH_GROUP_2_ID, LINE_1_ID);
        verify(changeoverNormsSearchService).findBestMatching(TECH_2_ID, TECH_GROUP_2_ID, TECH_1_ID, TECH_GROUP_1_ID, LINE_1_ID);
        verifyNoMoreInteractions(changeoverNormsSearchService);

        Collection<Interval> line1intervals = resMap.get(LINE_1_ID);
        Assert.assertEquals(4, line1intervals.size());
        Assert.assertTrue(line1intervals.contains(firstOrderInterval));
        Assert.assertTrue(line1intervals.contains(secondOrderInterval));
        Assert.assertTrue(line1intervals.contains(changeoverBefore(firstOrderInterval, 60)));
        Assert.assertTrue(line1intervals.contains(changeoverBefore(secondOrderInterval, 30)));
    }

    @Test
    public void shouldAppendChangeoverAfterLastOrderButCutOutOrderStartingAfterSearchIntervalEnds() {
        // given
        Interval firstOrderInterval = new Interval(SOME_DATE.minusHours(4), SOME_DATE.minusHours(2));
        Entity firstOrder = mockOrderProjection(firstOrderInterval, LINE_1_ID, null, TECH_1_ID, TECH_GROUP_1_ID);

        Interval secondOrderInterval = new Interval(SOME_DATE, SOME_DATE.plusHours(2));
        Entity secondOrder = mockOrderProjection(secondOrderInterval, LINE_1_ID, null, TECH_2_ID, TECH_GROUP_2_ID);

        Interval orderAfterInterval = new Interval(SEARCH_DOMAIN_INTERVAL.getEnd().plusHours(1), SEARCH_DOMAIN_INTERVAL.getEnd()
                .plusHours(3));
        Entity orderAfter = mockOrderProjection(orderAfterInterval, LINE_1_ID, null, TECH_2_ID, TECH_GROUP_2_ID);

        stubModelHelper(firstOrder, secondOrder, orderAfter);

        stubChangeoverSearchResults(TECH_1_ID, TECH_GROUP_1_ID, TECH_2_ID, TECH_GROUP_2_ID, LINE_1_ID, minutesAsSeconds(30));
        stubChangeoverSearchResults(TECH_2_ID, TECH_GROUP_2_ID, TECH_2_ID, TECH_GROUP_2_ID, LINE_1_ID, minutesAsSeconds(90));

        // when
        Multimap<Long, Interval> resMap = orderAndChangeoverIntervalsProvider.getIntervalsPerProductionLine(DEFAULT_CONTEXT);

        // then
        verify(changeoverNormsSearchService).findBestMatching(TECH_1_ID, TECH_GROUP_1_ID, TECH_2_ID, TECH_GROUP_2_ID, LINE_1_ID);
        verify(changeoverNormsSearchService).findBestMatching(TECH_2_ID, TECH_GROUP_2_ID, TECH_2_ID, TECH_GROUP_2_ID, LINE_1_ID);
        verifyNoMoreInteractions(changeoverNormsSearchService);

        Collection<Interval> line1intervals = resMap.get(LINE_1_ID);
        Assert.assertEquals(4, line1intervals.size());
        Assert.assertTrue(line1intervals.contains(firstOrderInterval));
        Assert.assertTrue(line1intervals.contains(secondOrderInterval));
        Assert.assertTrue(line1intervals.contains(changeoverBefore(orderAfterInterval, 90)));
        Assert.assertTrue(line1intervals.contains(changeoverBefore(secondOrderInterval, 30)));
    }

    @Test
    public void shouldNotTryToCalculateChangeoverIfTheBothOrdersDoesNotHaveTechnology() {
        // given
        Interval firstOrderInterval = new Interval(SOME_DATE.minusHours(4), SOME_DATE.minusHours(2));
        Entity firstOrder = mockOrderProjection(firstOrderInterval, LINE_1_ID, null, null, null);

        Interval secondOrderInterval = new Interval(SOME_DATE, SOME_DATE.plusHours(2));
        Entity secondOrder = mockOrderProjection(secondOrderInterval, LINE_1_ID, null, null, null);

        stubModelHelper(firstOrder, secondOrder);

        // when
        Multimap<Long, Interval> resMap = orderAndChangeoverIntervalsProvider.getIntervalsPerProductionLine(DEFAULT_CONTEXT);

        // then
        verifyZeroInteractions(changeoverNormsSearchService);
        verifyNoMoreInteractions(changeoverNormsSearchService);

        Collection<Interval> line1intervals = resMap.get(LINE_1_ID);
        Assert.assertEquals(2, line1intervals.size());
        Assert.assertTrue(line1intervals.contains(firstOrderInterval));
        Assert.assertTrue(line1intervals.contains(secondOrderInterval));
    }

    @Test
    public void shouldNotTryToCalculateChangeoverIfFirstOfTheOrdersDoesNotHaveTechnology() {
        // given
        Interval firstOrderInterval = new Interval(SOME_DATE.minusHours(4), SOME_DATE.minusHours(2));
        Entity firstOrder = mockOrderProjection(firstOrderInterval, LINE_1_ID, null, null, null);

        Interval secondOrderInterval = new Interval(SOME_DATE, SOME_DATE.plusHours(2));
        Entity secondOrder = mockOrderProjection(secondOrderInterval, LINE_1_ID, null, TECH_2_ID, TECH_GROUP_2_ID);

        stubModelHelper(firstOrder, secondOrder);

        // when
        Multimap<Long, Interval> resMap = orderAndChangeoverIntervalsProvider.getIntervalsPerProductionLine(DEFAULT_CONTEXT);

        // then
        verifyZeroInteractions(changeoverNormsSearchService);
        verifyNoMoreInteractions(changeoverNormsSearchService);

        Collection<Interval> line1intervals = resMap.get(LINE_1_ID);
        Assert.assertEquals(2, line1intervals.size());
        Assert.assertTrue(line1intervals.contains(firstOrderInterval));
        Assert.assertTrue(line1intervals.contains(secondOrderInterval));
    }

    @Test
    public void shouldNotTryToCalculateChangeoverIfSecondOfTheOrdersDoesNotHaveTechnology() {
        // given
        Interval firstOrderInterval = new Interval(SOME_DATE.minusHours(4), SOME_DATE.minusHours(2));
        Entity firstOrder = mockOrderProjection(firstOrderInterval, LINE_1_ID, null, TECH_1_ID, TECH_GROUP_1_ID);

        Interval secondOrderInterval = new Interval(SOME_DATE, SOME_DATE.plusHours(2));
        Entity secondOrder = mockOrderProjection(secondOrderInterval, LINE_1_ID, null, null, null);

        stubModelHelper(firstOrder, secondOrder);

        // when
        Multimap<Long, Interval> resMap = orderAndChangeoverIntervalsProvider.getIntervalsPerProductionLine(DEFAULT_CONTEXT);

        // then
        verifyZeroInteractions(changeoverNormsSearchService);
        verifyNoMoreInteractions(changeoverNormsSearchService);

        Collection<Interval> line1intervals = resMap.get(LINE_1_ID);
        Assert.assertEquals(2, line1intervals.size());
        Assert.assertTrue(line1intervals.contains(firstOrderInterval));
        Assert.assertTrue(line1intervals.contains(secondOrderInterval));
    }
}
