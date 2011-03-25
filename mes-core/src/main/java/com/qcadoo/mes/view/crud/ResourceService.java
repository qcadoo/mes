package com.qcadoo.mes.view.crud;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Service;

@Service
public class ResourceService {

    private Set<ResourceModule> resourceModules = new HashSet<ResourceModule>();

    public void addResourceModule(final ResourceModule resourceModule) {
        resourceModules.add(resourceModule);
    }

    public void removeResourceModule(final ResourceModule resourceModule) {
        resourceModules.remove(resourceModule);
    }

    public void serveResource(final HttpServletRequest request, final HttpServletResponse response) {

        boolean resourceServed = false;
        for (ResourceModule resourceModule : resourceModules) {
            if (resourceModule.serveResource(request, response)) {
                resourceServed = true;
                continue;
            }
        }
        if (!resourceServed) {
            try {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "resource not found");
            } catch (IOException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }

}
