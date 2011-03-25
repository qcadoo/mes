package com.qcadoo.mes.view.internal.module.resourceModule;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import com.qcadoo.mes.view.crud.ResourceModule;
import com.qcadoo.mes.view.crud.ResourceService;

public class UniversalResourceModule extends ResourceModule {

    private final ApplicationContext applicationContext;

    private final String uriPattern;

    private final PathMatcher matcher = new AntPathMatcher();

    public UniversalResourceModule(final ResourceService resourceService, final ApplicationContext applicationContext,
            final String pluginIdentifier, final String uriPattern) {
        super(resourceService);
        this.applicationContext = applicationContext;
        if (uriPattern.startsWith("/")) {
            this.uriPattern = "/" + pluginIdentifier + uriPattern;
        } else {
            this.uriPattern = "/" + pluginIdentifier + "/" + uriPattern;
        }

    }

    @Override
    public boolean serveResource(final HttpServletRequest request, final HttpServletResponse response) {
        Resource resource = getResourceFromURI(request.getRequestURI());
        if (resource != null && resource.exists()) {
            response.setContentType(getContentTypeFromURI(request.getRequestURI()));
            try {
                copy(resource.getInputStream(), response.getOutputStream());
            } catch (IOException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
            return true;
        } else {
            return false;
        }
    }

    private boolean matchPattern(final String uri) {
        return matcher.match(uriPattern, uri);
    }

    private Resource getResourceFromURI(final String uri) {
        if (matchPattern(uri)) {
            return applicationContext.getResource("classpath:" + uri);
        }
        return null;
    }

    private String getContentTypeFromURI(final String uri) {
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
