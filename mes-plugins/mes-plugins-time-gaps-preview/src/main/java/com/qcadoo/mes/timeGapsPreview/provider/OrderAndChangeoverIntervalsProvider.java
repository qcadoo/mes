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

import java.util.Date;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
import com.qcadoo.mes.lineChangeoverNorms.ChangeoverNormsSearchService;
import com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields;
import com.qcadoo.mes.lineChangeoverNormsForOrders.constants.OrderFieldsLCNFO;
import com.qcadoo.mes.timeGapsPreview.TimeGapsContext;
import com.qcadoo.mes.timeGapsPreview.provider.helper.OrderIntervalsModelHelper;
import com.qcadoo.mes.timeGapsPreview.util.IntervalsComparator;
import com.qcadoo.model.api.Entity;

@Service
public class OrderAndChangeoverIntervalsProvider implements IntervalsProvider {

    @Autowired
    private ChangeoverNormsSearchService changeoverNormsSearchService;

    @Autowired
    private OrderIntervalsModelHelper orderIntervalsModelHelper;

    @Override
    public Multimap<Long, Interval> getIntervalsPerProductionLine(final TimeGapsContext context) {
        Multimap<Long, Interval> intervals = HashMultimap.create();
        Set<Order> orders = buildOrders(orderIntervalsModelHelper.getOrdersProjection(context));
        intervals.putAll(ordersToIntervals(orders));
        Multimap<Long, Interval> changeoverIntervals = getChangeovers(getGaps(groupedByStartDate(orders)));
        intervals.putAll(changeoverIntervals);
        Multimap<Long, Interval> filteredIntervals = Multimaps.filterValues(intervals, new Predicate<Interval>() {

            @Override
            public boolean apply(final Interval interval) {
                return context.getInterval().overlaps(interval);
            }
        });
        return Multimaps.unmodifiableMultimap(filteredIntervals);
    }

    private Set<Order> buildOrders(final Iterable<Entity> projections) {
        Set<Order> orders = Sets.newHashSet();
        for (Entity projection : projections) {
            orders.add(Order.from(projection));
        }
        return orders;
    }

    private Multimap<Long, Interval> ordersToIntervals(final Iterable<Order> orders) {
        Multimap<Long, Interval> ordersByLine = HashMultimap.create();
        for (Order order : orders) {
            Long lineId = order.productionLineId;
            ordersByLine.put(lineId, order.interval);
        }
        return ordersByLine;
    }

    private TreeMultimap<DateTime, Order> groupedByStartDate(final Set<Order> orders) {
        return TreeMultimap.create(Multimaps.index(orders, new Function<Order, DateTime>() {

            @Override
            public DateTime apply(final Order order) {
                if (order == null) {
                    return null;
                }
                return order.interval.getStart();
            }
        }));
    }

    private Multimap<Long, OrdersGap> getGaps(final TreeMultimap<DateTime, Order> ordersByStartTime) {
        Multimap<Long, OrdersGap> gaps = HashMultimap.create();
        NavigableSet<DateTime> keys = ordersByStartTime.keySet();
        for (Map.Entry<DateTime, Order> startTimeAndOrder : ordersByStartTime.entries()) {
            Order fromOrder = startTimeAndOrder.getValue();
            DateTime end = fromOrder.interval.getEnd();
            DateTime closestNextOrderStartDate = keys.higher(end);
            if (closestNextOrderStartDate == null) {
                continue;
            }
            for (Order toOrder : ordersByStartTime.get(closestNextOrderStartDate)) {
                if (ObjectUtils.equals(fromOrder.productionLineId, toOrder.productionLineId)) {
                    gaps.put(fromOrder.productionLineId, OrdersGap.between(fromOrder, toOrder));
                }
            }
        }
        return gaps;
    }

    // TODO it can be optimized by searching only the longest changeover between orders listed in given orderGaps set.
    private Multimap<Long, Interval> getChangeovers(final Multimap<Long, OrdersGap> orderGaps) {
        return Multimaps.transformValues(orderGaps, new Function<OrdersGap, Interval>() {

            @Override
            public Interval apply(final OrdersGap orderGap) {
                return gapToInterval(orderGap);
            }
        });
    }

