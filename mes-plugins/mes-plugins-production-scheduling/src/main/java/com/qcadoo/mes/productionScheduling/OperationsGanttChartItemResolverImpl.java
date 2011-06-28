package com.qcadoo.mes.productionScheduling;

import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.ganttChart.GanttChartItem;
import com.qcadoo.view.api.components.ganttChart.GanttChartScale;

@Service
public class OperationsGanttChartItemResolverImpl implements OperationsGanttChartItemResolver {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    public Map<String, List<GanttChartItem>> resolve(final GanttChartScale scale, final JSONObject context) {
        try {
            Long orderId = Long.valueOf(context.getString("orderId"));

            List<Entity> operations = dataDefinitionService.get("productionScheduling", "orderOperationComponent").find()
                    .add(SearchRestrictions.belongsTo("order", "orders", "order", orderId)).list().getEntities();
            List<Entity> machines = dataDefinitionService.get("basic", "machine").find().list().getEntities();

        } catch (NumberFormatException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } catch (JSONException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }

        return null;
    }

}
