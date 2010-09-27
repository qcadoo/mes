package com.qcadoo.mes.core.api;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;

import com.qcadoo.mes.core.view.ViewDefinition;

public interface ViewDefinitionService {

    List<ViewDefinition> list();

    // TODO masz to remove
    void initViews();

    void save(ViewDefinition viewDefinition);

    // TODO change expression
    @PreAuthorize("hasRole('ROLE_ADMIN') or ((#viewName != 'products.orderGridView') and (#viewName != 'products.orderDetailsView')"
            + " or hasRole('ROLE_SUPERVISOR')) and ((#viewName != 'users.userGridView') and (#viewName != 'users.userDetailsView')"
            + " and (#viewName != 'users.groupGridView') and (#viewName != 'users.groupDetailsView')"
            + " and (#viewName != 'plugins.pluginGridView') and (#viewName != 'plugins.pluginDetailsView'))")
    ViewDefinition get(String pluginIdentifier, String viewName);

    void delete(String pluginIdentifier, String viewName);

}
