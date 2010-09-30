package com.qcadoo.mes.api;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;

import com.qcadoo.mes.view.ViewDefinition;
import com.qcadoo.mes.view.menu.MenuDefinition;

public interface ViewDefinitionService {

    List<ViewDefinition> list();

    void save(ViewDefinition viewDefinition);

    @PreAuthorize("hasRole('ROLE_ADMIN') or (#pluginIdentifier == 'dictionaries') or (#pluginIdentifier == 'products' "
            + "and (#viewName != 'orderGridView' and #viewName != 'orderDetailsView' or hasRole('ROLE_SUPERVISOR')))")
    ViewDefinition get(String pluginIdentifier, String viewName);

    void delete(String pluginIdentifier, String viewName);

    MenuDefinition getMenu();

}
