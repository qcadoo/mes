package com.qcadoo.plugin.internal.api;

import java.util.Set;

import org.springframework.core.io.Resource;

public interface PluginXmlResolver {

    Set<Resource> getPluginXmlFiles();

}
