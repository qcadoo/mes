/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.1
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */

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
     * @return the view definition, null if not found
     */
    @PreAuthorize("hasRole('ROLE_ADMIN') or (#pluginIdentifier == 'dictionaries') or (#pluginIdentifier == 'products' "
            + "and (#viewName != 'orderGridView' and #viewName != 'orderDetailsView' or hasRole('ROLE_SUPERVISOR'))) or "
            + "(#pluginIdentifier == 'users' and #viewName == 'userProfileView')")
    ViewDefinition get(String pluginIdentifier, String viewName);

    /**
     * Return the view definition matching the given plugin's identifier and view's name.
     * 
     * @param pluginIdentifier
     *            plugin's identifier
     * @param viewName
     *            view's name
     * @return the view definition, null if not found
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
