/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
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

package com.qcadoo.mes.viewold;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.api.TranslationService;

/**
 * Abstract implementation of {@link ContainerComponent}.
 * 
 * @param <T>
 *            class of the component's value
 */
public abstract class AbstractContainerComponent<T> extends AbstractComponent<T> implements ContainerComponent<T> {

    private final Map<String, Component<?>> components = new LinkedHashMap<String, Component<?>>();

    /**
     * @see Component#castValue(Map, JSONObject)
     */
    public abstract T castContainerValue(final Map<String, Entity> selectedEntities, final JSONObject viewObject)
            throws JSONException;

    /**
     * @see Component#getValue(Entity, Map, ViewValue, Set, Locale)
     */
    public abstract T getContainerValue(final Entity entity, final Map<String, Entity> selectedEntities,
            final ViewValue<T> viewValue, final Set<String> pathsToUpdate, final Locale locale);

    /**
     * @see Component#castValue(Map, JSONObject)
     */
    public abstract void addContainerMessages(final Entity entity, final ViewValue<T> viewValue, final Locale locale);

    /**
     * Create new {@link ContainerComponent}.
     * 
     * @see AbstractComponent#AbstractComponent(String, ContainerComponent, String, String, TranslationService)
     */
    public AbstractContainerComponent(final String name, final ContainerComponent<?> parentContainer, final String fieldPath,
            final String sourceFieldPath, final TranslationService translationService) {
        super(name, parentContainer, fieldPath, sourceFieldPath, translationService);
    }

    @Override
    public final Map<String, Component<?>> getComponents() {
        return components;
    }

    /**
     * Add child component.
     * 
     * @param component
     *            component.
     */
    public final void addComponent(final Component<?> component) {
        components.put(component.getName(), component);
    }

    @Override
    public final ViewValue<T> castComponentValue(final Map<String, Entity> selectedEntities, final JSONObject viewObject)
            throws JSONException {
        ViewValue<T> value = new ViewValue<T>();

        for (Component<?> component : components.values()) {
            JSONObject componentViewObject = viewObject != null ? viewObject.getJSONObject("components").getJSONObject(
                    component.getName()) : null;
            ViewValue<?> componentViewValue = component.castValue(selectedEntities, componentViewObject);
            value.addComponent(component.getName(), componentViewValue);
        }

        value.setValue(castContainerValue(selectedEntities, viewObject));

        return value;
    }

    @Override
    public final ViewValue<T> getComponentValue(final Entity entity, final Entity parentEntity,
            final Map<String, Entity> selectedEntities, final ViewValue<T> viewValue, final Set<String> pathsToUpdate,
            final Locale locale) {

        ViewValue<T> value = new ViewValue<T>();

        boolean isAnyViewValueNotNull = false;

        for (Component<?> component : components.values()) {
            ViewValue<?> componentViewValue = viewValue != null ? viewValue.getComponent(component.getName()) : null;
            ViewValue<?> newViewValue = component.getValue(entity, selectedEntities, componentViewValue, pathsToUpdate, locale);
            if (newViewValue != null) {
                isAnyViewValueNotNull = true;
                value.addComponent(component.getName(), newViewValue);
            }
        }

        if (viewValue == null || !viewValue.isIgnoreMode()) {
            value.setValue(getContainerValue(entity, selectedEntities, viewValue, pathsToUpdate, locale));
        }

        addContainerMessages(entity, value, locale);

        if (isAnyViewValueNotNull || value.getValue() != null) {
            return value;
        } else {
            return null;
        }
    }

    /**
     * @see Component#updateTranslations(Map, Locale)
     */
    protected final void updateComponentsTranslations(final Map<String, String> translations, final Locale locale) {
        for (Component<?> component : components.values()) {
            component.updateTranslations(translations, locale);
        }
    }

    @Override
    public final boolean isContainer() {
        return true;
    }

}
