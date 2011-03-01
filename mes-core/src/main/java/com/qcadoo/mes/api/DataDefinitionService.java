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

package com.qcadoo.mes.api;

import java.util.List;

import com.qcadoo.mes.model.DataDefinition;

/**
 * Service for manipulating data definitions.
 * 
 * @apiviz.uses com.qcadoo.mes.model.DataDefinition
 */
public interface DataDefinitionService {

    /**
     * Return the data definition matching the given plugin's identifier and model's name.
     * 
     * @param pluginIdentifier
     *            plugin's identifier
     * @param modelName
     *            model's name
     * @return the data definition
     * @throws NullPointerException
     *             if data definition is not found
     */
    DataDefinition get(String pluginIdentifier, String modelName);

    /**
     * Return all defined data definitions.
     * 
     * @return the data definitions
     */
    List<DataDefinition> list();

    /**
     * Save the data definition.
     * 
     * @param dataDefinition
     *            data definition
     */
    void save(DataDefinition dataDefinition);

    /**
     * Delete the data definition.
     * 
     * @param dataDefinition
     *            data definition
     */
    void delete(DataDefinition dataDefinition);

}
