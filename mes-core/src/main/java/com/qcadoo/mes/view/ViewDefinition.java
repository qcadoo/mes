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

package com.qcadoo.mes.view;

import java.util.Locale;
import java.util.Map;

import org.json.JSONObject;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.model.DataDefinition;

/**
 * Object defines view structure. The {@link #getPluginIdentifier()} and {@link #getName()} are used to identifier view name.
 * 
 * @apiviz.owns com.qcadoo.mes.model.DataDefinition
 * @apiviz.uses com.qcadoo.mes.view.Component
 * @apiviz.uses com.qcadoo.mes.view.ViewValue
 * @apiviz.has com.qcadoo.mes.view.RootComponent
 * @apiviz.uses com.qcadoo.mes.api.Entity
 */
public interface ViewDefinition {

    /**
     * Return name of this view definition.
     * 
     * @return name
     */
    String getName();

    /**
     * Return plugin's identifier for this data definition.
     * 
     * @return plugin's identifier
     */
    String getPluginIdentifier();

    /**
     * Return data definition related with this view definition.
     * 
     * @return data definition
     */
    DataDefinition getDataDefinition();

    /**
     * @see RootComponent#lookupComponent(String)
     */
    Component<?> lookupComponent(String path);

    /**
     * Call {@link RootComponent#castValue(Map, JSONObject)} on root component.
     */
    ViewValue<Long> castValue(Map<String, Entity> selectedEntities, JSONObject jsonObject);

    /**
     * Get listeners for given trigger and call {@link RootComponent#getValue(Entity, Map, ViewValue, java.util.Set, Locale)} on
     * root component. If trigger is null, listeners set is empty. If this is save or delete, trigger will be included in
     * listeners set.
     */
    ViewValue<Long> getValue(final Entity entity, final Map<String, Entity> selectedEntities,
            final ViewValue<Long> globalJsonValue, String triggerComponentName, boolean saveOrDelete, final Locale locale);

    /**
     * Call {@link RootComponent#updateTranslations(Map, Locale)} on root component.
     */
    void updateTranslations(final Map<String, String> translations, final Locale locale);

    /**
     * Return root component attached to this view definition.
     * 
     * @return root component
     */
    RootComponent getRoot();

    /**
     * Return true if this view definition represent view which can be used in application menu.
     * 
     * @return is menuable
     */
    boolean isMenuable();
}
