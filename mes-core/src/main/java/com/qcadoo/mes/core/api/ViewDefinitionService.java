package com.qcadoo.mes.core.api;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;

import com.qcadoo.mes.core.view.ViewDefinition;

public interface ViewDefinitionService {

    List<ViewDefinition> list();

    void save(ViewDefinition viewDefinition);

    @PreAuthorize("hasRole('ROLE_ADMIN') or ((#pluginIdentifier == 'products') and (#viewName != 'orderGridView') and (#viewName != 'orderDetailsView')"
            + " or hasRole('ROLE_SUPERVISOR')) and ((#viewName != 'users.userGridView') and (#viewName != 'users.userDetailsView')"
            + " and (#viewName != 'users.groupGridView') and (#viewName != 'users.groupDetailsView')"
            + " and (#viewName != 'plugins.pluginGridView') and (#viewName != 'plugins.pluginDetailsView'))")
    ViewDefinition get(String pluginIdentifier, String viewName);

    void delete(String pluginIdentifier, String viewName);

}
