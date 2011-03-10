package com.qcadoo.plugin;

public interface ModuleFactoryAccessor {

    ModuleFactory<?> getModuleFactory(String identifier);

}