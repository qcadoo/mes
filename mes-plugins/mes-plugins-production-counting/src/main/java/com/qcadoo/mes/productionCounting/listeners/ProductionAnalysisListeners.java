package com.qcadoo.mes.productionCounting.listeners;

import java.util.Collections;
import java.util.Map;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionCounting.constants.ProductionCountingConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.grid.GridComponentFilterSQLUtils;
import com.qcadoo.view.api.components.grid.GridComponentMultiSearchFilter;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class ProductionAnalysisListeners {

    public static final String MODEL_PRODUCTION_ANALYSIS_DTO = "productionAnalysisDto";



    private static final String TABLE_PRODUCTIONCOUNTING_PRODUCTIONANALYSISDTO = "productioncounting_productionanalysisdto";

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void refreshProductionAnalysisMV(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        jdbcTemplate.queryForObject("SELECT refreshProductionAnalysisMV()",
                Maps.newHashMap()
                , Object.class);
    }


    public void calculateTotalQuantities(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        final FieldComponent totalProducedQuantity = (FieldComponent) view.getComponentByReference("totalProducedQuantity");
        final FieldComponent numberOfWorkers = (FieldComponent) view.getComponentByReference("numberOfWorkers");
        final GridComponent grid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);

        String query = buildQuery();

        Map<String, String> filter = grid.getFilters();
        GridComponentMultiSearchFilter multiSearchFilter = grid.getMultiSearchFilter();
        String filterQ;
        try {
            filterQ = GridComponentFilterSQLUtils.addFilters(filter, grid.getColumns(),
                    TABLE_PRODUCTIONCOUNTING_PRODUCTIONANALYSISDTO,
                    dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER, MODEL_PRODUCTION_ANALYSIS_DTO));
            filterQ += " AND ";
            filterQ += GridComponentFilterSQLUtils.addMultiSearchFilter(multiSearchFilter, grid.getColumns(),
                    TABLE_PRODUCTIONCOUNTING_PRODUCTIONANALYSISDTO,
                    dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER, MODEL_PRODUCTION_ANALYSIS_DTO));
        } catch (Exception e) {
            filterQ = "";
        }

        if (StringUtils.isNoneBlank(filterQ)) {
            query = query + " WHERE " + filterQ;
        }

        Map<String, Object> values = jdbcTemplate.queryForMap(query, Collections.emptyMap());
        totalProducedQuantity.setFieldValue(values.get("totalDoneQuantity"));
        numberOfWorkers.setFieldValue(values.get("totalStaffNumber"));
        totalProducedQuantity.requestComponentUpdateState();
        numberOfWorkers.requestComponentUpdateState();
    }

    private String buildQuery() {
        StringBuilder query = new StringBuilder();
        query.append("SELECT SUM(donequantity)::bigint AS totalDoneQuantity, ");
        query.append("COUNT(DISTINCT staff_id) AS totalStaffNumber ");
        query.append("FROM productioncounting_productionanalysisdto");
        return query.toString();
    }
}
