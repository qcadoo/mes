package com.qcadoo.mes.productionCounting.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.internal.constants.StaffWorkTimeFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.*;

@Service
public class StaffTimeCalculator {

    private static final String TOTAL_LABOR_PROJECTION_ALIAS = "totalLaborProjection";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public Long countTotalLaborTime(final Long productionRecordId) {
        SearchCriteriaBuilder scb = getStaffWorkTimeDD().find();
        SearchProjection totalLaborProjection = SearchProjections.alias(SearchProjections.sum(StaffWorkTimeFields.LABOR_TIME),
                TOTAL_LABOR_PROJECTION_ALIAS);
        SearchProjection rowCntProjection = SearchProjections.rowCount();
        scb.setProjection(SearchProjections.list().add(rowCntProjection).add(totalLaborProjection));
        scb.add(SearchRestrictions.belongsTo(StaffWorkTimeFields.PRODUCTION_RECORD,
                ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_PRODUCTION_RECORD,
                productionRecordId));

        // Fix for missing id column. Touch on your own risk.
        scb.addOrder(SearchOrders.asc(TOTAL_LABOR_PROJECTION_ALIAS));
        Entity res = scb.setMaxResults(1).uniqueResult();
        if (res == null) {
            return 0L;
        }
        Long totalLabor = (Long) res.getField(TOTAL_LABOR_PROJECTION_ALIAS);
        if (totalLabor == null) {
            return 0L;
        }
        return totalLabor;
    }

    private DataDefinition getStaffWorkTimeDD() {
        return dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                ProductionCountingConstants.MODEL_STAFF_WORK_TIME);
    }
}
