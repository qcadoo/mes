/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright © Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.application;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public final class PluginResourcesResolver implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(PluginResourcesResolver.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Value("${QCADOO_WEBAPP_PATH}")
    private String webappPath;

    @PostConstruct
    public void init() {
        LOG.info("Webapp path: " + webappPath);
    }

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent event) {
        boolean copyResources = true;
        String copyResorcesProperty = System.getProperty("com.qcadoo.mes.application.copyResources");
        if (copyResorcesProperty != null) {
            copyResources = Boolean.parseBoolean(copyResorcesProperty);
        }
        if (copyResources) {
            copyResources("js", "js");
            copyResources("css", "css");
            copyResources("img", "img");
            copyResources("WEB-INF/jsp", "WEB-INF/jsp");
        }
    }

    private void copyResources(final String type, final String targetPath) {
        LOG.info("Copying resources " + type + " ...");

        try {
            Resource[] resources = applicationContext.getResources("classpath*:META-INF/" + type + "/**/*");

            for (Resource resource : resources) {
                copyResource(resource, type, targetPath);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot find resources " + type + " in classpath", e);
        }
    }

    private void copyResource(final Resource resource, final String type, final String targetPath) {
        if (!resource.isReadable()) {
            return;
        }

        try {
            String path = resource.getURI().toString().split("META-INF/" + type)[1];
            File file = new File(webappPath + "/" + targetPath + path);
            copyFile(resource, path, file);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot copy resource " + resource, e);
        }
    }

    private void copyFile(final Resource resource, final String path, final File file) throws IOException {
        if (resource.getInputStream().available() == 0) {
            file.mkdirs();
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Copying " + path + " to " + file.getAbsolutePath());
            }

            OutputStream output = null;

            try {
                output = new BufferedOutputStream(new FileOutputStream(file));
                IOUtils.copy(resource.getInputStream(), output);
            } finally {
                IOUtils.closeQuietly(output);
            }
        }
    }
}
