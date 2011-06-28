package com.qcadoo.mes.productionScheduling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.ganttChart.GanttChartItem;
import com.qcadoo.view.api.components.ganttChart.GanttChartScale;

@Service
public class OperationsGanttChartItemResolverImpl implements OperationsGanttChartItemResolver {

    private static final Logger LOG = LoggerFactory.getLogger(OperationsGanttChartItemResolverImpl.class);

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    @Override
    public Map<String, List<GanttChartItem>> resolve(final GanttChartScale scale, final JSONObject context, final Locale locale) {
        try {
            Long orderId = Long.valueOf(context.getString("orderId"));
            Entity order = dataDefinitionService.get("orders", "order").get(orderId);

            if (order == null) {
                LOG.warn("Cannot find order for " + orderId);
                return Collections.emptyMap();
            }

            List<Entity> operations = dataDefinitionService.get("productionScheduling", "orderOperationComponent").find()
                    .add(SearchRestrictions.belongsTo("order", order)).list().getEntities();

            if (operations.isEmpty()) {
                LOG.warn("Cannot find operations for " + order);
                return Collections.emptyMap();
            }

            Date orderStartDate = null;
            Date orderEndDate = null;

            if (order.getField("effectiveDateFrom") != null) {
                orderStartDate = (Date) order.getField("effectiveDateFrom");
                orderEndDate = new Date(((Date) order.getField("effectiveDateFrom")).getTime()
                        + (((Date) order.getField("dateTo")).getTime() - ((Date) order.getField("dateFrom")).getTime()));
            } else if (order.getField("dateFrom") != null) {
                orderStartDate = (Date) order.getField("dateFrom");
                orderEndDate = (Date) order.getField("dateTo");
            } else {
                LOG.warn("Cannot find orderStartDate for " + order);
                return Collections.emptyMap();
            }

            scale.setDateFrom(orderStartDate);
            scale.setDateTo(orderEndDate);

            Map<String, List<GanttChartItem>> items = new TreeMap<String, List<GanttChartItem>>();

            Map<Long, String> machines = new HashMap<Long, String>();
            machines.put(0L, translationService.translate(
                    "productionScheduling.ganttOrderOperationsCalendar.gantt.machineNotAssigned", locale));

            for (Entity operation : operations) {
                Date dateFrom = (Date) operation.getField("effectiveDateFrom");
                Date dateTo = (Date) operation.getField("effectiveDateTo");

                if (dateFrom == null || dateTo == null) {
                    continue;
                }

                Entity machine = operation.getBelongsToField("effectiveMachine");

                String machineName = null;

                if (machine != null) {
                    if (!machines.containsKey(machine.getId())) {
                        machines.put(machine.getId(), getDescriptionForMachine(machine));
                    }
                    machineName = machines.get(machine.getId());
                } else {
                    machineName = machines.get(0L);
                }

                GanttChartItem item = scale.createGanttChartItem(machineName, getDescriptionForOperarion(operation),
                        operation.getId(), dateFrom, dateTo);

                if (!items.containsKey(machineName)) {
                    items.put(machineName, new ArrayList<GanttChartItem>());
                }

                items.get(machineName).add(item);
            }

            return items;
        } catch (NumberFormatException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } catch (JSONException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private String getDescriptionForOperarion(final Entity operation) {
        return operation.getBelongsToField("operation").getStringField("number") + " - "
                + operation.getBelongsToField("operation").getStringField("name");
    }

    private String getDescriptionForMachine(final Entity machine) {
        return machine.getStringField("number") + " - " + machine.getStringField("name");
    }
}
