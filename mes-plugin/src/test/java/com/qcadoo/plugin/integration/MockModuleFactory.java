package com.qcadoo.plugin.integration;

import org.jdom.Element;

import com.qcadoo.plugin.internal.api.ModuleFactory;

public class MockModuleFactory implements ModuleFactory<MockModule> {

    @Override
    public void postInitialize() {
    }

    @Override
    public MockModule parse(final String pluginIdentifier, final Element element) {
        return new MockModule();
    }

    @Override
    public String getIdentifier() {
        return "mock";
    }

}
