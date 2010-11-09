package com.qcadoo.mes.plugins.internal.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarFile;

import javax.annotation.PostConstruct;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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

    public void removePluginFile(final String fileName) throws PluginException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Removing file: " + fileName);
        }
        // A File object to represent the filename
        File f = new File(fileName);

        // Make sure the file or directory exists and isn't write protected
        if (!f.exists()) {
            throw new PluginException("Delete: no such file or directory: " + fileName);
        }
        if (!f.canWrite()) {
            throw new PluginException("Delete: write protected: " + fileName);
        }
        // If it is a directory, make sure it is empty
        if (f.isDirectory()) {
            throw new PluginException("Delete: this is a directory: " + fileName);
        }

        // Attempt to delete it
        boolean success = f.delete();
        if (!success) {
            throw new PluginException("Delete: deletion failed");
        }
    }

    public void movePluginFile(final String filePath, final String dirPath) throws PluginException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Moving file: " + filePath + " to: " + dirPath);
        }
        // File (or directory) to be moved
        File file = new File(filePath);
        // Destination directory
        File dir = new File(dirPath);
        // Move file to new directory

        boolean success = file.renameTo(new File(dir, file.getName()));
        if (!success) {
            throw new PluginException("Move: move failed");
        }
    }

    public PluginsPlugin readDescriptor(final File file) throws IOException, ParserConfigurationException, SAXException {

        PluginsPlugin plugin = new PluginsPlugin();
        JarFile jarFile = new JarFile(file);

        InputStream in = jarFile.getInputStream(jarFile.getEntry(DESCRIPTOR));

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();

        Document doc = db.parse(in);

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

    public void removeResources(final String type, final String targetPath) {
        LOG.info("Removing resources " + type + " ...");

        deleteDirectory(new File(targetPath));
    }

    private boolean deleteDirectory(final File path) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Recursive removing directory: " + path);
        }
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    boolean success = files[i].delete();
                    if (!success) {
                        LOG.error("Problem with removing file");
                    }
                }
            }
        }
        return (path.delete());
    }

}
