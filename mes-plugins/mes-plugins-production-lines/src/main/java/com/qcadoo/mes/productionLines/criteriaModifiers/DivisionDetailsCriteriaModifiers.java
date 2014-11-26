package com.qcadoo.mes.productionLines.criteriaModifiers;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionLines.constants.ProductionLineFields;
import com.qcadoo.mes.productionLines.constants.WorkstationFieldsPL;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class DivisionDetailsCriteriaModifiers {

    public void showProductionLinesWithoutDivisionOnly(final SearchCriteriaBuilder scb) {
        scb.add(SearchRestrictions.isNull(ProductionLineFields.DIVISION));
    }

    public void showWorkstationsWithoutProductionLineOnly(final SearchCriteriaBuilder scb) {
        scb.add(SearchRestrictions.isNull(WorkstationFieldsPL.PRODUCTION_LINE));
    }
}
