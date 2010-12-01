/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.plugins.internal.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import javax.annotation.PostConstruct;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.qcadoo.mes.beans.plugins.PluginsPlugin;
import com.qcadoo.mes.plugins.internal.enums.PluginDescriptorProperties;
import com.qcadoo.mes.plugins.internal.exceptions.PluginException;

@Component
public final class PluginUtil {

    private static final Logger LOG = LoggerFactory.getLogger(PluginUtil.class);

    private static final String DESCRIPTOR = "plugin.xml";

    @Value("${QCADOO_RESTART_CMD}")
    private String restartCommand;

    @PostConstruct
    public void init() {
        LOG.info("Restart command: " + restartCommand);
    }

    public File transferFileToTmp(final MultipartFile file, final String tmpPath) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Transfering file: " + file.getOriginalFilename() + " to tmp");
        }
        File tmpDir = new File(tmpPath);
        if (!tmpDir.exists()) {
            boolean success = tmpDir.mkdir();
            if (!success) {
                LOG.error("Problem with creating tmp directory");
                throw new IOException("Error with creating directory");
            }
        }
        File pluginFile = new File(tmpDir, file.getOriginalFilename());
        file.transferTo(pluginFile);

        return pluginFile;
    }

    public void removePluginFile(final String fileName, final boolean onExit) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Removing file: " + fileName);
        }
        // A File object to represent the filename
        File f = new File(fileName);

        // Attempt to delete it
        if (onExit) {
            try {
                FileUtils.forceDelete(f);
            } catch (IOException e) {
                LOG.error("Problem with deleting file - " + e.getMessage());
                LOG.info("Trying delete file after JVM stop");
                FileUtils.forceDeleteOnExit(f);
            }
        } else {
            FileUtils.forceDelete(f);
        }
    }

    public void movePluginFile(final String filePath, final String dirPath) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Moving file: " + filePath + " to: " + dirPath);
        }
        // File (or directory) to be moved
        File file = new File(filePath);
        // Destination directory
        File dir = new File(dirPath);
        // Move file to new directory

        FileUtils.moveFile(file, new File(dir, file.getName()));
    }

    public PluginsPlugin readDescriptor(final File file) throws IOException, ParserConfigurationException, SAXException {

        PluginsPlugin plugin = new PluginsPlugin();
        JarFile jarFile = new JarFile(file);

        ZipEntry entry = jarFile.getEntry(DESCRIPTOR);
        if (entry == null) {
            throw new IOException("Plugin descriptor not found");
        }
        InputStream in = jarFile.getInputStream(entry);

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();

        Document doc = db.parse(in);
        in.close();
        jarFile.close();
        doc.getDocumentElement().normalize();

        for (PluginDescriptorProperties property : PluginDescriptorProperties.values()) {
            String value = null;
            Node fstNode = doc.getElementsByTagName(property.getValue()).item(0);
            if (fstNode.getNodeType() == Node.ELEMENT_NODE && fstNode.getFirstChild() != null) {
                value = ((Element) fstNode).getFirstChild().getNodeValue();
            }

            switch (property) {
                case IDENTIFIER:
                    plugin.setIdentifier(value);
                    break;
                case NAME:
                    plugin.setName(value);
                    break;
                case PACKAGE_NAME:
                    plugin.setPackageName(value);
                    break;
                case VERSION:
                    plugin.setVersion(value);
                    break;
                case VENDOR:
                    plugin.setVendor(value);
                    break;
                case DESCRIPTION:
                    plugin.setDescription(value);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown element node");
            }

        }

        return plugin;
    }

    public void restartServer() throws PluginException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Command path: " + restartCommand);
        }
        try {
            Process shutdownProcess = Runtime.getRuntime().exec(restartCommand);
            shutdownProcess.waitFor();

            if (LOG.isDebugEnabled()) {
                LOG.debug("Shutdown exit value: " + shutdownProcess.exitValue());
            }
        } catch (IOException e) {
            throw new PluginException("Restart failed - " + e.getMessage(), e);
        } catch (InterruptedException e) {
            throw new PluginException("Restart failed - " + e.getMessage(), e);
        }

    }

    public void removeResources(final String type, final String targetPath) throws IOException {
        LOG.info("Removing resources " + type + " ...");

        File f = new File(targetPath);
        if (f.exists()) {
            try {
                FileUtils.forceDelete(f);
            } catch (IOException e) {
                LOG.error("Problem with deleting file - " + e.getMessage());
                LOG.info("Trying delete file after JVM stop");
                FileUtils.forceDeleteOnExit(f);
            }
        }
    }

}
