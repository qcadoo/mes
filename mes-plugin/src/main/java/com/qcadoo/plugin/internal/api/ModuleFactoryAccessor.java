package com.qcadoo.plugin.internal.api;

public interface ModuleFactoryAccessor {

    void init();

    ModuleFactory<?> getModuleFactory(String identifier);

}