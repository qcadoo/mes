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
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.text.StrSubstitutor;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.deviationCausesReporting.DeviationsReportCriteria;
import com.qcadoo.mes.deviationCausesReporting.constants.DeviationType;
import com.qcadoo.mes.deviationCausesReporting.domain.DeviationCauseHolderComparators;
import com.qcadoo.mes.deviationCausesReporting.domain.DeviationSummary;
import com.qcadoo.mes.orders.constants.CommonReasonTypeFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.constants.deviationReasonTypes.DeviationModelDescriber;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchQueryBuilder;

/**
 * Provider for detailed deviation summaries, grouped by its cause's type.
 * 
 * @since 1.3.0
 */
@Service
public class DeviationSummariesDataProvider {

    private static final String L_DATE_FROM = "dateFromPlaceholder";

    private static final String L_DATE_TO = "dateToPlaceholder";

    private static final String L_EXCLUDED_ORDER_STATES = "excludedOrderStates";

    // I have the feelings that I probably should extract whole logic responsible for creating this partial queries into separate
    // object, called 'DeviationsSummaryQueryBuilder' or something like that..
    private static final String PARTIAL_QUERY_TPL = "SELECT rt.${REASON_TYPE_FIELD} AS deviationCause, "
            + "rt.${REASON_DATE_FIELD} AS date, o.${ORDER_NUMBER_FIELD} AS orderNumber, "
            + "p.${PRODUCT_NUMBER_FIELD} AS productNumber, rt.${REASON_COMMENT_PATH} AS comment "
            + "FROM #${REASON_MODEL_PLUGIN}_${REASON_MODEL_NAME} AS rt INNER JOIN rt.${REASON_ORDER_PATH} o LEFT JOIN o.${ORDER_PRODUCT} p "
            + "WHERE rt.${REASON_DATE_FIELD} >= :" + L_DATE_FROM + " AND rt.${REASON_DATE_FIELD} < :" + L_DATE_TO + " "
            + "AND o.${ORDER_STATE} NOT IN (:" + L_EXCLUDED_ORDER_STATES + ")";

    private static final Function<DeviationType, String> QUERY_FOR_DEVIATION_TYPE = new Function<DeviationType, String>() {

        @Override
        public String apply(final DeviationType deviationType) {
            Map<String, String> placeholderValues = Maps.newHashMap();
            placeholderValues.put("REASON_DATE_FIELD", CommonReasonTypeFields.DATE);
            placeholderValues.put("ORDER_NUMBER_FIELD", OrderFields.NUMBER);
            placeholderValues.put("PRODUCT_NUMBER_FIELD", ProductFields.NUMBER);
            placeholderValues.put("ORDER_PRODUCT", OrderFields.PRODUCT);
            placeholderValues.put("ORDER_STATE", OrderFields.STATE);

            DeviationModelDescriber modelDescriber = deviationType.getModelDescriber();
            placeholderValues.put("REASON_TYPE_FIELD", modelDescriber.getReasonTypeFieldName());
            placeholderValues.put("REASON_COMMENT_PATH", deviationType.getCommentPath());
            placeholderValues.put("REASON_MODEL_PLUGIN", modelDescriber.getModelPlugin());
            placeholderValues.put("REASON_MODEL_NAME", modelDescriber.getModelName());
            placeholderValues.put("REASON_ORDER_PATH", deviationType.getPathToOrder());
            return new StrSubstitutor(placeholderValues, "${", "}").replace(PARTIAL_QUERY_TPL).toString();
        }
    };

    private static final Function<Entity, DeviationSummary> BUILD_SUMMARY_POJO_FROM_PROJECTION = new Function<Entity, DeviationSummary>() {

        @Override
        public DeviationSummary apply(final Entity projection) {
            String deviationCause = projection.getStringField("deviationCause");
            LocalDate date = LocalDate.fromDateFields(projection.getDateField("date"));
            String orderNumber = projection.getStringField("orderNumber");
            String productNumber = projection.getStringField("productNumber");
            String comment = projection.getStringField("comment");
            return new DeviationSummary(deviationCause, date, orderNumber, productNumber, comment);
        }
    };

    private static final Function<DeviationSummary, String> CAUSE_EXTRACTOR = new Function<DeviationSummary, String>() {

        @Override
        public String apply(final DeviationSummary deviationSummary) {
            return deviationSummary.getDeviationCause();
        }
    };

    @Autowired
    private DataDefinitionService dataDefinitionService;

    private final Function<String, List<Entity>> buildQueryExecutionFunc(final DataDefinition dataDefinition,
            final DeviationsReportCriteria criteria) {
        return new Function<String, List<Entity>>() {

            @Override
            public List<Entity> apply(final String query) {
                SearchQueryBuilder sqb = dataDefinition.find(query);
                sqb.setTimestamp(L_DATE_FROM, criteria.getSearchInterval().getStart().toDate());
                sqb.setTimestamp(L_DATE_TO, criteria.getSearchInterval().getEnd().toDate());
                sqb.setParameterList(L_EXCLUDED_ORDER_STATES, criteria.getExcludedOrderStates());
                return sqb.list().getEntities();
            }
        };
    }

    /**
     * Get detailed deviation summaries, matching given criteria. Resulting Multimap's keys will be sorted ascending.
     * 
     * @param criteria
     *            restrictions for deviations to be summarized.
     * @return Multimap containing deviation summaries, grouped by its reason (cause) type. Multimap's keys will be in ascending
     *         order.
     */
    public Multimap<String, DeviationSummary> getDeviationsByCauseType(final DeviationsReportCriteria criteria) {
        ImmutableList<DeviationSummary> flatDeviationSummaries = FluentIterable.from(Arrays.asList(DeviationType.values()))
                .transform(QUERY_FOR_DEVIATION_TYPE).transformAndConcat(buildQueryExecutionFunc(getDataDefinition(), criteria))
                .transform(BUILD_SUMMARY_POJO_FROM_PROJECTION).toSortedList(DeviationCauseHolderComparators.BY_REASON_ASC);
        return Multimaps.index(flatDeviationSummaries, CAUSE_EXTRACTOR);
    }

    private DataDefinition getDataDefinition() {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER);
    }
}
