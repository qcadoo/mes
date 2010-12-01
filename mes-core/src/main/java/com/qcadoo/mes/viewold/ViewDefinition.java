/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.viewold;

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
