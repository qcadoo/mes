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

import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.model.DataDefinition;

/**
 * Component represents view element - windows, forms, inputs, etc.
 * 
 * Component should defined field or sourceField path. It is used to resolve data and field definition of the component and
 * register component as listener.
 * 
 * <b>Field path</b> defined relation between this component and its parent. It takes parent's entity and gets its field using
 * this path. It is defined in view.xml using "field" attribute.
 * 
 * <b>Source field path</b> defines relation between this component and source component. It takes source component's entity and
 * gets its field using this path. It is defined in view.xml using "source" attribute using notation "#{component}.path" -
 * component is the path to the source component, path is the source field path.
 * 
 * @param <T>
 *            class of the component's value
 */
public interface Component<T> {

    /**
     * Return type of the component - window, form, input, etc. It is used to find javascript and jsp files generating component.
     * 
     * @return type
     */
    String getType();

    /**
     * Prepare view value which will be returned to the client.
     * 
     * @param entity
     *            main entity
     * @param selectedEntities
     *            selected entities
     * @param viewValue
     *            view value of the component
     * @param pathsToUpdate
     *            components' paths that should be updated
     * @param locale
     *            locale
     * @return view value
     * @see Component#castValue(Map, JSONObject)
     */
    ViewValue<T> getValue(Entity entity, Map<String, Entity> selectedEntities, ViewValue<?> viewValue, Set<String> pathsToUpdate,
            Locale locale);

    /**
     * Return component's name.
     * 
     * @return name
     */
    String getName();

    /**
     * Return sourceField's path.
     * 
     * @return sourceField's path
     * @see AbstractComponent#AbstractComponent(String, ContainerComponent, String, String, TranslationService)
     */
    String getSourceFieldPath();

    /**
     * Return path of this component. Path is equal to joined names of all parent component plus the name of this component.
     * 
     * @return path
     */
    String getPath();

    /**
     * Return field's path.
     * 
     * @return field's path
     * @see AbstractComponent#AbstractComponent(String, ContainerComponent, String, String, TranslationService)
     */
    String getFieldPath();

    /**
     * Return view definition which holds this component.
     * 
     * @return view definition
     */
    ViewDefinition getViewDefinition();

    /**
     * Return data definition of this component.
     * 
     * @return data definition
     * @see AbstractComponent#AbstractComponent(String, ContainerComponent, String, String, TranslationService)
     */
    DataDefinition getDataDefinition();

    /**
     * Return true if component should be enabled by default.
     * 
     * @return is default enabled
     */
    boolean isDefaultEnabled();

    /**
     * Return true if component should be visible by default.
     * 
     * @return is default visible
     */
    boolean isDefaultVisible();

    /**
     * Add component translations.
     * 
     * @param translationsMap
     *            translations
     * @param locale
     *            locale
     */
    void updateTranslations(Map<String, String> translationsMap, Locale locale);

    /**
     * Initialize component using given registry. Return true if initialization was successful.
     * 
     * @param componentRegistry
     *            registry
     * @return is successful
     */
    boolean initializeComponent(Map<String, Component<?>> componentRegistry);

    /**
     * Return true if component is initialized.
     * 
     * @return is initialized
     */
    boolean isInitialized();

    /**
     * Convert JSON to components view value. {@link SelectableComponent} and {@link SaveableComponent} should add it entities to
     * selected entities.
     * 
     * @param selectedEntities
     *            selected entities
     * @param viewObject
     *            JSON
     * @return view value
     * @throws JSONException
     *             if JSON is invalid
     */
    ViewValue<T> castValue(Map<String, Entity> selectedEntities, JSONObject viewObject) throws JSONException;

    /**
     * Return component's listeners.
     * 
     * @return listeners
     */
    Set<String> getListeners();

    /**
     * Return true if this component is related to the view entity.
     * 
     * @return is related to main entity
     */
    boolean isRelatedToMainEntity();

}
