package com.qcadoo.mes.orders.deviations.dataProvider;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.text.StrSubstitutor;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.orders.constants.CommonReasonTypeFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.deviations.DeviationsReportCriteria;
import com.qcadoo.mes.orders.deviations.constants.DeviationType;
import com.qcadoo.mes.orders.deviations.domain.DeviationSummary;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchQueryBuilder;

@Service
public class DeviationSummariesDataProvider {

    private static final String L_DATE_FROM = "dateFromPlaceholder";

    private static final String L_DATE_TO = "dateToPlaceholder";

    // I have the feelings that I probably should extract whole logic responsible for creating this partial queries into separate
    // object, called 'DeviationsSummaryQueryBuilder' or something like that..
    private static final String PARTIAL_QUERY_TPL = "select rt.${REASON_TYPE_FIELD} as deviationCause, "
            + "rt.${REASON_DATE_FIELD} as date, o.${ORDER_NUMBER_FIELD} as orderNumber, "
            + "p.${PRODUCT_NUMBER_FIELD} as productNumber, o.${ORDER_COMMENT_FIELD} as comment "
            + "from #orders_${REASON_MODEL_NAME} as rt inner join rt.${REASON_ORDER_FIELD} o left join o.${ORDER_PRODUCT} p "
            + "where rt.${REASON_DATE_FIELD} >= :" + L_DATE_FROM + " and rt.${REASON_DATE_FIELD} < :" + L_DATE_TO;

    private static final ImmutableMap<String, String> COMMON_LITERAL_SUBSTITUTIONS = ImmutableMap.of("REASON_DATE_FIELD",
            CommonReasonTypeFields.DATE, "ORDER_NUMBER_FIELD", OrderFields.NUMBER, "PRODUCT_NUMBER_FIELD", ProductFields.NUMBER,
            "REASON_ORDER_FIELD", CommonReasonTypeFields.ORDER, "ORDER_PRODUCT", OrderFields.PRODUCT);

    private static final Function<DeviationType, String> QUERY_FOR_DEVIATION_TYPE = new Function<DeviationType, String>() {

        @Override
        public String apply(final DeviationType deviationType) {
            Map<String, String> literalSubstitutions = Maps.newHashMap(COMMON_LITERAL_SUBSTITUTIONS);
            literalSubstitutions.put("REASON_TYPE_FIELD", deviationType.getReasonTypeInReasonModelFieldName());
            literalSubstitutions.put("ORDER_COMMENT_FIELD", deviationType.getCommentInOrderFieldName());
            literalSubstitutions.put("REASON_MODEL_NAME", deviationType.getReasonModelName());
            return new StrSubstitutor(literalSubstitutions, "${", "}").replace(PARTIAL_QUERY_TPL).toString();
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
                return sqb.list().getEntities();
            }
        };
    }

    public Multimap<String, DeviationSummary> getDeviationsByCauseType(final DeviationsReportCriteria criteria) {
        FluentIterable<DeviationSummary> flatDeviationSummaries = FluentIterable.from(Arrays.asList(DeviationType.values()))
                .transform(QUERY_FOR_DEVIATION_TYPE).transformAndConcat(buildQueryExecutionFunc(getDataDefinition(), criteria))
                .transform(BUILD_SUMMARY_POJO_FROM_PROJECTION);
        return Multimaps.index(flatDeviationSummaries, CAUSE_EXTRACTOR);
    }

    public DataDefinition getDataDefinition() {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER);
    }
}
