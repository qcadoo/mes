package com.qcadoo.mes.technologies.criteriaModifiers;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class TOCDetailsCriteriaModifiers {

    public void showProductionLinesForDivision(final SearchCriteriaBuilder scb, final FilterValueHolder filterValue) {
        if (filterValue.has(TechnologyOperationComponentFields.DIVISION)) {
            Long divisionId = filterValue.getLong(TechnologyOperationComponentFields.DIVISION);
            scb.createAlias(TechnologyOperationComponentFields.DIVISION, TechnologyOperationComponentFields.DIVISION,
                    JoinType.INNER).add(SearchRestrictions.eq(TechnologyOperationComponentFields.DIVISION + ".id", divisionId));
        }
    }

    public void showWorkstationsForProductionLine(final SearchCriteriaBuilder scb, final FilterValueHolder filterValue) {
        if (filterValue.has(TechnologyOperationComponentFields.PRODUCTION_LINE)) {
            Long productionLineId = filterValue.getLong(TechnologyOperationComponentFields.PRODUCTION_LINE);
            scb.createAlias(TechnologyOperationComponentFields.PRODUCTION_LINE,
                    TechnologyOperationComponentFields.PRODUCTION_LINE, JoinType.INNER).add(
                    SearchRestrictions.eq(TechnologyOperationComponentFields.PRODUCTION_LINE + ".id", productionLineId));
        }
    }

}
