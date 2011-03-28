/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.3.0
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */

package com.qcadoo.mes.application;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.yahoo.platform.yui.compressor.YUICompressor;

@Component
public final class PluginResourcesResolver implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(PluginResourcesResolver.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Value("${QCADOO_WEBAPP_PATH}")
    private String webappPath;

    @Value("${copyPluginResources}")
    private boolean copyPluginResources;

    @Value("${compressStaticResources}")
    private boolean compressStaticResources;

    @PostConstruct
    public void init() {
        LOG.info("Webapp path: " + webappPath);
    }

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent event) {
        if (copyPluginResources) {
            // copyResources("js", "js");
            // copyResources("css", "css");
            // copyResources("img", "img");
            // copyResources("WEB-INF/jsp", "WEB-INF/jsp");

            try {
                if (compressStaticResources) {
                    compressResources("js", "js");
                    // TODO plugin masz dla css musimy zmieniać znacznik "url"
                    // jeśli zaczyna się od "/" usuwamy go
                    // jeśli nie, dodajemy ścieżke do pliku
                    // chodzi o to aby wszystkie "url" były względne do qcadoo.css
                    compressResources("css", "css");
                }
            } catch (IOException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }

    private static void mergeFiles(final Writer output, final File folder, final String type, final List<String> excluded)
            throws IOException {
        File[] files = folder.listFiles();

        Arrays.sort(files, new Comparator<File>() {

            @Override
            public int compare(final File f1, final File f2) {
                return f1.getName().compareTo(f2.getName());
            }
        });

        for (File file : files) {
            if (file.isDirectory()) {
                mergeFiles(output, file, type, excluded);
            } else if (file.isFile() && !excluded.contains(file.getName()) && file.getName().endsWith("." + type)) {
                Reader input = new FileReader(file);
                IOUtils.copy(input, output);
                output.write("\n\n");
                input.close();
            }
        }
    }

    private void compressResources(final String type, final String targetPath) throws IOException {
        File file = new File(webappPath + "/" + targetPath + "/qcadoo." + type);
        File minFile = new File(webappPath + "/" + targetPath + "/qcadoo.min." + type);

        if (file.exists()) {
            file.delete();
        }

        if (minFile.exists()) {
            minFile.delete();
        }

        Writer output = new BufferedWriter(new FileWriter(file));

        File folder = new File(webappPath + "/" + targetPath);

        mergeFiles(output, folder, type, Arrays.asList(new String[] { "qcadoo." + type, "qcadoo.min." + type }));

        output.flush();
        output.close();

        YUICompressor.main(new String[] { "-v", "-o", minFile.getAbsolutePath(), file.getAbsolutePath() });
    }

    // private void copyResources(final String type, final String targetPath) {
    // LOG.info("Copying resources " + type + " ...");
    //
    // try {
    // Resource[] resources = applicationContext.getResources("classpath*:META-INF/" + type + "/**/*");
    //
    // for (Resource resource : resources) {
    // copyResource(resource, type, targetPath);
    // }
    // } catch (IOException e) {
    // throw new IllegalStateException("Cannot find resources " + type + " in classpath", e);
    // }
    // }

    // private void copyResource(final Resource resource, final String type, final String targetPath) {
    // if (!resource.isReadable()) {
    // return;
    // }
    //
    // try {
    // String path = resource.getURI().toString().split("META-INF/" + type)[1];
    // File file = new File(webappPath + "/" + targetPath + path);
    // copyFile(resource, path, file);
    // } catch (IOException e) {
    // throw new IllegalStateException("Cannot copy resource " + resource, e);
    // }
    // }

    // private void copyFile(final Resource resource, final String path, final File file) throws IOException {
    // if (resource.getInputStream().available() == 0) {
    // file.mkdirs();
    // } else {
    // if (LOG.isDebugEnabled()) {
    // LOG.debug("Copying " + path + " to " + file.getAbsolutePath());
    // }
    //
    // OutputStream output = null;
    //
    // try {
    // output = new BufferedOutputStream(new FileOutputStream(file));
    // IOUtils.copy(resource.getInputStream(), output);
    // } finally {
    // IOUtils.closeQuietly(output);
    // }
    // }
    // }
}
