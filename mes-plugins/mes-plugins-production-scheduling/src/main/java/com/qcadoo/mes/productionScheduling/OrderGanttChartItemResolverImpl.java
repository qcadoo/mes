package com.qcadoo.mes.productionScheduling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.ganttChart.GanttChartItem;
import com.qcadoo.view.api.components.ganttChart.GanttChartScale;

@Service
public class OrderGanttChartItemResolverImpl implements OrderGanttChartItemResolver {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    @Transactional
    public Map<String, List<GanttChartItem>> resolve(final GanttChartScale scale) {
        List<Entity> orders = dataDefinitionService.get("orders", "order").find().add(SearchRestrictions.ne("state", "03done"))
                .add(SearchRestrictions.lt("dateFrom", scale.getDateTo()))
                .add(SearchRestrictions.gt("dateTo", scale.getDateFrom())).list().getEntities();

        List<GanttChartItem> items = new ArrayList<GanttChartItem>();

        for (Entity order : orders) {
            items.add(getItemForOrder(order, scale));
        }

        return Collections.singletonMap("", items);
    }

    private GanttChartItem getItemForOrder(final Entity order, final GanttChartScale scale) {
        String orderName = order.getStringField("name");
        Date from = (Date) order.getField("dateFrom");
        Date to = (Date) order.getField("dateTo");

        if (order.getField("effectiveDateFrom") != null) {
            long diff = to.getTime() - from.getTime();
            from = (Date) order.getField("effectiveDateFrom");
            to = new Date(from.getTime() + diff);
        }

        return scale.createGanttChartItem("", orderName, order.getId(), from, to);
    }

}
