package com.qcadoo.mes.view.internal.module;

import com.google.common.base.Preconditions;
import com.qcadoo.mes.api.ViewDefinitionService;
import com.qcadoo.mes.view.ComponentPattern;
import com.qcadoo.mes.view.ViewDefinition;
import com.qcadoo.mes.view.internal.ComponentCustomEvent;
import com.qcadoo.plugin.api.Module;
import com.qcadoo.plugin.api.PluginState;

public class ViewListenerModule extends Module {

    private final ViewDefinitionService viewDefinitionService;

    private final String extendsViewPlugin;

    private final String extendsViewName;

    private final String extendsComponentName;

    private final ComponentCustomEvent event;

    public ViewListenerModule(final ViewDefinitionService viewDefinitionService, final String extendsViewPlugin,
            final String extendsViewName, final String extendsComponentName, final ComponentCustomEvent event) {
        this.viewDefinitionService = viewDefinitionService;
        this.extendsViewPlugin = extendsViewPlugin;
        this.extendsViewName = extendsViewName;
        this.extendsComponentName = extendsComponentName;
        this.event = event;
    }

    @Override
    public void init(final PluginState state) {
        if (PluginState.ENABLED.equals(state)) {
            enable();
        }
    }

    @Override
    public void enable() {
        getComponent().addCustomEvent(event);
    }

    @Override
    public void disable() {
        getComponent().removeCustomEvent(event);
    }

    private ComponentPattern getComponent() {
        ViewDefinition extendsView = viewDefinitionService.getWithoutSession(extendsViewPlugin, extendsViewName);
        Preconditions.checkNotNull(extendsView,
                "View Listener extension error: View listener extension referes to view which not exists (" + extendsViewPlugin
                        + " - " + extendsViewName + ")");
        ComponentPattern component = extendsView.getComponentByReference(extendsComponentName);
        Preconditions.checkNotNull(component, "View Listener extension error: Component '" + extendsComponentName
                + "' not exists in view " + extendsViewPlugin + " - " + extendsViewName + ")");
        return component;
    }

}
