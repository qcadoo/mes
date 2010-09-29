package com.qcadoo.mes.core.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.mes.beans.plugins.PluginsPlugin;
import com.qcadoo.mes.core.api.PluginManagementService;
import com.qcadoo.mes.core.api.ViewDefinitionService;
import com.qcadoo.mes.core.view.ViewDefinition;

@Service
public final class ViewDefinitionServiceImpl implements ViewDefinitionService {

    @Autowired
    private PluginManagementService pluginManagementService;

    private final Map<String, ViewDefinition> viewDefinitions = new HashMap<String, ViewDefinition>();

    @Override
    @Transactional(readOnly = true)
    public ViewDefinition get(final String pluginIdentifier, final String viewName) {
        ViewDefinition viewDefinition = viewDefinitions.get(pluginIdentifier + "." + viewName);
        if (viewDefinition != null && belongsToActivePlugin(viewDefinition)) {
            return viewDefinition;
        } else {
            return viewDefinition;
        }
    }

    private boolean belongsToActivePlugin(final ViewDefinition viewDefinition) {
        PluginsPlugin plugin = pluginManagementService.getPluginByIdentifierAndStatus(viewDefinition.getPluginIdentifier(),
                "active");
        return (plugin != null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ViewDefinition> list() {
        return new ArrayList<ViewDefinition>(viewDefinitions.values());
    }

    @Override
    @Transactional
    public void save(final ViewDefinition viewDefinition) {
        viewDefinitions.put(viewDefinition.getPluginIdentifier() + "." + viewDefinition.getName(), viewDefinition);
    }

    @Override
    @Transactional
    public void delete(final String pluginIdentifier, final String viewName) {
        viewDefinitions.remove(pluginIdentifier + "." + viewName);
    }

}
