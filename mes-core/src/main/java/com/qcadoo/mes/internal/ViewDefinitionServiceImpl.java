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

package com.qcadoo.mes.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.mes.api.ViewDefinitionService;
import com.qcadoo.mes.utils.Pair;
import com.qcadoo.mes.view.ViewDefinition;
import com.qcadoo.model.api.aop.Monitorable;

@Service
public final class ViewDefinitionServiceImpl implements ViewDefinitionService {

    // @Autowired
    // private ViewDefinitionParser viewDefinitionParser;

    private final List<Pair<String, String>> menuViews = new ArrayList<Pair<String, String>>();

    private final Map<String, ViewDefinition> viewDefinitions = new HashMap<String, ViewDefinition>();

    // private final Map<String, Node> dynamicViewDefinitions = new HashMap<String, Node>();

    @Override
    @Transactional(readOnly = true)
    @Monitorable
    public ViewDefinition get(final String pluginIdentifier, final String viewName) {
        return getWithoutSession(pluginIdentifier, viewName);
    }

    @Override
    @Transactional(readOnly = true)
    @Monitorable
    public ViewDefinition getWithoutSession(final String pluginIdentifier, final String viewName) {
        String key = pluginIdentifier + "." + viewName;
        if (viewDefinitions.containsKey(key)) {
            return viewDefinitions.get(key);
            // } else if (dynamicViewDefinitions.containsKey(key)) {
            // return viewDefinitionParser.parseViewDefinition(dynamicViewDefinitions.get(key), pluginIdentifier, viewName);
        } else {
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Monitorable
    public List<Pair<String, String>> listForMenu() {
        return menuViews;
    }

    @Override
    @Transactional(readOnly = true)
    @Monitorable
    public List<ViewDefinition> list() {
        return new ArrayList<ViewDefinition>(viewDefinitions.values());
    }

    @Override
    @Transactional
    @Monitorable
    public void save(final ViewDefinition viewDefinition) {
        viewDefinitions.put(viewDefinition.getPluginIdentifier() + "." + viewDefinition.getName(), viewDefinition);
        if (viewDefinition.isMenuAccessible()) {
            menuViews.add(new Pair<String, String>(viewDefinition.getPluginIdentifier(), viewDefinition.getName()));
        }
    }

    // @Override
    // @Monitorable
    // public void saveDynamic(final String pluginIdentifier, final String viewName, final boolean isMenuAccessible,
    // final Node viewNode) {
    // dynamicViewDefinitions.put(pluginIdentifier + "." + viewName, viewNode);
    // if (isMenuAccessible) {
    // menuViews.add(new Pair<String, String>(pluginIdentifier, viewName));
    // }
    // }

    @Override
    @Transactional
    @Monitorable
    public void delete(final ViewDefinition viewDefinition) {
        viewDefinitions.remove(viewDefinition.getPluginIdentifier() + "." + viewDefinition.getName());
    }

}
