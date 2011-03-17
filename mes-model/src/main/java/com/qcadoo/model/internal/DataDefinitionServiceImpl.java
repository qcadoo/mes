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

package com.qcadoo.model.internal;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.aop.Monitorable;
import com.qcadoo.model.internal.api.InternalDataDefinitionService;

@Service
public final class DataDefinitionServiceImpl implements InternalDataDefinitionService {

    private final Map<String, DataDefinition> enabledDataDefinitions = new HashMap<String, DataDefinition>();

    private final Map<String, DataDefinition> dataDefinitions = new HashMap<String, DataDefinition>();

    @Override
    @Monitorable
    public DataDefinition get(final String pluginIdentifier, final String modelName) {
        DataDefinition dataDefinition = enabledDataDefinitions.get(pluginIdentifier + "." + modelName);
        checkNotNull(dataDefinition, "data definition for %s#%s cannot be found", pluginIdentifier, modelName);
        return dataDefinition;
    }

    @Override
    @Monitorable
    public List<DataDefinition> list() {
        return new ArrayList<DataDefinition>(enabledDataDefinitions.values());
    }

    @Override
    @Monitorable
    public void save(final DataDefinition dataDefinition) {
        dataDefinitions.put(dataDefinition.getPluginIdentifier() + "." + dataDefinition.getName(), dataDefinition);
        if (enabledDataDefinitions.containsKey(dataDefinition.getPluginIdentifier() + "." + dataDefinition.getName())) {
            enabledDataDefinitions.put(dataDefinition.getPluginIdentifier() + "." + dataDefinition.getName(), dataDefinition);
        }
    }

    @Override
    public DataDefinition getAll(final String pluginIdentifier, final String modelName) {
        DataDefinition dataDefinition = dataDefinitions.get(pluginIdentifier + "." + modelName);
        checkNotNull(dataDefinition, "data definition for %s#%s cannot be found", pluginIdentifier, modelName);
        return dataDefinition;
    }

    @Override
    public List<DataDefinition> listAll() {
        return new ArrayList<DataDefinition>(dataDefinitions.values());
    }

    @Override
    public void disable(final String pluginIdentifier, final String modelName) {
        enabledDataDefinitions.remove(pluginIdentifier + "." + modelName);
    }

    @Override
    public void enable(final String pluginIdentifier, final String modelName) {
        if (dataDefinitions.containsKey(pluginIdentifier + "." + modelName)) {
            enabledDataDefinitions.put(pluginIdentifier + "." + modelName,
                    dataDefinitions.get(pluginIdentifier + "." + modelName));
        }
    }

}
