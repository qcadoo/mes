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

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.ShiftFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionCounting.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.constants.ProductionTrackingFields;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductOutComponentFields;
import com.qcadoo.mes.productionCounting.states.constants.ProductionTrackingState;
import com.qcadoo.mes.productionPerShift.domain.ProductionProgress;
import com.qcadoo.mes.productionPerShift.factory.ProductionProgressDTOFactory;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.*;

import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

import static com.qcadoo.mes.productionPerShift.factory.ProductionProgressDTOFactory.*;
import static com.qcadoo.model.api.search.SearchProjections.alias;
import static com.qcadoo.model.api.search.SearchProjections.field;
import static com.qcadoo.model.api.search.SearchRestrictions.*;

@Service
public class RegisteredProgressDataProvider implements ProductionProgressDataProvider {

    private static final String RECORD_ALIAS = "pr";

    private static final String ORDER_ALIAS = "ord";

    private static final String PRODUCT_ALIAS = "prod";

    private static final String SHIFT_ALIAS = "sh";

    private static final char DOT = '.';

    private static final String DOT_ID = DOT + "id";

    private static final SearchProjection PROJECTION = buildProjection();

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    public Collection<ProductionProgress> find(final Interval searchInterval) {
        List<ProductionProgress> progresses = Lists.newArrayList();
        for (Entity projection : findProjections(searchInterval)) {
            ProductionProgress progress = ProductionProgressDTOFactory.from(projection);
            progresses.add(progress);
        }
        return progresses;
    }

    private List<Entity> findProjections(final Interval searchInterval) {
        SearchCriteriaBuilder scb = getDataDefinition().find();
        createAliases(scb);
        scb.setProjection(PROJECTION);
        scb.add(buildCriteria(searchInterval));
        return scb.list().getEntities();
    }

    private void createAliases(final SearchCriteriaBuilder scb) {
        scb.createAlias(TrackingOperationProductOutComponentFields.PRODUCTION_TRACKING, RECORD_ALIAS, JoinType.INNER);
        scb.createAlias(RECORD_ALIAS + DOT + ProductionTrackingFields.SHIFT, SHIFT_ALIAS, JoinType.INNER);
        scb.createAlias(TrackingOperationProductOutComponentFields.PRODUCT, PRODUCT_ALIAS, JoinType.INNER);
        scb.createAlias(RECORD_ALIAS + DOT + ProductionTrackingFields.ORDER, ORDER_ALIAS, JoinType.INNER);
    }

    private SearchCriterion buildCriteria(final Interval searchInterval) {
        SearchCriterion isAccepted = eq(RECORD_ALIAS + DOT + ProductionTrackingFields.STATE,
                ProductionTrackingState.ACCEPTED.getStringValue());
        SearchCriterion dayIsAfterSearchIntervalStarts = ge(RECORD_ALIAS + DOT + ProductionTrackingFields.SHIFT_START_DAY,
                searchInterval.getStart().toDate());
        SearchCriterion dayIsBeforeSearchIntervalEnds = le(RECORD_ALIAS + DOT + ProductionTrackingFields.SHIFT_START_DAY,
                searchInterval.getEnd().toDate());
        SearchCriterion isMainOutputProduct = eqField(PRODUCT_ALIAS + DOT_ID,
                ORDER_ALIAS + DOT + OrderFields.PRODUCT + DOT_ID);
        return and(isAccepted, dayIsAfterSearchIntervalStarts, dayIsBeforeSearchIntervalEnds, isMainOutputProduct);
    }

    private static SearchProjection buildProjection() {
        SearchProjectionList projection = SearchProjections.list();

        projection.add(alias(field(ORDER_ALIAS + DOT_ID), ORDER_ID_ALIAS));
        projection.add(alias(field(ORDER_ALIAS + DOT + OrderFields.NUMBER), ORDER_NUMBER_ALIAS));

        projection.add(alias(field(PRODUCT_ALIAS + DOT_ID), PRODUCT_ID_ALIAS));
        projection.add(alias(field(PRODUCT_ALIAS + DOT + ProductFields.NUMBER), PRODUCT_NUMBER_ALIAS));
        projection.add(alias(field(PRODUCT_ALIAS + DOT + ProductFields.UNIT), PRODUCT_UNIT_ALIAS));

        projection.add(alias(field(SHIFT_ALIAS + DOT_ID), SHIFT_ID_ALIAS));
        projection.add(alias(field(SHIFT_ALIAS + DOT + ShiftFields.NAME), SHIFT_NAME_ALIAS));

        projection.add(alias(field(RECORD_ALIAS + DOT + ProductionTrackingFields.SHIFT_START_DAY), SHIFT_START_DAY_ALIAS));

        projection.add(alias(field(TrackingOperationProductOutComponentFields.USED_QUANTITY), QUANTITY_ALIAS));

        return projection;
    }

    private DataDefinition getDataDefinition() {
        return dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                ProductionCountingConstants.MODEL_TRACKING_OPERATION_PRODUCT_OUT_COMPONENT);
    }
}
