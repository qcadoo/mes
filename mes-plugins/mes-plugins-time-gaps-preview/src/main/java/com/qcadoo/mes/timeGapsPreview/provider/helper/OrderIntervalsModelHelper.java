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
package com.qcadoo.mes.timeGapsPreview.provider.helper;

import static com.qcadoo.model.api.search.SearchOrders.asc;
import static com.qcadoo.model.api.search.SearchProjections.alias;
import static com.qcadoo.model.api.search.SearchProjections.field;
import static com.qcadoo.model.api.search.SearchRestrictions.*;

import java.util.*;

import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.lineChangeoverNormsForOrders.constants.OrderFieldsLCNFO;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.states.constants.OrderStateStringValues;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.timeGapsPreview.TimeGapsContext;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.*;

@Service
public class OrderIntervalsModelHelper {

    private static final Collection<String> IGNORED_STATES = Collections.unmodifiableList(Lists.newArrayList(
            OrderStateStringValues.ABANDONED, OrderStateStringValues.DECLINED));

    public static final String DATE_FROM_ALIAS = "dateFrom_a";

    public static final String DATE_TO_ALIAS = "dateTo_a";

    public static final String PRODUCTION_LINE_ID_ALIAS = "productionLineId_a";

    public static final String TECHNOLOGY_ID_ALIAS = "technologyId_a";

