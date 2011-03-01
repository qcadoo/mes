/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.3.0
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

package com.qcadoo.mes.view.menu.items;

import com.qcadoo.mes.view.menu.MenuItem;

/**
 * Menu item that leads to specified ViewDefinition.
 * 
 * @see com.qcadoo.mes.view.ViewDefinition
 */
public final class ViewDefinitionMenuItemItem extends MenuItem {

    private final String viewName;

    /**
     * @param name
     *            identifier of item
     * @param label
     *            item label to display
     * @param pluginIdentifier
     *            plugin identifier of this item
     * @param viewName
     *            name of view that this item leads to
     */
    public ViewDefinitionMenuItemItem(final String name, final String label, final String pluginIdentifier, final String viewName) {
        super(name, label, pluginIdentifier);
        this.viewName = viewName;
    }

    @Override
    public String getPage() {
        return "page/" + getPluginIdentifier() + "/" + viewName + ".html";
    }

}
