package com.qcadoo.mes.view.internal.module.resourceModule;

import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.google.common.base.Preconditions;
import com.qcadoo.plugin.api.ModuleFactory;

public class ResourceModuleFactory implements ModuleFactory<ResourceModule> {

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void init() {
        // empty
    }

    @Override
    public ResourceModule parse(final String pluginIdentifier, final Element element) {
        String uri = element.getAttributeValue("uri");
        Preconditions.checkNotNull(uri, "Resource module error: uri not defined");

        // TODO mina add slash when not defined

        return new ResourceModule(resourceService, applicationContext, uri);
    }

    @Override
    public String getIdentifier() {
        return "resource";
    }
}