    public static final String TECHNOLOGY_GROUP_ID_ALIAS = "technologyGroupId_a";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public List<Entity> getOrdersProjection(final TimeGapsContext context) {
        DataDefinition orderDD = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER);
        List<Entity> orderProjections = findOverlapOrders(orderDD, context);
        orderProjections.addAll(findNearestOrdersBeforeInterval(orderDD, context));
        orderProjections.addAll(findNearestOrdersAfterInterval(orderDD, context));
        return orderProjections;
    }

    private Date findFinishDateOfLastOrderBeforeInterval(final DataDefinition orderDD, final TimeGapsContext context) {
        SearchCriteriaBuilder scb = orderDD.find();
        scb.setProjection(alias(field(OrderFields.FINISH_DATE), OrderFields.FINISH_DATE));
        addProductionLineDomainCriteria(scb, context.getProductionLines());
        scb.add(buildCommonCriterion());
        scb.add(le(OrderFields.FINISH_DATE, context.getInterval().getStart().toDate()));
        scb.addOrder(SearchOrders.desc(OrderFields.FINISH_DATE));
        Entity projection = scb.setMaxResults(1).uniqueResult();
        if (projection == null) {
            return null;
        }
        return projection.getDateField(OrderFields.FINISH_DATE);
    }

    private Date findStartDateOfFirstOrderAfterInterval(final DataDefinition orderDD, final TimeGapsContext context) {
        SearchCriteriaBuilder scb = orderDD.find();
        scb.setProjection(alias(field(OrderFields.START_DATE), OrderFields.START_DATE));
        addProductionLineDomainCriteria(scb, context.getProductionLines());
        scb.add(buildCommonCriterion());
        scb.add(ge(OrderFields.START_DATE, context.getInterval().getEnd().toDate()));
        scb.addOrder(SearchOrders.asc(OrderFields.START_DATE));
        Entity projection = scb.setMaxResults(1).uniqueResult();
        if (projection == null) {
            return null;
        }
        return projection.getDateField(OrderFields.START_DATE);
    }

    private List<Entity> findNearestOrdersBeforeInterval(final DataDefinition orderDD, final TimeGapsContext context) {
        Date lastDateBefore = findFinishDateOfLastOrderBeforeInterval(orderDD, context);
        if (lastDateBefore == null) {
            return Collections.emptyList();
        }
        SearchCriteriaBuilder scb = prepareCriteriaBuilder(orderDD, context);
        scb.add(eq(OrderFields.FINISH_DATE, lastDateBefore));
        scb.addOrder(SearchOrders.desc(OrderFields.FINISH_DATE));
        return scb.list().getEntities();
    }

    private List<Entity> findNearestOrdersAfterInterval(final DataDefinition orderDD, final TimeGapsContext context) {
        Date firstDateAfter = findStartDateOfFirstOrderAfterInterval(orderDD, context);
        if (firstDateAfter == null) {
            return Collections.emptyList();
        }
        SearchCriteriaBuilder scb = prepareCriteriaBuilder(orderDD, context);
        scb.add(eq(OrderFields.START_DATE, firstDateAfter));
        scb.addOrder(SearchOrders.desc(OrderFields.START_DATE));
        return scb.list().getEntities();
    }

    private List<Entity> findOverlapOrders(final DataDefinition orderDD, final TimeGapsContext context) {
        SearchCriteriaBuilder scb = prepareCriteriaBuilder(orderDD, context);

        Interval searchInterval = context.getInterval();
        SearchCriterion startDateIsInDomain = between(OrderFields.START_DATE, searchInterval.getStart().toDate(), searchInterval
                .getEnd().toDate());
        SearchCriterion finishDateIsInDomain = between(OrderFields.FINISH_DATE, searchInterval.getStart().toDate(),
                searchInterval.getEnd().toDate());
        SearchCriterion orderDatesCoverInterval = and(le(OrderFields.START_DATE, searchInterval.getStart().toDate()),
                ge(OrderFields.FINISH_DATE, searchInterval.getEnd().toDate()));
        SearchCriterion orderOverlapsSearchDomain = or(startDateIsInDomain, finishDateIsInDomain, orderDatesCoverInterval);
        scb.add(orderOverlapsSearchDomain);

        scb.addOrder(asc(DATE_FROM_ALIAS));
        return scb.list().getEntities();
    }

    private SearchCriteriaBuilder prepareCriteriaBuilder(final DataDefinition orderDD, final TimeGapsContext context) {
        SearchCriteriaBuilder scb = orderDD.find();
        scb.setProjection(buildProjection());
        scb.add(buildCommonCriterion());
        addProductionLineDomainCriteria(scb, context.getProductionLines());
        scb.createAlias(OrderFields.TECHNOLOGY, OrderFields.TECHNOLOGY);
        return scb;
    }

    private void addProductionLineDomainCriteria(final SearchCriteriaBuilder scb, final Set<Long> productionLinesDomain) {
        scb.createCriteria(OrderFields.PRODUCTION_LINE, OrderFields.PRODUCTION_LINE).add(in("id", productionLinesDomain));
    }

    private SearchCriterion buildCommonCriterion() {
        return and(not(in(OrderFields.STATE, IGNORED_STATES)), isNotNull(OrderFields.START_DATE),
                isNotNull(OrderFields.FINISH_DATE), isNotNull(OrderFields.PRODUCTION_LINE));
    }

    private SearchProjection buildProjection() {
        SearchProjection lineIdProjection = alias(field(OrderFields.PRODUCTION_LINE + ".id"), PRODUCTION_LINE_ID_ALIAS);
        SearchProjection dateFromProjection = alias(field(OrderFields.START_DATE), DATE_FROM_ALIAS);
        SearchProjection dateToProjection = alias(field(OrderFields.FINISH_DATE), DATE_TO_ALIAS);
        SearchProjection technologyId = alias(field(OrderFields.TECHNOLOGY + ".id"), TECHNOLOGY_ID_ALIAS);
        SearchProjection technologyGroupId = alias(
                field(OrderFields.TECHNOLOGY + "." + TechnologyFields.TECHNOLOGY_GROUP + ".id"), TECHNOLOGY_GROUP_ID_ALIAS);
        SearchProjection ownChangeoverDurationProjection = alias(field(OrderFieldsLCNFO.OWN_LINE_CHANGEOVER_DURATION),
                OrderFieldsLCNFO.OWN_LINE_CHANGEOVER_DURATION);
        return SearchProjections.list().add(dateFromProjection).add(dateToProjection).add(lineIdProjection).add(technologyId)
                .add(technologyGroupId).add(ownChangeoverDurationProjection);
    }

}
