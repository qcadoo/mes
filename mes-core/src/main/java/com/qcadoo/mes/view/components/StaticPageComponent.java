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

package com.qcadoo.mes.view.components;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.view.AbstractComponent;
import com.qcadoo.mes.view.ComponentOption;
import com.qcadoo.mes.view.ContainerComponent;
import com.qcadoo.mes.view.ViewValue;

/**
 * Represents static page element. Inside this element is content from another html page. <br/>
 * <br/>
 * Options:
 * <ul>
 * <li>
 * page - String - URI to page with content should be include as content of this component</li>
 * </ul>
 */
public final class StaticPageComponent extends AbstractComponent<Object> {

    public StaticPageComponent(final String name, final ContainerComponent<?> parentContainer, final String fieldPath,
            final String sourceFieldPath, final TranslationService translationService) {
        super(name, parentContainer, fieldPath, sourceFieldPath, translationService);
    }

    @Override
    public String getType() {
        return "staticPage";
    }

    @Override
    public void initializeComponent() {
        for (ComponentOption option : getRawOptions()) {
            if ("page".equals(option.getType())) {
                addOption("page", option.getValue());
            }
        }
    }

    @Override
    public final ViewValue<Object> castComponentValue(final Map<String, Entity> selectedEntities, final JSONObject viewObject)
            throws JSONException {
        return new ViewValue<Object>(null);
    }

    @Override
    public final ViewValue<Object> getComponentValue(final Entity entity, final Entity parentEntity,
            final Map<String, Entity> selectedEntities, final ViewValue<Object> viewValue, final Set<String> pathsToUpdate,
            final Locale locale) {
        return new ViewValue<Object>(null);
    }
}
