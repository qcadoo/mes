package com.qcadoo.mes.deviationCausesReporting.dataProvider;

import java.util.Arrays;
import java.util.Collection;
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
import com.google.common.collect.ImmutableMap;
import com.qcadoo.mes.deviationCausesReporting.DeviationsReportCriteria;
import com.qcadoo.mes.deviationCausesReporting.constants.DeviationType;
import com.qcadoo.mes.deviationCausesReporting.domain.DeviationWithOccurrencesCount;
import com.qcadoo.mes.orders.constants.deviationReasonTypes.DeviationModelDescriber;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchQueryBuilder;
import com.qcadoo.model.constants.QcadooModelConstants;

@Service
public class DeviationWithOccurrencesDataProvider {

    private static final String L_DATE_FROM = "dateFromPlaceholder";

    private static final String L_DATE_TO = "dateToPlaceholder";

    private static final String PARTIAL_COUNT_SUB_QUERY_TPL = "(select count(*) from #${MODEL_PLUGIN}_${MODEL_NAME} "
            + "where ${REASON_TYPE_FIELD_NAME} = di.name and date >= :" + L_DATE_FROM + " AND date <= :" + L_DATE_TO + ")";

    private static final String TOTAL_OCCURRENCES_PROJECTION = prepareTotalOccurrencesForProblemProjection();

    private static String prepareTotalOccurrencesForProblemProjection() {
        Collection<String> subQueries = Collections2.transform(Arrays.asList(DeviationType.values()),
                new Function<DeviationType, String>() {

                    @Override
                    public String apply(final DeviationType deviationType) {
                        DeviationModelDescriber modelDescriber = deviationType.getModelDescriber();
                        StrSubstitutor substitutor = new StrSubstitutor(ImmutableMap.of("MODEL_PLUGIN",
                                modelDescriber.getModelPlugin(), "MODEL_NAME", modelDescriber.getModelName(),
                                "REASON_TYPE_FIELD_NAME", modelDescriber.getReasonTypeFieldName()), "${", "}");
                        return substitutor.replace(PARTIAL_COUNT_SUB_QUERY_TPL).toString();
                    }
                });
        return StringUtils.join(subQueries, " + ");
    }

    private static final String CAUSE_ALIAS = "causeProjectionAlias";

    private static final String TOTAL_COUNT_ALIAS = "totalCountProjectionAlias";

    private static final String ALL_PROBLEM_OCCURRENCES_QUERY = "select di.name as " + CAUSE_ALIAS + ", ("
            + TOTAL_OCCURRENCES_PROJECTION + ") as " + TOTAL_COUNT_ALIAS
            + " from #qcadooModel_dictionaryItem as di where di.dictionary.name = 'reasonTypeOfChangingOrderState'";

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

    public ImmutableList<DeviationWithOccurrencesCount> getDeviationsWithOccurrencesCount(final DeviationsReportCriteria criteria) {
        return FluentIterable.from(getMatchingOccurrencesProjection(criteria)).transform(DEVIATION_PROJECTION_TO_POJO).toList();
    }

    private List<Entity> getMatchingOccurrencesProjection(final DeviationsReportCriteria criteria) {
        SearchQueryBuilder sqb = getDictionaryItemDD().find(ALL_PROBLEM_OCCURRENCES_QUERY);
        Interval searchInterval = criteria.getSearchInterval();
        sqb.setTimestamp(L_DATE_FROM, searchInterval.getStart().toDate());
        sqb.setTimestamp(L_DATE_TO, searchInterval.getEnd().toDate());
        return sqb.list().getEntities();
    }

    private DataDefinition getDictionaryItemDD() {
        return dataDefinitionService.get(QcadooModelConstants.PLUGIN_IDENTIFIER, QcadooModelConstants.MODEL_DICTIONARY_ITEM);
    }

}
