package com.qcadoo.mes.view.internal.module.resourceModule;

import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

import com.qcadoo.plugin.api.Module;
import com.qcadoo.plugin.api.PluginState;

public class ResourceModule extends Module {

    private final ResourceService resourceService;

    private final ApplicationContext applicationContext;

    private final String uriRoot;

    public ResourceModule(final ResourceService resourceService, final ApplicationContext applicationContext, final String uriRoot) {
        this.resourceService = resourceService;
        this.applicationContext = applicationContext;
        this.uriRoot = uriRoot;
    }

    @Override
    public void init(final PluginState state) {
        // empty
    }

    @Override
    public void enable() {
        resourceService.addResourceModule(this);
    }

    @Override
    public void disable() {
        resourceService.removeResourceModule(this);
    }

    public Resource getResource(final String uri) {

        Resource resource = applicationContext.getResource("classpath:" + uriRoot + uri);

        if (resource != null && resource.exists()) {
            return resource;
        }
        return null;
    }
}
