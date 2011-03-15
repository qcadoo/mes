package com.qcadoo.plugin.internal.api;

import java.io.File;

import org.springframework.core.io.Resource;

public interface PluginDescriptorResolver {

    Resource[] getDescriptors();

    Resource getDescriptor(File file);

}
