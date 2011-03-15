package com.qcadoo.plugin.integration;

import org.w3c.dom.Node;

import com.qcadoo.plugin.internal.api.ModuleFactory;

public class MockModuleFactory implements ModuleFactory<MockModule> {

    public static int postInitializeCallCount = 0;

    @Override
    public void postInitialize() {
        postInitializeCallCount++;
    }

    @Override
    public MockModule parse(final Node node) {
        return new MockModule();
    }

    @Override
    public String getIdentifier() {
        return "mock";
    }

    public static int getPostInitializeCallCount() {
        return postInitializeCallCount;
    }

}
