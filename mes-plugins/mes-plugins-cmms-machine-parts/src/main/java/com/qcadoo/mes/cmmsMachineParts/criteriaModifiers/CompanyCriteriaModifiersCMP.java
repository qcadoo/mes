package com.qcadoo.mes.cmmsMachineParts.criteriaModifiers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.ParameterFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class CompanyCriteriaModifiersCMP {

    @Autowired
    private ParameterService parameterService;

    public void hideOwnerCompany(SearchCriteriaBuilder scb) {
        Entity owner = parameterService.getParameter().getBelongsToField(ParameterFields.COMPANY);
        if (owner != null) {
            scb.add(SearchRestrictions.idNe(owner.getId()));
        }
    }
}
