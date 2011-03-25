package com.qcadoo.mes.view.internal.module.resourceModule;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.io.Resource;
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

        Resource resource = null;
        for (ResourceModule resourceModule : resourceModules) {
            resource = resourceModule.getResource(request.getRequestURI());
            if (resource != null) {
                continue;
            }
        }

        if (resource != null) {

            response.setContentType(getContentType(request.getRequestURI()));

            try {
                copy(resource.getInputStream(), response.getOutputStream());
            } catch (IOException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }

        } else {

            // TODO mina 404

        }

    }

    private String getContentType(final String uri) {
        String[] arr = uri.split("\\.");
        String ext = arr[arr.length - 1];

        if ("js".equals(ext)) {
            return "text/javascript";
        } else if ("css".equals(ext)) {
            return "text/css";
        } else if ("png".equals(ext)) {
            return "image/png";
        } else {
            // TODO mina more types
        }
        return "";
    }

    private static final int IO_BUFFER_SIZE = 4 * 1024;

    private void copy(final InputStream in, final OutputStream out) throws IOException {
        byte[] b = new byte[IO_BUFFER_SIZE];
        int read;
        while ((read = in.read(b)) != -1) {
            out.write(b, 0, read);
        }
    }
}
