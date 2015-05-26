package com.qcadoo.mes.productionLines.criteriaModifiers;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.WorkstationFields;
import com.qcadoo.mes.productionLines.constants.WorkstationFieldsPL;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class WorkstationCriteriaModifiers {

    public void showWorkstationsWithoutProductionLineOnly(final SearchCriteriaBuilder scb) {
        scb.add(SearchRestrictions.isNull(WorkstationFieldsPL.PRODUCTION_LINE));
    }

    public void showWorkstationsForCurrentProductionLine(final SearchCriteriaBuilder scb, final FilterValueHolder filterValue) {
        if (filterValue.has("productionLine")) {
            Long productionLineId = filterValue.getLong("productionLine");
            scb.createAlias(WorkstationFieldsPL.PRODUCTION_LINE, WorkstationFieldsPL.PRODUCTION_LINE, JoinType.INNER).add(
                    SearchRestrictions.eq(WorkstationFieldsPL.PRODUCTION_LINE + ".id", productionLineId));
        }
    }

    public void showWorkstationsForCurrentDivision(final SearchCriteriaBuilder scb, final FilterValueHolder filterValue) {
        if (filterValue.has("division")) {
            Long divisionId = filterValue.getLong("division");
            scb.createAlias(WorkstationFields.DIVISION, WorkstationFields.DIVISION, JoinType.INNER).add(
                    SearchRestrictions.eq(WorkstationFields.DIVISION + ".id", divisionId));
        }
    }
}
