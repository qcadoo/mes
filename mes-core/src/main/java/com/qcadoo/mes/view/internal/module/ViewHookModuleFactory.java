package com.qcadoo.mes.view.internal.module;

import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Preconditions;
import com.qcadoo.mes.api.ViewDefinitionService;
import com.qcadoo.mes.view.HookDefinition;
import com.qcadoo.mes.view.ViewDefinition;
import com.qcadoo.mes.view.hooks.internal.HookFactory;
import com.qcadoo.plugin.api.ModuleFactory;

public class ViewHookModuleFactory implements ModuleFactory<ViewHookModule> {

    @Autowired
    private HookFactory hookFactory;

    @Autowired
    private ViewDefinitionService viewDefinitionService;

    @Override
    public void init() {
    }

    @Override
    public ViewHookModule parse(final String pluginIdentifier, final Element element) {
        String plugin = element.getAttributeValue("plugin");
        String view = element.getAttributeValue("view");
        String hookTypeStr = element.getAttributeValue("hookType");
        String bean = element.getAttributeValue("bean");
        String method = element.getAttributeValue("method");
        Preconditions.checkNotNull(plugin, "View hook extension error: plugin not defined");
        Preconditions.checkNotNull(view, "View hook extension error: view not defined");
        Preconditions.checkNotNull(hookTypeStr, "View hook extension error: hookType not defined");
        Preconditions.checkNotNull(bean, "View hook extension error: bean not defined");
        Preconditions.checkNotNull(method, "View hook extension error: method not defined");

        HookDefinition hook = hookFactory.getHook(bean, method);

        ViewDefinition.HookType hookType;
        if ("postConstructHook".equals(hookTypeStr)) {
            hookType = ViewDefinition.HookType.POST_CONSTRUCT;
        } else if ("postInitializeHook".equals(hookTypeStr)) {
            hookType = ViewDefinition.HookType.POST_INITIALIZE;
        } else if ("preInitializeHook".equals(hookTypeStr)) {
            hookType = ViewDefinition.HookType.PRE_INITIALIZE;
        } else if ("preRenderHook".equals(hookTypeStr)) {
            hookType = ViewDefinition.HookType.PRE_RENDER;
        } else {
            throw new IllegalStateException("Unknow view extension hook type: " + hookTypeStr);
        }

        return new ViewHookModule(viewDefinitionService, plugin, view, hookType, hook);
    }

    @Override
    public String getIdentifier() {
        return "viewHookExtension";
    }

}
