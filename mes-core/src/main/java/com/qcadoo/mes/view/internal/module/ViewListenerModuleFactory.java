package com.qcadoo.mes.view.internal.module;

import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Preconditions;
import com.qcadoo.mes.api.ViewDefinitionService;
import com.qcadoo.mes.view.hooks.internal.HookDefinitionImpl;
import com.qcadoo.mes.view.hooks.internal.HookFactory;
import com.qcadoo.mes.view.internal.ComponentCustomEvent;
import com.qcadoo.plugin.internal.api.ModuleFactory;

public class ViewListenerModuleFactory implements ModuleFactory<ViewListenerModule> {

    @Autowired
    private HookFactory hookFactory;

    @Autowired
    private ViewDefinitionService viewDefinitionService;

    @Override
    public void init() {
    }

    @Override
    public ViewListenerModule parse(final String pluginIdentifier, final Element element) {
        String plugin = element.getAttributeValue("plugin");
        String view = element.getAttributeValue("view");
        String component = element.getAttributeValue("component");
        String eventName = element.getAttributeValue("event");
        String bean = element.getAttributeValue("bean");
        String method = element.getAttributeValue("method");
        Preconditions.checkNotNull(plugin, "View listener extension error: plugin not defined");
        Preconditions.checkNotNull(view, "View listener extension error: view not defined");
        Preconditions.checkNotNull(component, "View listener extension error: component not defined");
        Preconditions.checkNotNull(eventName, "View listener extension error: event not defined");
        Preconditions.checkNotNull(bean, "View listener extension error: bean not defined");
        Preconditions.checkNotNull(method, "View listener extension error: method not defined");

        HookDefinitionImpl hook = (HookDefinitionImpl) hookFactory.getHook(bean, method);
        ComponentCustomEvent event = new ComponentCustomEvent(eventName, hook.getObject(), method);

        return new ViewListenerModule(viewDefinitionService, plugin, view, component, event);
    }

    @Override
    public String getIdentifier() {
        return "viewListenerExtension";
    }

}
