package com.qcadoo.mes.materialFlow.hooks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.AwesomeDynamicListComponent;

@Service
public class ProfileHooksMF {

    private static final String L_USER_LOCATIONS_BORDER_LAYOUT = "userLocationsBorderLayout";

    private static final String L_USER_LOCATIONS = "userLocations";

    private static final String L_LOCATION = "location";

    @Autowired
    private SecurityService securityService;

    public void setupUserLocationsSection(final ViewDefinitionState view) {
        view.getComponentByReference(L_USER_LOCATIONS_BORDER_LAYOUT).setVisible(true);
        if (!securityService.hasCurrentUserRole("ROLE_ADMIN")) {
            view.getComponentByReference(L_USER_LOCATIONS).setEnabled(false);
            AwesomeDynamicListComponent userLocations = (AwesomeDynamicListComponent) view
                    .getComponentByReference(L_USER_LOCATIONS);
            userLocations.getFormComponents().forEach(fc -> fc.findFieldComponentByName(L_LOCATION).setEnabled(false));
        }
    }

}
