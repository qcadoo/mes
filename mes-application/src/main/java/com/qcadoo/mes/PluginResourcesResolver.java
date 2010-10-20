package com.qcadoo.mes;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.Resource;
import org.springframework.web.context.WebApplicationContext;

import com.yahoo.platform.yui.compressor.YUICompressor;

public final class PluginResourcesResolver implements ApplicationContextAware, ApplicationListener<ContextRefreshedEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(PluginResourcesResolver.class);

    private ApplicationContext applicationContext;

    private String webappPath;

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.webappPath = ((WebApplicationContext) applicationContext).getServletContext().getRealPath("/");
    }

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent event) {
        copyResources("js", "js");
        copyResources("css", "css");
        copyResources("img", "img");
        copyResources("jsp", "WEB-INF/jsp");
        mergeResources("js");
        mergeResources("css");
    }

    @SuppressWarnings("unchecked")
    private void mergeResources(final String type) {
        LOG.info("Merging resources " + type + " ...");

        Collection<File> files = FileUtils.listFiles(new File(webappPath + "/" + type), new String[] { type }, true);

        try {
            FileUtils.deleteQuietly(new File(webappPath + "/" + type + "/qcd.min." + type));
            FileUtils.deleteQuietly(new File(webappPath + "/" + type + "/qcd.all." + type));

            BufferedWriter out = new BufferedWriter(new FileWriter(new File(webappPath + "/" + type + "/qcd.all." + type)));

            String line = null;

            for (File file : files) {
                if (file.canRead() && !file.getName().equals("qcd.min." + type)) {
                    LOG.debug("Merging " + file.getAbsolutePath());
                    BufferedReader in = new BufferedReader(new FileReader(file));
                    while ((line = in.readLine()) != null) {
                        out.append(line).append("\n");
                    }
                    in.close();
                }
            }

            out.close();

            LOG.info("Compressing resources " + type + " ...");

            YUICompressor.main(new String[] { webappPath + "/" + type + "/qcd.all." + type, "-o",
                    webappPath + "/" + type + "/qcd.min." + type });
        } catch (IOException e) {
            throw new IllegalStateException("cannot copy resources: " + type, e);
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
            throw new IllegalStateException("cannot copy resources: " + type, e);
        }
    }

    private void copyResource(final Resource resource, final String type, final String targetPath) throws IOException {
        if (!resource.isReadable()) {
            return;
        }

        String path = resource.getURI().toString().split("META-INF/" + type)[1];
        File file = new File(webappPath + "/" + targetPath + path);

        copyFile(resource, path, file);
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
