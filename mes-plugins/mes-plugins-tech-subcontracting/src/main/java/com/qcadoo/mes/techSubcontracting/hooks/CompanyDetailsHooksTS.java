package com.qcadoo.mes.techSubcontracting.hooks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.CompanyService;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class CompanyDetailsHooksTS {

    @Autowired
    private CompanyService companyService;

    public void disabledGridWhenCompanyIsAnOwner(final ViewDefinitionState view) {
        companyService.disabledGridWhenCompanyIsOwner(view, "operationGroups", "operations");
    }

}
