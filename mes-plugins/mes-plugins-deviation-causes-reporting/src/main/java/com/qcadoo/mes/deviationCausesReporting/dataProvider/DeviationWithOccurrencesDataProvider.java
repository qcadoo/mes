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
package com.qcadoo.mes.deviationCausesReporting.dataProvider;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrSubstitutor;
import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.qcadoo.mes.deviationCausesReporting.DeviationsReportCriteria;
import com.qcadoo.mes.deviationCausesReporting.constants.DeviationType;
import com.qcadoo.mes.deviationCausesReporting.domain.DeviationCauseHolderComparators;
import com.qcadoo.mes.deviationCausesReporting.domain.DeviationWithOccurrencesCount;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.deviationReasonTypes.DeviationModelDescriber;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchQueryBuilder;
import com.qcadoo.model.constants.QcadooModelConstants;

/**
 * Provider for deviations' occurrences count.
 * 
 * @since 1.3.0
 */
@Service
public class DeviationWithOccurrencesDataProvider {

    private static final String L_DATE_FROM = "dateFromPlaceholder";

    private static final String L_DATE_TO = "dateToPlaceholder";

    private static final String L_EXCLUDED_ORDER_STATES = "excludedOrderStates";

    private static final String PARTIAL_COUNT_SUB_QUERY_TPL = "(SELECT count(*) FROM #${MODEL_PLUGIN}_${MODEL_NAME} "
            + "WHERE ${REASON_TYPE_FIELD_NAME} = di.name AND date >= :" + L_DATE_FROM + " AND date <= :" + L_DATE_TO
            + " AND ${ORDER_PATH}.${ORDER_STATE} NOT IN (:" + L_EXCLUDED_ORDER_STATES + "))";

    private static final String TOTAL_OCCURRENCES_PROJECTION = prepareTotalOccurrencesForProblemProjection();

    private static String prepareTotalOccurrencesForProblemProjection() {
        Collection<String> subQueries = Collections2.transform(Arrays.asList(DeviationType.values()),
                new Function<DeviationType, String>() {

                    @Override
                    public String apply(final DeviationType deviationType) {
                        DeviationModelDescriber modelDescriber = deviationType.getModelDescriber();
                        HashMap<String, String> placeholderValues = Maps.newHashMap();
                        placeholderValues.put("MODEL_PLUGIN", modelDescriber.getModelPlugin());
                        placeholderValues.put("MODEL_NAME", modelDescriber.getModelName());
                        placeholderValues.put("REASON_TYPE_FIELD_NAME", modelDescriber.getReasonTypeFieldName());
                        placeholderValues.put("ORDER_PATH", deviationType.getPathToOrder());
                        placeholderValues.put("ORDER_STATE", OrderFields.STATE);
                        StrSubstitutor substitutor = new StrSubstitutor(placeholderValues, "${", "}");
                        return substitutor.replace(PARTIAL_COUNT_SUB_QUERY_TPL).toString();
                    }
                });
        return StringUtils.join(subQueries, " + ");
    }

    private static final String CAUSE_ALIAS = "causeProjectionAlias";

    private static final String TOTAL_COUNT_ALIAS = "totalCountProjectionAlias";

    private static final String ALL_PROBLEM_OCCURRENCES_QUERY = "SELECT di.name AS " + CAUSE_ALIAS + ", ("
            + TOTAL_OCCURRENCES_PROJECTION + ") AS " + TOTAL_COUNT_ALIAS
            + " FROM #qcadooModel_dictionaryItem AS di WHERE di.dictionary.name = 'reasonTypeOfChangingOrderState'";

    private static final Function<Entity, DeviationWithOccurrencesCount> DEVIATION_PROJECTION_TO_POJO = new Function<Entity, DeviationWithOccurrencesCount>() {

        @Override
        public DeviationWithOccurrencesCount apply(final Entity deviationProjection) {
            Long totalNumberOfOccurrences = (Long) deviationProjection.getField(TOTAL_COUNT_ALIAS);
            String cause = deviationProjection.getStringField(CAUSE_ALIAS);
            return new DeviationWithOccurrencesCount(cause, totalNumberOfOccurrences);
        }
    };

    @Autowired
    private DataDefinitionService dataDefinitionService;

    /**
     * Return a List of POJOs representing a pair of deviation's cause and number of its occurrences.
     * 
     * @param criteria
     *            restrictions for deviations to be summarized.
     * @return ImmutableList of POJOs representing a pair of deviation's cause and number of its occurrences.
     */
    public ImmutableList<DeviationWithOccurrencesCount> getDeviationsWithOccurrencesCount(final DeviationsReportCriteria criteria) {
        return FluentIterable.from(getMatchingOccurrencesProjection(criteria)).transform(DEVIATION_PROJECTION_TO_POJO)
                .toSortedList(DeviationCauseHolderComparators.BY_REASON_ASC);
    }

    private List<Entity> getMatchingOccurrencesProjection(final DeviationsReportCriteria criteria) {
        SearchQueryBuilder sqb = getDictionaryItemDD().find(ALL_PROBLEM_OCCURRENCES_QUERY);
        Interval searchInterval = criteria.getSearchInterval();
        sqb.setTimestamp(L_DATE_FROM, searchInterval.getStart().toDate());
        sqb.setTimestamp(L_DATE_TO, searchInterval.getEnd().toDate());
        sqb.setParameterList(L_EXCLUDED_ORDER_STATES, criteria.getExcludedOrderStates());
        return sqb.list().getEntities();
    }

    private DataDefinition getDictionaryItemDD() {
        return dataDefinitionService.get(QcadooModelConstants.PLUGIN_IDENTIFIER, QcadooModelConstants.MODEL_DICTIONARY_ITEM);
    }

}
