package com.qcadoo.mes.cmmsMachineParts.criteriaModifiers;

import com.qcadoo.mes.cmmsMachineParts.constants.SourceCostFields;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.springframework.stereotype.Service;

@Service public class SourceCostCriteriaModifiers {

    public void selectActive(final SearchCriteriaBuilder scb) {
        scb.add(SearchRestrictions.eq(SourceCostFields.ACTIVE, true));
    }
}
