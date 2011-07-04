package com.qcadoo.mes.productionScheduling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.ganttChart.GanttChartItem;
import com.qcadoo.view.api.components.ganttChart.GanttChartScale;

@Service
public class OrderGanttChartItemResolverImpl implements OrderGanttChartItemResolver {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    @Override
    @Transactional
    public Map<String, List<GanttChartItem>> resolve(final GanttChartScale scale, final JSONObject context, final Locale locale) {
        List<Entity> orders = dataDefinitionService.get("orders", "order").find().add(SearchRestrictions.ne("state", "03done"))
                .add(SearchRestrictions.lt("dateFrom", scale.getDateTo()))
                .add(SearchRestrictions.gt("dateTo", scale.getDateFrom())).list().getEntities();

        List<GanttChartItem> items = new ArrayList<GanttChartItem>();

        for (Entity order : orders) {
            items.add(getItemForOrder(order, scale, locale));
        }

        return Collections.singletonMap("", items);
    }

    private GanttChartItem getItemForOrder(final Entity order, final GanttChartScale scale, final Locale locale) {
        Date from = (Date) order.getField("dateFrom");
        Date to = (Date) order.getField("dateTo");

        if (order.getField("effectiveDateFrom") != null) {
            long diff = to.getTime() - from.getTime();
            from = (Date) order.getField("effectiveDateFrom");
            to = new Date(from.getTime() + diff);
        }

        return scale.createGanttChartItem("", getOrderDescription(order, locale), order.getId(), from, to);
    }

    private String getOrderDescription(final Entity order, final Locale locale) {
        return order.getStringField("number") + " - " + order.getStringField("name") + "<br/>"
                + translationService.translate("orders.order.state.label", locale) + ": "
                + translationService.translate("orders.order.state.value." + order.getStringField("state"), locale);
    }

}
