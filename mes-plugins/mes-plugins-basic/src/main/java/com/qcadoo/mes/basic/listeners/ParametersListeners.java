package com.qcadoo.mes.basic.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.CompanyService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class ParametersListeners {

    @Autowired
    private CompanyService companyService;

    public void redirectToCompany(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        Long companyId = companyService.getCompanyId();

        if (companyId != null) {
            String url = "../page/basic/companyDetails.html?context={\"form.id\":\"" + companyId + "\"}";
            view.redirectTo(url, false, true);
        }
    }
}
