package com.qcadoo.plugin.internal.descriptorresolver;

import java.util.Collections;
import java.util.Set;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.qcadoo.plugin.internal.api.PluginDescriptorResolver;

@Service
public class DefaultPluginDescriptorResolver implements PluginDescriptorResolver {

    @Override
    public Set<Resource> getDescriptors() {
        // TODO Auto-generated method stub
        return Collections.emptySet();
    }

}
