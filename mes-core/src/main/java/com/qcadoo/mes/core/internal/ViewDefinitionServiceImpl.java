package com.qcadoo.mes.core.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.mes.beans.plugins.PluginsPlugin;
import com.qcadoo.mes.core.api.PluginManagementService;
import com.qcadoo.mes.core.api.ViewDefinitionService;
import com.qcadoo.mes.core.enums.PluginStatus;
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
        if (viewDefinition != null) {
            PluginsPlugin plugin = pluginManagementService.getPluginByIdentifierAndStatus(viewDefinition.getPluginIdentifier(),
                    PluginStatus.ACTIVE.getValue());
            if (plugin != null) {
                return viewDefinition;
            }
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ViewDefinition> list() {
        List<ViewDefinition> viewsList = new ArrayList<ViewDefinition>();
        List<PluginsPlugin> activePluginList = pluginManagementService.getActivePlugins();
        for (PluginsPlugin activePlugin : activePluginList) {
            for (ViewDefinition viewDefinition : viewDefinitions.values()) {
                if (activePlugin.getIdentifier().equals(viewDefinition.getPluginIdentifier())) {
                    viewsList.add(viewDefinition);
                }
            }
        }

        Collections.sort(viewsList, new Comparator<ViewDefinition>() {

            @Override
            public int compare(final ViewDefinition v1, final ViewDefinition v2) {
                return v1.getName().compareTo(v2.getName());
            }

        });

        return viewsList;
    }

    @Override
    public void save(final ViewDefinition viewDefinition) {
        viewDefinitions.put(viewDefinition.getPluginIdentifier() + "." + viewDefinition.getName(), viewDefinition);
    }

    @Override
    public void delete(final String pluginIdentifier, final String viewName) {
        viewDefinitions.remove(pluginIdentifier + "." + viewName);
    }

}
