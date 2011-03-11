package com.qcadoo.plugin.internal.api;

import java.util.Set;

import org.springframework.core.io.Resource;

public interface PluginDescriptorResolver {

    Set<Resource> getDescriptors();

}
