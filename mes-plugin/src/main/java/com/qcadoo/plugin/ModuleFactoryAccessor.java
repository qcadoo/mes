package com.qcadoo.plugin;

public interface ModuleFactoryAccessor {

    void postInitialize();

    ModuleFactory<?> getModuleFactory(String identifier);

}