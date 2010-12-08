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

package com.qcadoo.mes.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.mes.api.ViewDefinitionService;
import com.qcadoo.mes.model.aop.internal.Monitorable;
import com.qcadoo.mes.view.ViewDefinition;

@Service
public final class ViewDefinitionServiceImpl implements ViewDefinitionService {

    private final Map<String, ViewDefinition> viewDefinitions = new HashMap<String, ViewDefinition>();

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
        return viewDefinitions.get(pluginIdentifier + "." + viewName);
    }

    @Override
    @Transactional(readOnly = true)
    @Monitorable
    public List<ViewDefinition> listForMenu() {
        List<ViewDefinition> menuableViews = new LinkedList<ViewDefinition>();
        for (ViewDefinition viewDefinition : viewDefinitions.values()) {
            if (viewDefinition.isMenuAccessible()) {
                menuableViews.add(viewDefinition);
            }
        }
        return menuableViews;
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
    }

    @Override
    @Transactional
    @Monitorable
    public void delete(final ViewDefinition viewDefinition) {
        viewDefinitions.remove(viewDefinition.getPluginIdentifier() + "." + viewDefinition.getName());
    }

}
