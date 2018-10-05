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

import static com.qcadoo.model.api.search.SearchRestrictions.*;

import java.util.Date;
import java.util.List;

import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.qcadoo.mes.timeGapsPreview.TimeGapsContext;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.*;

public abstract class AbstractIntervalsProvider implements IntervalsProvider {

    private static final String DATE_FROM_ALIAS = "dateFrom_a";

    private static final String DATE_TO_ALIAS = "dateTo_a";

    private static final String PRODUCTION_LINE_ID_ALIAS = "productionLineId_a";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    protected abstract String getPluginIdentifier();

    protected abstract String getModelName();

    protected abstract String getDateFromFieldName();

    protected abstract String getDateToFieldName();

    protected abstract String getProductionLineFieldName();

    @Override
    public Multimap<Long, Interval> getIntervalsPerProductionLine(final TimeGapsContext context) {
        Multimap<Long, Interval> intervals = HashMultimap.create();
        for (Entity datesProjection : getDatesProjection(context)) {
            Date from = datesProjection.getDateField(DATE_FROM_ALIAS);
            Date to = datesProjection.getDateField(DATE_TO_ALIAS);
            Long lineId = (Long) datesProjection.getField(PRODUCTION_LINE_ID_ALIAS);
            Interval interval = new Interval(from.getTime(), to.getTime());
            intervals.put(lineId, interval);
        }
        return Multimaps.unmodifiableMultimap(intervals);
    }

    protected SearchCriterion getSearchCriterion() {
        return null;
    }

    private List<Entity> getDatesProjection(final TimeGapsContext context) {
        DataDefinition dataDef = dataDefinitionService.get(getPluginIdentifier(), getModelName());
        SearchCriteriaBuilder scb = dataDef.find();
        SearchCriterion restriction = getSearchCriterion();
        if (restriction != null) {
            scb.add(restriction);
        }

        scb.add(SearchRestrictions.isNotNull(getDateFromFieldName()));
        scb.add(SearchRestrictions.isNotNull(getDateToFieldName()));
        scb.add(SearchRestrictions.isNotNull(getProductionLineFieldName()));

        scb.createCriteria(getProductionLineFieldName(), getProductionLineFieldName())
                .add(in("id", context.getProductionLines()));

        Interval searchInterval = context.getInterval();
        SearchCriterion startDateIsInDomain = between(getDateFromFieldName(), searchInterval.getStart().toDate(), searchInterval
                .getEnd().toDate());
        SearchCriterion finishDateIsInDomain = between(getDateToFieldName(), searchInterval.getStart().toDate(), searchInterval
                .getEnd().toDate());
        SearchCriterion datesCoverInterval = and(le(getDateFromFieldName(), searchInterval.getStart().toDate()),
                ge(getDateToFieldName(), searchInterval.getEnd().toDate()));

        scb.add(or(startDateIsInDomain, finishDateIsInDomain, datesCoverInterval));

        SearchProjection lineIdProjection = SearchProjections.alias(
                SearchProjections.field(getProductionLineFieldName() + ".id"), PRODUCTION_LINE_ID_ALIAS);
        SearchProjection dateFromProjection = SearchProjections.alias(SearchProjections.field(getDateFromFieldName()),
                DATE_FROM_ALIAS);
        SearchProjection dateToProjection = SearchProjections.alias(SearchProjections.field(getDateToFieldName()), DATE_TO_ALIAS);
        scb.setProjection(SearchProjections.list().add(dateFromProjection).add(dateToProjection).add(lineIdProjection));
        scb.addOrder(SearchOrders.asc(DATE_FROM_ALIAS));
        return scb.list().getEntities();
    }

}
