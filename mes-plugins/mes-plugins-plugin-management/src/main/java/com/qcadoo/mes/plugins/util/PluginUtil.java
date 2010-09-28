package com.qcadoo.mes.plugins.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.qcadoo.mes.beans.plugins.PluginsPlugin;
import com.qcadoo.mes.plugins.enums.PluginDescriptorProperties;
import com.qcadoo.mes.plugins.exception.PluginException;

public final class PluginUtil {

    private static final String binPath = "bin/";

    private static final String webappsRegex = "webapps/\\S*/";

    private static final int restartInterval = 1000;

    private static final String descriptor = "plugin.xml";

    private static final Logger LOG = LoggerFactory.getLogger(PluginUtil.class);

    private PluginUtil() {
    }

    public static File transferFileToTmp(final MultipartFile file, final String tmpPath) throws IOException {
        LOG.debug("Transfering file: " + file.getOriginalFilename() + " to tmp");
        File tmpDir = new File(tmpPath);
        if (!tmpDir.exists()) {
            tmpDir.mkdir();
        }
        File pluginFile = new File(tmpPath + file.getOriginalFilename());
        file.transferTo(pluginFile);

        return pluginFile;
    }

    public static void removePluginFile(final String fileName) throws PluginException {
        LOG.debug("Removing file: " + fileName);
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

    public static void movePluginFile(final String filePath, final String dirPath) throws PluginException {
        LOG.debug("Moving file: " + filePath + " to: " + dirPath);
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

    public static PluginsPlugin readDescriptor(final File file) throws IOException, ParserConfigurationException, SAXException {

        PluginsPlugin plugin = new PluginsPlugin();
        JarFile jarFile = new JarFile(file);

        InputStream in = jarFile.getInputStream(jarFile.getEntry(descriptor));

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
            }

        }

        return plugin;
    }

    public static void restartServer(final String webappPath) throws PluginException {
        String[] commandsStop = { "./shutdown.sh" };
        String[] commandsStart = { "./startup.sh" };
        String commandPath = webappPath.replaceAll(webappsRegex, binPath);
        LOG.debug("Command path: " + commandPath);
        try {
            File dir = new File(commandPath);
            Runtime runtime = Runtime.getRuntime();

            Process shutdownProcess = runtime.exec(commandsStop, null, dir);
            shutdownProcess.waitFor();
            LOG.debug("Shutdown exit value: " + shutdownProcess.exitValue());

            Thread.sleep(restartInterval);

            Process startupProcess = runtime.exec(commandsStart, null, dir);
            startupProcess.waitFor();
            LOG.debug("Startup exit value: " + startupProcess.exitValue());

        } catch (IOException e) {
            throw new PluginException("Restart failed");
        } catch (InterruptedException e) {
            throw new PluginException("Restart failed");
        }

    }

    public static void removeResources(final String type, final String targetPath) {
        LOG.info("Removing resources " + type + " ...");

        deleteDirectory(new File(targetPath));
    }

    private static boolean deleteDirectory(final File path) {
        LOG.debug("Recursive removing directory: " + path);
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }

}