    private Interval gapToInterval(final OrdersGap orderGap) {
        Integer ownChangeoverDuration = orderGap.to.ownLineChangeoverDuration;
        if (ownChangeoverDuration != null) {
            DateTime end = orderGap.to.interval.getStart();
            return new Interval(end.minusSeconds(ownChangeoverDuration), end);
        } else {
            Long fromTechId = orderGap.from.technologyId;
            Long fromTechGroupId = orderGap.from.technologyGroupId;
            Long toTechId = orderGap.to.technologyId;
            Long toTechGroupId = orderGap.to.technologyGroupId;
            Long productionLineId = orderGap.to.productionLineId;

            if (fromTechId == null || toTechId == null) {
                return null;
            }
            Entity maybeChangeover = changeoverNormsSearchService.findBestMatching(fromTechId, fromTechGroupId, toTechId,
                    toTechGroupId, productionLineId);
            if (maybeChangeover == null) {
                return null;
            }
            return changeoverToInterval(maybeChangeover, orderGap.to.interval.getStart());
        }
    }

    private Interval changeoverToInterval(final Entity changeover, final DateTime endDate) {
        Integer durationSeconds = changeover.getIntegerField(LineChangeoverNormsFields.DURATION);
        DateTime startDate;
        if (durationSeconds > 0) {
            startDate = endDate.minusSeconds(durationSeconds);
        } else {
            startDate = endDate;
        }
        return new Interval(startDate, endDate);
    }

    protected static class OrdersGap {

        private final Order from;

        private final Order to;

        public static OrdersGap between(final Order fromOrder, final Order toOrder) {
            return new OrdersGap(fromOrder, toOrder);
        }

        private OrdersGap(final Order from, final Order to) {
            Preconditions.checkArgument(from != null, "order must be not null.");
            Preconditions.checkArgument(to != null, "order must be not null.");
            this.from = from;
            this.to = to;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            OrdersGap ordersGap = (OrdersGap) o;
            return new EqualsBuilder().append(from, ordersGap.from).append(to, ordersGap.to).isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(from).append(to).toHashCode();
        }
    }

    protected static class Order implements Comparable<Order> {

        private final Interval interval;

        private final Long technologyId;

        private final Long technologyGroupId;

        private final Integer ownLineChangeoverDuration;

        private final Long productionLineId;

        public static Order from(final Entity projection) {
            Date dateFrom = projection.getDateField(OrderIntervalsModelHelper.DATE_FROM_ALIAS);
            Date dateTo = projection.getDateField(OrderIntervalsModelHelper.DATE_TO_ALIAS);
            Interval interval = new Interval(dateFrom.getTime(), dateTo.getTime());
            Long technologyId = (Long) projection.getField(OrderIntervalsModelHelper.TECHNOLOGY_ID_ALIAS);
            Long technologyGroupId = (Long) projection.getField(OrderIntervalsModelHelper.TECHNOLOGY_GROUP_ID_ALIAS);
            Integer ownLineChangeoverDuration = projection.getIntegerField(OrderFieldsLCNFO.OWN_LINE_CHANGEOVER_DURATION);
            Long productionLineId = (Long) projection.getField(OrderIntervalsModelHelper.PRODUCTION_LINE_ID_ALIAS);
            return new Order(interval, technologyId, technologyGroupId, productionLineId, ownLineChangeoverDuration);
        }

        private Order(final Interval interval, final Long technologyId, final Long technologyGroupId,
                final Long productionLineId, final Integer ownLineChangeoverDuration) {
            Preconditions.checkArgument(interval != null, "interval must be not null.");
            Preconditions.checkArgument(productionLineId != null, "productionLineId must be not null.");
            this.interval = interval;
            this.technologyId = technologyId;
            this.technologyGroupId = technologyGroupId;
            this.ownLineChangeoverDuration = ownLineChangeoverDuration;
            this.productionLineId = productionLineId;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Order order = (Order) o;
            return new EqualsBuilder().append(technologyId, order.technologyId)
                    .append(technologyGroupId, order.technologyGroupId).append(interval, order.interval)
                    .append(ownLineChangeoverDuration, order.ownLineChangeoverDuration).isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(technologyId).append(technologyGroupId).append(interval)
                    .append(ownLineChangeoverDuration).toHashCode();
        }

        @Override
        public int compareTo(final Order other) {
            if (ObjectUtils.equals(this, other)) {
                return 0;
            }
            return IntervalsComparator.START_DATE_ASC_AND_DURATION_DESC.compare(this.interval, other.interval);
        }
    }

}
