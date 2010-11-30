/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
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
import com.qcadoo.mes.newview.ViewDefinition;

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
