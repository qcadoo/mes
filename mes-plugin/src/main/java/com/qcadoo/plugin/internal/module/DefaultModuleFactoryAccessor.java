package com.qcadoo.plugin.internal.module;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.qcadoo.plugin.internal.api.ModuleFactory;
import com.qcadoo.plugin.internal.api.ModuleFactoryAccessor;

public final class DefaultModuleFactoryAccessor implements ModuleFactoryAccessor {

    private final Map<String, ModuleFactory<?>> moduleFactoryRegistry = new LinkedHashMap<String, ModuleFactory<?>>();

    @Override
    public void init() {
        for (ModuleFactory<?> moduleFactory : moduleFactoryRegistry.values()) {
            moduleFactory.init();
        }
    }

    @Override
    public ModuleFactory<?> getModuleFactory(final String identifier) {
        if (!moduleFactoryRegistry.containsKey(identifier)) {
            throw new IllegalStateException("ModuleFactory " + identifier + " is not defined");
        }
        return moduleFactoryRegistry.get(identifier);
    }

    public void setModuleFactories(final List<ModuleFactory<?>> moduleFactories) {
        for (ModuleFactory<?> moduleFactory : moduleFactories) {
            if (moduleFactoryRegistry.containsKey(moduleFactory.getIdentifier())) {
                throw new IllegalStateException("ModuleFactory " + moduleFactory.getClass().getCanonicalName()
                        + " try to overwrite existing module with identifier " + moduleFactory.getIdentifier());
            }
            moduleFactoryRegistry.put(moduleFactory.getIdentifier(), moduleFactory);
        }
    }

}
