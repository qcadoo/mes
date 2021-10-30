package com.qcadoo.mes.productionLines.hooks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionLines.constants.UserFieldsPL;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class UserDetailsHooksPL {

    @Autowired
    private SecurityService securityService;

    public void setFieldsEnabledForAdmin(final ViewDefinitionState view) {
        if (securityService.hasCurrentUserRole("ROLE_ADMIN")) {
            LookupComponent lookupComponent = (LookupComponent) view.getComponentByReference(UserFieldsPL.PRODUCTION_LINE);
            lookupComponent.setEnabled(true);
            lookupComponent.requestComponentUpdateState();
        }
    }

}
