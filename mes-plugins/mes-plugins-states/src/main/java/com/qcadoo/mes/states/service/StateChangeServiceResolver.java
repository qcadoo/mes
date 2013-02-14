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
package com.qcadoo.mes.states.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.qcadoo.model.api.DataDefinition;

@Service
public class StateChangeServiceResolver {

    private final Map<String, StateChangeService> servicesMap;

    public StateChangeServiceResolver() {
        this.servicesMap = Maps.newHashMap();
    }

    public void register(final DataDefinition dataDefinition, final StateChangeService service) {
        register(buildKey(dataDefinition), service);
    }

    public void register(final String pluginIdentifier, final String modelName, final StateChangeService service) {
        register(buildKey(pluginIdentifier, modelName), service);
    }

    private void register(final String key, final StateChangeService service) {
        servicesMap.put(key, service);
    }

    public void unregister(final StateChangeService service) {
        servicesMap.values().remove(service);
    }

    public void unregister(final DataDefinition dataDefinition) {
        unregister(buildKey(dataDefinition));
    }

    public void unregister(final String pluginIdentifier, final String modelName) {
        unregister(buildKey(pluginIdentifier, modelName));
    }

    private void unregister(final String key) {
        servicesMap.put(key, null);
    }

    public StateChangeService get(final DataDefinition dataDefinition) {
        return get(buildKey(dataDefinition));
    }

    public StateChangeService get(final String pluginIdentifier, final String modelName) {
        return get(buildKey(pluginIdentifier, modelName));
    }

    private StateChangeService get(final String key) {
        return servicesMap.get(key);
    }

    private String buildKey(final DataDefinition dataDefinition) {
        return buildKey(dataDefinition.getPluginIdentifier(), dataDefinition.getName());
    }

    private String buildKey(final String pluginIdentifier, final String modelName) {
        final StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append(pluginIdentifier);
        keyBuilder.append('.');
        keyBuilder.append(modelName);
        return keyBuilder.toString();
    }

}
