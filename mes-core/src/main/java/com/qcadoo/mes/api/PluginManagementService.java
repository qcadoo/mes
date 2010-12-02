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

import org.springframework.web.multipart.MultipartFile;

import com.qcadoo.mes.beans.plugins.PluginsPlugin;

/**
 * Service for manipulating plugins.
 * 
 * @apiviz.uses com.qcadoo.mes.api.PluginManagementOperationStatus
 */
public interface PluginManagementService {

    /**
     * Return plugin with given identifier.
     * 
     * @param pluginIdentifier
     *            plugin's identifier
     * @return the plugin, or null if not found
     */
    PluginsPlugin getByIdentifier(final String pluginIdentifier);

    /**
     * Return plugin with given identifier and status.
     * 
     * @param pluginIdentifier
     *            plugin's identifier
     * @param status
     *            status
     * @return the plugin, or null if not found
     */
    PluginsPlugin getByIdentifierAndStatus(String pluginIdentifier, String status);

    /**
     * Return plugin with given id.
     * 
     * @param id
     *            id
     * @return the plugin, or null if not found
     */
    PluginsPlugin get(Long id);

    /**
     * Return plugin with given name and vendor.
     * 
     * @param name
     *            name
     * @param vendor
     *            vendor
     * @return the plugin, or null if not found
     */
    PluginsPlugin getByNameAndVendor(final String name, final String vendor);

    /**
     * Download the plugin and save it into the database with status "downloaded".
     * 
     * @param file
     *            file to upload
     * @throws com.qcadoo.mes.plugins.internal.exceptions.PluginException
     *             if operation cannot be executed
     * @return the operation's status
     */
    PluginManagementOperationStatus downloadPlugin(final MultipartFile file);

    /**
     * Remove the "downloaded" plugin.
     * 
     * @param id
     *            id
     * @throws com.qcadoo.mes.plugins.internal.exceptions.PluginException
     *             if operation cannot be executed
     * @return the operation's status
     */
    PluginManagementOperationStatus removePlugin(final Long id);

    /**
     * Change the plugin's status to "active".
     * 
     * @param id
     *            id
     * @throws com.qcadoo.mes.plugins.internal.exceptions.PluginException
     *             if operation cannot be executed
     * @return the operation's status
     */
    PluginManagementOperationStatus enablePlugin(final Long id);

    /**
     * Change the plugin's status to "installed".
     * 
     * @param id
     *            id
     * @throws com.qcadoo.mes.plugins.internal.exceptions.PluginException
     *             if operation cannot be executed
     * @return the operation's status
     */
    PluginManagementOperationStatus disablePlugin(final Long id);

    /**
     * Remove the "installed" plugin and restart the server.
     * 
     * @param id
     *            id
     * @throws com.qcadoo.mes.plugins.internal.exceptions.PluginException
     *             if operation cannot be executed
     * @return the operation's status
     */
    PluginManagementOperationStatus deinstallPlugin(final Long id);

    /**
     * Download new plugin's version and restart the server.
     * 
     * @param file
     *            file to upload
     * @throws com.qcadoo.mes.plugins.internal.exceptions.PluginException
     *             if operation cannot be executed
     * @return the operation's status
     */
    PluginManagementOperationStatus updatePlugin(final Long id, final MultipartFile file);

    /**
     * Restart the server.
     * 
     * @throws com.qcadoo.mes.plugins.internal.exceptions.PluginException
     *             if restart cannot be executed
     */
    void restartServer();

    boolean pluginIsInstalled(final Long id);

}
