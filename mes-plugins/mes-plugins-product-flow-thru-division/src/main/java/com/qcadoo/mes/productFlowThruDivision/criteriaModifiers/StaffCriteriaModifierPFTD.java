/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 * <p>
 * This file is part of Qcadoo.
 * <p>
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.productFlowThruDivision.criteriaModifiers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.StaffFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.constants.ProductionLineScheduleFields;
import com.qcadoo.mes.productionLines.constants.DivisionFieldsPL;
import com.qcadoo.mes.productionLines.constants.ProductionLineFields;
import com.qcadoo.mes.productionLines.constants.ProductionLinesConstants;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyProductionLineFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchProjections;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchSubqueries;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class StaffCriteriaModifierPFTD {

    public static final String L_ORDER_ID = "orderId";

    public static final String L_PRODUCTION_LINE_ID = "productionLineId";

    private static final String L_DOT = ".";

    private static final String L_ID = "id";

    private static final String L_THIS_ID = "this.id";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void filterByProductionLine(final SearchCriteriaBuilder scb, final FilterValueHolder filterValueHolder) {
        if (filterValueHolder.has(L_ORDER_ID)) {
            SearchCriteriaBuilder subCriteria = dataDefinitionService
                    .get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER)
                    .findWithAlias(OrdersConstants.MODEL_ORDER)
                    .add(SearchRestrictions.idEq(filterValueHolder.getLong(L_ORDER_ID)))
                    .createAlias(OrderFields.STAFF, OrderFields.STAFF, JoinType.INNER)
                    .add(SearchRestrictions.eqField(OrderFields.STAFF + L_DOT + L_ID, L_THIS_ID))
                    .setProjection(SearchProjections.id());
            scb.add(SearchSubqueries.notExists(subCriteria));
        }
        if (filterValueHolder.has(L_PRODUCTION_LINE_ID)) {
            scb.add(SearchRestrictions.or(SearchRestrictions.belongsTo(StaffFields.PRODUCTION_LINE,
                            ProductionLinesConstants.PLUGIN_IDENTIFIER, ProductionLinesConstants.MODEL_PRODUCTION_LINE,
                            filterValueHolder.getLong(L_PRODUCTION_LINE_ID)),
                    SearchRestrictions.isNull(StaffFields.PRODUCTION_LINE)));
        } else {
            scb.add(SearchRestrictions.isNull(StaffFields.PRODUCTION_LINE));
        }

    }

}
