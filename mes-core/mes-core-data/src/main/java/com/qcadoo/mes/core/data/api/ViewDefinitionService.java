package com.qcadoo.mes.core.data.api;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;

import com.qcadoo.mes.core.data.definition.view.ViewDefinition;

public interface ViewDefinitionService {

    @PreAuthorize("hasRole('ROLE_ADMIN') or ((#viewName != 'users.userGridView') and (#viewName != 'users.userDetailsView')"
            + " and (#viewName != 'users.groupGridView') and (#viewName != 'users.groupDetailsView')"
            + " and (#viewName != 'plugins.pluginGridView') and (#viewName != 'plugins.pluginDetailsView'))")
    ViewDefinition getViewDefinition(String viewName);

    List<ViewDefinition> getAllViews();

}
