package com.qcadoo.mes.view.internal.module.resourceModule;

import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.google.common.base.Preconditions;
import com.qcadoo.mes.view.crud.ResourceService;
import com.qcadoo.plugin.api.ModuleFactory;

public class UniversalResourceModuleFactory implements ModuleFactory<UniversalResourceModule> {

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void init() {
        // empty
    }

    @Override
    public UniversalResourceModule parse(final String pluginIdentifier, final Element element) {
        String uri = element.getAttributeValue("uri");
        Preconditions.checkNotNull(uri, "Resource module error: uri not defined");
        return new UniversalResourceModule(resourceService, applicationContext, pluginIdentifier, uri);
    }

    @Override
    public String getIdentifier() {
        return "resource";
    }
}
