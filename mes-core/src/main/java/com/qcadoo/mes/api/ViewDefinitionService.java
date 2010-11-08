package com.qcadoo.mes.api;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;

import com.qcadoo.mes.view.ViewDefinition;

/**
 * Service for manipulating view definitions.
 * 
 * @see com.qcadoo.mes.view.internal.ViewDefinitionParser
 * @apiviz.uses com.qcadoo.mes.view.ViewDefinition
 */
public interface ViewDefinitionService {

    /**
     * Return the view definition matching the given plugin's identifier and view's name. The method checks if user has sufficient
     * permissions.
     * 
     * @param pluginIdentifier
     *            plugin's identifier
     * @param viewName
     *            view's name
     * @return the view definition
     * @throws NullPointerException
     *             if view definition is not found
     * @throws IllegalStateException
     *             if view's plugin is inactive
     */
    @PreAuthorize("hasRole('ROLE_ADMIN') or (#pluginIdentifier == 'dictionaries') or (#pluginIdentifier == 'products' "
            + "and (#viewName != 'orderGridView' and #viewName != 'orderDetailsView' or hasRole('ROLE_SUPERVISOR')))")
    ViewDefinition get(String pluginIdentifier, String viewName);

    /**
     * Return the view definition matching the given plugin's identifier and view's name.
     * 
     * @param pluginIdentifier
     *            plugin's identifier
     * @param viewName
     *            view's name
     * @return the view definition
     * @exception NullPointerException
     *                if view definition is not found
     * @exception IllegalStateException
     *                if view's plugin is inactive
     */
    ViewDefinition getWithoutSession(String pluginIdentifier, String viewName);

    /**
     * Return all defined view definitions.
     * 
     * @return the data definitions
     */
    List<ViewDefinition> list();

    /**
     * Return all view definitions which can be displayed in menu.
     * 
     * @return the data definitions
     */
    List<ViewDefinition> listForMenu();

    /**
     * Save the data definition.
     * 
     * @param viewDefinition
     *            view definition
     */
    void save(ViewDefinition viewDefinition);

    /**
     * Delete the data definition.
     * 
     * @param viewDefinition
     *            view definition
     */
    void delete(ViewDefinition viewDefinition);

}
