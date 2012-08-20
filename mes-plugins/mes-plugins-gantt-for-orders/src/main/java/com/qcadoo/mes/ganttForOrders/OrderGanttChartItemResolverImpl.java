/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.7
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
package com.qcadoo.mes.ganttForOrders;

/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.4.5
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
        List<Entity> orders = dataDefinitionService.get("orders", "order").find()
                .add(SearchRestrictions.ne("state", "04completed")).add(SearchRestrictions.lt("dateFrom", scale.getDateTo()))
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
