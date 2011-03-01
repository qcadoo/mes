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

package com.qcadoo.mes.model;

import java.util.Locale;

import org.json.JSONObject;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.view.ViewDefinition;
import com.qcadoo.mes.view.ViewDefinitionState;

/**
 * Defines hooks for validation/saving entities and generating views.
 */
public interface HookDefinition {

    /**
     * Validate field using custom hook.
     * 
     * @param dataDefinition
     *            data definition
     * @param value
     *            field value
     * @return is field valid
     */
    boolean callWithObjectAndGetBoolean(final DataDefinition dataDefinition, final Object value);

    /**
     * Validate entity using custom hook.
     * 
     * @param dataDefinition
     *            data definition
     * @param entity
     *            entity
     * @return is entity valid
     */
    boolean callWithEntityAndGetBoolean(final DataDefinition dataDefinition, final Entity entity);

    /**
     * Call custom hook on entity.
     * 
     * @param dataDefinition
     *            data definition
     * @param entity
     *            entity
     */
    void callWithEntity(final DataDefinition dataDefinition, final Entity entity);

    /**
     * Call custom hook on view.
     * 
     * @param viewDefinitionState
     *            view definition state
     * @param locale
     *            locale
     */
    void callWithViewState(ViewDefinitionState viewDefinitionState, final Locale locale);

    /**
     * Call custom hook on view.
     * 
     * @param object
     *            json object
     * @param locale
     *            locale
     */
    void callWithJSONObject(ViewDefinition viewDefinition, JSONObject object, final Locale locale);

}
