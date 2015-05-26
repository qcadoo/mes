package com.qcadoo.mes.technologies.criteriaModifiers;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.technologies.constants.OperationFields;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class OperationDetailsCriteriaModifiers {

    public void showProductionLinesForDivision(final SearchCriteriaBuilder scb, final FilterValueHolder filterValue) {
        if (filterValue.has(OperationFields.DIVISION)) {
            Long divisionId = filterValue.getLong(OperationFields.DIVISION);
            // scb.createAlias(OperationFields.DIVISION, OperationFields.DIVISION, JoinType.INNER).add(
            // SearchRestrictions.eq(OperationFields.DIVISION + ".id", divisionId));
        }
    }

    public void showWorkstationsForProductionLine(final SearchCriteriaBuilder scb, final FilterValueHolder filterValue) {
        if (filterValue.has(OperationFields.PRODUCTION_LINE)) {
            Long productionLineId = filterValue.getLong(OperationFields.PRODUCTION_LINE);
            scb.createAlias(OperationFields.PRODUCTION_LINE, OperationFields.PRODUCTION_LINE, JoinType.INNER).add(
                    SearchRestrictions.eq(OperationFields.PRODUCTION_LINE + ".id", productionLineId));
        }
    }

}
