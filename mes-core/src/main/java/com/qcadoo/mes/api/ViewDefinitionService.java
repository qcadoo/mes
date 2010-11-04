package com.qcadoo.mes.api;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;

import com.qcadoo.mes.view.ViewDefinition;

public interface ViewDefinitionService {

    List<ViewDefinition> list();

    void save(ViewDefinition viewDefinition);

    @PreAuthorize("hasRole('ROLE_ADMIN') or (#pluginIdentifier == 'dictionaries') or (#pluginIdentifier == 'products' "
            + "and (#viewName != 'orderGridView' and #viewName != 'orderDetailsView' or hasRole('ROLE_SUPERVISOR')))")
    ViewDefinition get(String pluginIdentifier, String viewName);

    ViewDefinition getWithoutSession(String pluginIdentifier, String viewName);

    List<ViewDefinition> getMenuableViews();

    void delete(String pluginIdentifier, String viewName);

}
