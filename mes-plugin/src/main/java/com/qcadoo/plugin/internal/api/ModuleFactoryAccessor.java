package com.qcadoo.plugin.internal.api;

public interface ModuleFactoryAccessor {

    void postInitialize();

    ModuleFactory<?> getModuleFactory(String identifier);

}