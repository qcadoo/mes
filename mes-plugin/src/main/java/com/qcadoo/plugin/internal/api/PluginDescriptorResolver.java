package com.qcadoo.plugin.internal.api;

import org.springframework.core.io.Resource;

public interface PluginDescriptorResolver {

    Resource[] getDescriptors();

}
