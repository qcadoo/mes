package com.qcadoo.mes.view.internal.module;

import com.google.common.base.Preconditions;
import com.qcadoo.mes.api.ViewDefinitionService;
import com.qcadoo.mes.view.HookDefinition;
import com.qcadoo.mes.view.ViewDefinition;
import com.qcadoo.plugin.api.PluginState;
import com.qcadoo.plugin.internal.api.Module;

public class ViewHookModule extends Module {

    private final ViewDefinitionService viewDefinitionService;

    private final String extendsViewPlugin;

    private final String extendsViewName;

    private final ViewDefinition.HookType hookType;

    private final HookDefinition hook;

    public ViewHookModule(final ViewDefinitionService viewDefinitionService, final String extendsViewPlugin,
            final String extendsViewName, final ViewDefinition.HookType hookType, final HookDefinition hook) {
        this.viewDefinitionService = viewDefinitionService;
        this.extendsViewPlugin = extendsViewPlugin;
        this.extendsViewName = extendsViewName;
        this.hookType = hookType;
        this.hook = hook;
    }

    @Override
    public void init(final PluginState state) {
        if (PluginState.ENABLED.equals(state)) {
            enable();
        }
    }

    @Override
    public void enable() {
        getViewDefinition().addHook(hookType, hook);
    }

    @Override
    public void disable() {
        getViewDefinition().removeHook(hookType, hook);
    }

    private ViewDefinition getViewDefinition() {
        ViewDefinition extendsView = viewDefinitionService.getWithoutSession(extendsViewPlugin, extendsViewName);
        Preconditions.checkNotNull(extendsView, "View hook extension referes to view which not exists (" + extendsViewPlugin
                + " - " + extendsViewName + ")");
        return extendsView;
    }

}
