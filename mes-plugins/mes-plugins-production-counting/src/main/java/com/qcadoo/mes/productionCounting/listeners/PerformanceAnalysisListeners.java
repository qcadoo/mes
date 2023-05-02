package com.qcadoo.mes.productionCounting.listeners;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.productionCounting.constants.PerformanceAnalysisDetailsDtoFields;
import com.qcadoo.mes.productionCounting.constants.PerformanceAnalysisDtoFields;
import com.qcadoo.mes.productionCounting.constants.ProductionCountingConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.grid.GridComponentFilterSQLUtils;
import com.qcadoo.view.api.components.grid.GridComponentMultiSearchFilter;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class PerformanceAnalysisListeners {



    private static final String L_FILTERS = "filters";

    private static final String L_GRID_OPTIONS = "grid.options";

    private static final String L_WINDOW_ACTIVE_MENU = "window.activeMenu";

    private static final String ISNULL = "ISNULL";

    private static final String TABLE_PRODUCTIONCOUNTING_PERFORMANCEANALYSISDTO = "productioncounting_performanceanalysisdto";

    @Autowired
    private NumberService numberService;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void refreshPerformanceAnalysisMV(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        jdbcTemplate.queryForObject("SELECT refreshPerformanceAnalysisMV()",
                Maps.newHashMap()
                , Object.class);
    }

    public void showDetails(final ViewDefinitionState view, final ComponentState state, final String[] args) {

        GridComponent performanceAnalysisGrid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);

        Entity analysis = performanceAnalysisGrid.getSelectedEntities().get(0);

        Map<String, String> filters = Maps.newHashMap();

        filters.put(PerformanceAnalysisDetailsDtoFields.PRODUCTION_LINE_NUMBER,
                "[" + analysis.getStringField(PerformanceAnalysisDetailsDtoFields.PRODUCTION_LINE_NUMBER) + "]");

        String staffName = analysis.getStringField(PerformanceAnalysisDetailsDtoFields.STAFF_NAME);
        if (!Objects.isNull(staffName)) {
            filters.put(PerformanceAnalysisDetailsDtoFields.STAFF_NAME, "[" + staffName + "]");
        } else {
            filters.put(PerformanceAnalysisDetailsDtoFields.STAFF_NAME, ISNULL);
        }

        String shiftName = analysis.getStringField(PerformanceAnalysisDetailsDtoFields.SHIFT_NAME);
        if (!Objects.isNull(shiftName)) {
            filters.put(PerformanceAnalysisDetailsDtoFields.SHIFT_NAME, "[" + shiftName + "]");
        } else {
            filters.put(PerformanceAnalysisDetailsDtoFields.SHIFT_NAME, ISNULL);
        }

        Date timeRangeFrom = analysis.getDateField(PerformanceAnalysisDetailsDtoFields.TIME_RANGE_FROM);
        if (!Objects.isNull(timeRangeFrom)) {
            filters.put(PerformanceAnalysisDetailsDtoFields.TIME_RANGE_FROM, DateUtils.toDateString(timeRangeFrom));
        } else {
            filters.put(PerformanceAnalysisDetailsDtoFields.TIME_RANGE_FROM, ISNULL);
        }
        Date timeRangeTo = analysis.getDateField(PerformanceAnalysisDetailsDtoFields.TIME_RANGE_TO);
        if (!Objects.isNull(timeRangeTo)) {
            filters.put(PerformanceAnalysisDetailsDtoFields.TIME_RANGE_TO, DateUtils.toDateString(timeRangeTo));
        } else {
            filters.put(PerformanceAnalysisDetailsDtoFields.TIME_RANGE_TO, ISNULL);
        }

        Map<String, Object> gridOptions = Maps.newHashMap();
        gridOptions.put(L_FILTERS, filters);

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put(L_GRID_OPTIONS, gridOptions);

        parameters.put(L_WINDOW_ACTIVE_MENU, "analysis.performanceAnalysis");

        String url = "../page/productionCounting/performanceAnalysisDetails.html";
        view.redirectTo(url, false, true, parameters);
    }

    public void calculateTotalTime(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FieldComponent totalTimeBasedOnNorms = (FieldComponent) view
                .getComponentByReference(PerformanceAnalysisDtoFields.TOTAL_TIME_BASED_ON_NORMS);
        FieldComponent totalLaborTime = (FieldComponent) view
                .getComponentByReference(PerformanceAnalysisDtoFields.TOTAL_LABOR_TIME);
        FieldComponent totalDeviationTime = (FieldComponent) view
                .getComponentByReference(PerformanceAnalysisDtoFields.TOTAL_DEVIATION_TIME);
        FieldComponent totalPerformance = (FieldComponent) view
                .getComponentByReference(PerformanceAnalysisDtoFields.TOTAL_PERFORMANCE);
        GridComponent grid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);

        String query = buildQuery();

        Map<String, String> filter = grid.getFilters();
        GridComponentMultiSearchFilter multiSearchFilter = grid.getMultiSearchFilter();
        String filterQ;
        try {
            filterQ = GridComponentFilterSQLUtils.addFilters(filter, grid.getColumns(),
                    TABLE_PRODUCTIONCOUNTING_PERFORMANCEANALYSISDTO,
                    dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                            PerformanceAnalysisDtoFields.MODEL_PERFORMANCE_ANALYSIS_DTO));
            filterQ += " AND ";
            filterQ += GridComponentFilterSQLUtils.addMultiSearchFilter(multiSearchFilter, grid.getColumns(),
                    TABLE_PRODUCTIONCOUNTING_PERFORMANCEANALYSISDTO,
                    dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                            PerformanceAnalysisDtoFields.MODEL_PERFORMANCE_ANALYSIS_DTO));
        } catch (Exception e) {
            filterQ = "";
        }

        if (StringUtils.isNoneBlank(filterQ)) {
            query = query + " WHERE " + filterQ;
        }

        Map<String, Object> values = jdbcTemplate.queryForMap(query, Collections.emptyMap());
        totalTimeBasedOnNorms.setFieldValue(values.get("totaltimebasedonnorms"));
        totalLaborTime.setFieldValue(values.get("totallabortime"));
        totalDeviationTime.setFieldValue(values.get("totaltimedeviation"));
        totalPerformance.setFieldValue(numberService.format(values.get("totalperformance")));
        totalTimeBasedOnNorms.requestComponentUpdateState();
        totalLaborTime.requestComponentUpdateState();
        totalDeviationTime.requestComponentUpdateState();
        totalPerformance.requestComponentUpdateState();
    }

    private String buildQuery() {
        StringBuilder query = new StringBuilder();
        query.append("SELECT SUM(timebasedonnormssum) AS totaltimebasedonnorms, SUM(labortimesum) AS totallabortime, ");
        query.append("SUM(timedeviation) AS totaltimedeviation, (100 * SUM(timebasedonnormssum)::numeric/ ");
        query.append("SUM(labortimesum))::numeric(14,5) AS totalperformance ");
        query.append("FROM productioncounting_performanceanalysisdto ");
        return query.toString();
    }
}
