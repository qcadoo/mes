package com.qcadoo.mes.internal;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.mes.api.PluginManagementService;
import com.qcadoo.mes.api.ViewDefinitionService;
import com.qcadoo.mes.beans.plugins.PluginsPlugin;
import com.qcadoo.mes.enums.PluginStatus;
import com.qcadoo.mes.model.aop.internal.Monitorable;
import com.qcadoo.mes.view.ViewDefinition;

@Service
public final class ViewDefinitionServiceImpl implements ViewDefinitionService {

    @Autowired
    private PluginManagementService pluginManagementService;

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
        ViewDefinition viewDefinition = viewDefinitions.get(pluginIdentifier + "." + viewName);
        checkNotNull(viewDefinition, "view definition for %s#%s cannot be found", pluginIdentifier, viewName);
        checkState(belongsToActivePlugin(viewDefinition.getPluginIdentifier()),
                "view definition %s belongs to inactive plugin %s", viewName, pluginIdentifier);
        return viewDefinition;
    }

    @Override
    @Transactional(readOnly = true)
    @Monitorable
    public List<ViewDefinition> listForMenu() {
        List<ViewDefinition> menuableViews = new LinkedList<ViewDefinition>();
        for (ViewDefinition viewDefinition : viewDefinitions.values()) {
            if (viewDefinition.isMenuable()) {
                menuableViews.add(viewDefinition);
            }
        }
        return menuableViews;
    }

    private boolean belongsToActivePlugin(final String pluginIdentifier) {
        if (pluginIdentifier == null) {
            return true;
        }
        PluginsPlugin plugin = pluginManagementService.getByIdentifierAndStatus(pluginIdentifier, PluginStatus.ACTIVE.getValue());
        return (plugin != null);
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
