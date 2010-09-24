package com.qcadoo.mes.core.api;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;

import com.qcadoo.mes.core.view.ViewDefinition;

public interface ViewDefinitionService {

    @PreAuthorize("hasRole('ROLE_ADMIN') or ((#viewName != 'products.orderGridView') and (#viewName != 'products.orderDetailsView')"
            + " or hasRole('ROLE_SUPERVISOR')) and ((#viewName != 'users.userGridView') and (#viewName != 'users.userDetailsView')"
            + " and (#viewName != 'users.groupGridView') and (#viewName != 'users.groupDetailsView')"
            + " and (#viewName != 'plugins.pluginGridView') and (#viewName != 'plugins.pluginDetailsView'))")
    ViewDefinition getViewDefinition(String viewName);

    List<ViewDefinition> getAllViews();

    // TODO masz to remove
    void initViews();

}
