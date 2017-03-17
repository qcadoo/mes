package com.qcadoo.mes.productionCounting.listeners;

import java.util.Collections;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class ProductionAnalysisListeners {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public void calculateTotalQuantities(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FieldComponent totalProducedQuantity = (FieldComponent) view.getComponentByReference("totalProducedQuantity");
        FieldComponent numberOfWorkers = (FieldComponent) view.getComponentByReference("numberOfWorkers");

        Map<String, Object> values = jdbcTemplate.queryForMap(buildQuery(), Collections.emptyMap());
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
