/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
 * Version: 1.4
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
package com.qcadoo.mes.productionPerShift.dataProvider;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftConstants;
import com.qcadoo.mes.productionPerShift.domain.ProductionProgress;
import com.qcadoo.mes.productionPerShift.factory.ProductionProgressDTOFactory;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchQueryBuilder;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.qcadoo.mes.productionPerShift.factory.ProductionProgressDTOFactory.*;

@Service
public class PlannedProgressDataProvider implements ProductionProgressDataProvider {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    // FIXME maku: this query may not work as expected with many main output products.
    public static final String RAW_HQL_QUERY = "select "
            + "d.id as id, o.typeOfProductionRecording as typeOfProductionRecording, "
            + "p.actualDateOfDay as ${SHIFT_START_DAY_ALIAS}, sh.id as ${SHIFT_ID_ALIAS}, "
            + "sh.name as ${SHIFT_NAME_ALIAS}, d.quantity as ${QUANTITY_ALIAS}, o.number as ${ORDER_NUMBER_ALIAS}, "
            + "o.id as ${ORDER_ID_ALIAS}, prod.id as ${PRODUCT_ID_ALIAS}, prod.number as ${PRODUCT_NUMBER_ALIAS}, "
            + "prod.unit as ${PRODUCT_UNIT_ALIAS} from #productionPerShift_progressForDay p "
            + "join p.productionPerShift pps join pps.order o join o.product as prod "
            + "right join p.dailyProgress d left join d.shift sh where d.quantity > 0 AND "
            + "p.id in (select max(pfd.id) from #productionPerShift_progressForDay pfd "
            + "where pfd.actualDateOfDay >= :DATE_FROM AND pfd.actualDateOfDay <= :DATE_TO "
            + "group by pfd.productionPerShift.order, pfd.actualDateOfDay)";

    public static final String HQL_QUERY = getQueryWithFilledPlaceholders();

    private static String getQueryWithFilledPlaceholders() {
        Map<String, String> placeholderValues = Maps.newHashMap();
        placeholderValues.put("SHIFT_START_DAY_ALIAS", SHIFT_START_DAY_ALIAS);
        placeholderValues.put("SHIFT_ID_ALIAS", SHIFT_ID_ALIAS);
        placeholderValues.put("SHIFT_NAME_ALIAS", SHIFT_NAME_ALIAS);
        placeholderValues.put("QUANTITY_ALIAS", QUANTITY_ALIAS);
        placeholderValues.put("ORDER_NUMBER_ALIAS", ORDER_NUMBER_ALIAS);
        placeholderValues.put("ORDER_ID_ALIAS", ORDER_ID_ALIAS);
        placeholderValues.put("PRODUCT_ID_ALIAS", PRODUCT_ID_ALIAS);
        placeholderValues.put("PRODUCT_NUMBER_ALIAS", PRODUCT_NUMBER_ALIAS);
        placeholderValues.put("PRODUCT_UNIT_ALIAS", PRODUCT_UNIT_ALIAS);
        StrSubstitutor substitutor = new StrSubstitutor(placeholderValues, "${", "}");
        return substitutor.replace(RAW_HQL_QUERY);
    }

    public Collection<ProductionProgress> find(final Interval searchInterval) {
        List<Entity> projections = findProjections(searchInterval);
        List<ProductionProgress> productionProgresses = Lists.newLinkedList();
        for (Entity projection : projections) {
            ProductionProgress productionProgress = ProductionProgressDTOFactory.from(projection);
            productionProgresses.add(productionProgress);
        }
        return productionProgresses;
    }

    private List<Entity> findProjections(final Interval searchInterval) {
        Preconditions.checkArgument(searchInterval != null, "Search interval must be not null");
        SearchQueryBuilder sqb = getDataDefinition().find(HQL_QUERY);
        sqb.setDate("DATE_FROM", searchInterval.getStart().toDate());
        sqb.setDate("DATE_TO", searchInterval.getEnd().toDate());
        return sqb.list().getEntities();
    }

    private DataDefinition getDataDefinition() {
        return dataDefinitionService.get(ProductionPerShiftConstants.PLUGIN_IDENTIFIER,
                ProductionPerShiftConstants.MODEL_DAILY_PROGRESS);
    }

}
