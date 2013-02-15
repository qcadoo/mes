/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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
package com.qcadoo.mes.columnExtension;

import java.util.List;
import java.util.Map;

import com.qcadoo.model.api.Entity;

public interface ColumnExtensionService {

    /**
     * Reads data from xml for given plugin and model and return column values
     * 
     * @param plugin
     * 
     * @param model
     * 
     * @return values
     */
    Map<Integer, Map<String, String>> getColumnsAttributesFromXML(final String plugin, final String model);

    /**
     * Adds column for plugin and model with given values
     * 
     * @param pluginIdentifier
     * 
     * @param model
     * 
     * @param values
     * 
     * @return column
     */
    Entity addColumn(final String pluginIdentifier, final String model, final Map<String, String> values);

    /**
     * Deletes column for plugin and model with given values
     * 
     * @param pluginIdentifier
     * 
     * @param model
     * 
     * @param values
     */
    void deleteColumn(final String pluginIdentifier, final String model, final Map<String, String> values);

    /**
     * Check if columns empty
     * 
     * @param pluginIdentifier
     * 
     * @param model
     * 
     * @return boolean
     */
    boolean isColumnsEmpty(final String pluginIdentifier, final String model);

    /**
     * Filters empty columns
     * 
     * @param columns
     *            columns
     * 
     * @param rows
     *            rows
     * 
     * @param columnValues
     *            column values
     * 
     * @return filtered empty columns
     * 
     */
    List<Entity> filterEmptyColumns(final List<Entity> columns, final List<Entity> rows,
            final Map<Entity, Map<String, String>> columnValues);

}
