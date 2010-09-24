package com.qcadoo.mes.plugins.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.qcadoo.mes.beans.plugins.PluginsPlugin;
import com.qcadoo.mes.core.api.PluginManagementService;

@Controller
public class PluginManagementController {

    private static final Logger LOG = LoggerFactory.getLogger(PluginManagementController.class);

    @Autowired
    private PluginManagementService pluginManagementService;

    @Autowired
    private ApplicationContext applicationContext;

    private static final String libPath = "WEB-INF/lib/";

    private static final String binPath = "../bin/";

    private static final String tmpPath = "/Users/krna/Downloads/tmp/";

    private static final String descriptor = "plugin.xml";

    String webappPath;

    @RequestMapping(value = "download", method = RequestMethod.GET)
    public ModelAndView getDownloadPageView() {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("download");
        return mav;
    }

    @RequestMapping(value = "download", method = RequestMethod.POST)
    @Transactional
    public String handleDownload(@RequestParam("file") final MultipartFile file) {

        if (!file.isEmpty()) {
            // TODO KRNA max upload
            File pluginFile = transferFile(file);
            PluginsPlugin plugin = readDescriptor(pluginFile);
            PluginsPlugin databasePlugin = pluginManagementService.getInstalledPlugin(plugin);
            if (databasePlugin != null) {
                pluginFile.delete();
                // TODO KRNA plugin exist
                return "redirect:page/plugins.pluginGridView.html?iframe=true";
            } else {
                plugin.setDeleted(false);
                // TODO KRNA enum status
                plugin.setStatus("downloaded");
                plugin.setBase(false);
                plugin.setFileName(file.getOriginalFilename());

                pluginManagementService.savePlugin(plugin);
                return "redirect:page/plugins.pluginGridView.html?iframe=true";
            }
        } else {
            // TODO KRNA error
            return "redirect:page/plugins.pluginGridView.html?iframe=true";
        }

    }

    private File transferFile(final MultipartFile file) {
        File pluginFile = null;
        try {
            pluginFile = new File(tmpPath + file.getOriginalFilename());
            file.transferTo(pluginFile);
        } catch (IllegalStateException e) {
            // TODO KRNA error
            e.printStackTrace();
        } catch (IOException e) {
            // TODO KRNA error
            e.printStackTrace();
        }
        return pluginFile;
    }

    private PluginsPlugin readDescriptor(final File file) {
        String[] pluginProperties = { "identifier", "name", "packageName", "version", "vendor", "description" };
        PluginsPlugin plugin = new PluginsPlugin();
        try {
            JarFile jarFile = new JarFile(file);

            InputStream in = jarFile.getInputStream(jarFile.getEntry(descriptor));

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();

            Document doc = db.parse(in);

            doc.getDocumentElement().normalize();

            for (String property : pluginProperties) {
                String value = null;
                Node fstNode = doc.getElementsByTagName(property).item(0);
                if (fstNode.getNodeType() == Node.ELEMENT_NODE && fstNode.getFirstChild() != null) {
                    value = ((Element) fstNode).getFirstChild().getNodeValue();
                }

                if (property.equals("identifier")) {
                    plugin.setIdentifier(value);
                } else if (property.equals("name")) {
                    plugin.setName(value);
                } else if (property.equals("packageName")) {
                    plugin.setPackageName(value);
                } else if (property.equals("version")) {
                    plugin.setVersion(value);
                } else if (property.equals("vendor")) {
                    plugin.setVendor(value);
                } else if (property.equals("description")) {
                    plugin.setDescription(value);
                }

            }
        } catch (IOException e) {
            // TODO KRNA error
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            // TODO KRNA error
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO KRNA error
            e.printStackTrace();
        }
        return plugin;
    }

    @RequestMapping(value = "remove", method = RequestMethod.GET)
    @Transactional
    public ModelAndView getRemovePageView(@RequestParam("entityId") final String entityId) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("remove");

        removePlugin(entityId, tmpPath);

        return mav;
    }

    private void removePlugin(final String entityId, final String path) {

        PluginsPlugin databasePlugin = pluginManagementService.getPlugin(entityId);

        databasePlugin.setDeleted(true);

        pluginManagementService.savePlugin(databasePlugin);

        removePluginFile(path + databasePlugin.getFileName());
    }

    private void removePluginFile(final String fileName) {
        // A File object to represent the filename
        File f = new File(fileName);

        // Make sure the file or directory exists and isn't write protected
        if (!f.exists())
            throw new IllegalArgumentException("Delete: no such file or directory: " + fileName);

        if (!f.canWrite())
            throw new IllegalArgumentException("Delete: write protected: " + fileName);

        // If it is a directory, make sure it is empty
        if (f.isDirectory()) {
            throw new IllegalArgumentException("Delete: this is a directory: " + fileName);
        }

        // Attempt to delete it
        boolean success = f.delete();

        if (!success)
            throw new IllegalArgumentException("Delete: deletion failed");

    }

    @RequestMapping(value = "enablePage", method = RequestMethod.GET)
    public ModelAndView getEnablePageView() {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("enable");
        return mav;
    }

    @RequestMapping(value = "enable", method = RequestMethod.GET)
    @Transactional
    public String handleEnable(@RequestParam("entityId") final String entityId) {
        webappPath = ((WebApplicationContext) applicationContext).getServletContext().getRealPath("/");
        LOG.debug(webappPath);
        PluginsPlugin plugin = pluginManagementService.getPlugin(entityId);
        String pluginStatus = plugin.getStatus();
        // TODO KRNA enum status
        plugin.setStatus("active");
        pluginManagementService.savePlugin(plugin);
        // TODO KRNA enum status
        if (pluginStatus.equals("installed")) {
            return "redirect:page/plugins.pluginGridView.html?iframe=true";
        } else {
            boolean success = moveFile(plugin.getFileName());
            if (!success)
                throw new IllegalArgumentException("Move: move failed");
            return "redirect:enablePage.html";
        }
    }

    private boolean moveFile(final String fileName) {
        // File (or directory) to be moved
        File file = new File(tmpPath + fileName);
        // Destination directory
        File dir = new File(webappPath + libPath);
        // Move file to new directory
        return file.renameTo(new File(dir, file.getName()));
    }

    @RequestMapping(value = "handleRestart", method = RequestMethod.POST)
    @ResponseBody
    public String handleRestart() {
        String[] commands = { "bash shutdown.sh", "bash startup.sh" };
        // TODO KRNA path LOG.debug(webappPath + binPath);
        File file = new File("/Users/krna/apache-tomcat-6.0.29/bin/");
        try {
            Runtime runtime = Runtime.getRuntime();

            Process shutdownProcess = runtime.exec(commands[0], null, file);
            // TODO KRNA waiting
            shutdownProcess.waitFor();
            LOG.debug("Shutdown exit value: " + shutdownProcess.exitValue());
            Thread.sleep(3000);
            Process startupProcess = runtime.exec(commands[1], null, file);
            LOG.debug("Startup exit value: " + startupProcess.exitValue());
        } catch (IOException e) {
            // TODO KRNA error
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO KRNA error
            e.printStackTrace();
        }
        return "ok";
    }

    @RequestMapping(value = "disable", method = RequestMethod.GET)
    @Transactional
    public String getDisablePageView(@RequestParam("entityId") final String entityId) {
        PluginsPlugin plugin = pluginManagementService.getPlugin(entityId);
        // TODO KRNA enum status
        plugin.setStatus("installed");
        pluginManagementService.savePlugin(plugin);
        return "redirect:page/plugins.pluginGridView.html?iframe=true";
    }

    @RequestMapping(value = "deinstall", method = RequestMethod.GET)
    @Transactional
    public ModelAndView getDeinstallPageView(@RequestParam("entityId") final String entityId) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("enable");
        webappPath = ((WebApplicationContext) applicationContext).getServletContext().getRealPath("/");
        PluginsPlugin databaseEntity = pluginManagementService.getPlugin(entityId);
        // TODO KRNA check sequence
        removePlugin(entityId, webappPath + libPath);

        removeResources("js", "js", databaseEntity.getIdentifier());
        removeResources("css", "css", databaseEntity.getIdentifier());
        removeResources("img", "img", databaseEntity.getIdentifier());
        removeResources("jsp", "WEB-INF/jsp", databaseEntity.getIdentifier());

        return mav;
    }

    private void removeResources(final String type, final String targetPath, final String identifier) {
        LOG.info("Removing resources " + type + " ...");

        deleteDirectory(new File(webappPath + "/" + targetPath + "/" + identifier));
    }

    private boolean deleteDirectory(final File path) {
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

    @RequestMapping(value = "update", method = RequestMethod.GET)
    public ModelAndView getUpdatePageView() {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("update");
        return mav;
    }

    @RequestMapping(value = "update", method = RequestMethod.POST)
    @Transactional
    public String handleUpdate(@RequestParam("file") final MultipartFile file) {
        webappPath = ((WebApplicationContext) applicationContext).getServletContext().getRealPath("/");
        if (!file.isEmpty()) {
            // TODO KRNA max upload
            File pluginFile = transferFile(file);
            PluginsPlugin plugin = readDescriptor(pluginFile);
            PluginsPlugin databasePlugin = pluginManagementService.getInstalledPlugin(plugin);
            if (databasePlugin.getStatus().equals("downloaded")) {
                // TODO KRNA plugin has bad status
                return "redirect:page/plugins.pluginGridView.html?iframe=true";
            }
            if (databasePlugin.getVersion().compareTo(plugin.getVersion()) >= 0) {
                pluginFile.delete();
                // TODO KRNA plugin has good version
                return "redirect:page/plugins.pluginGridView.html?iframe=true";
            } else {
                plugin.setDeleted(false);
                // TODO KRNA enum status
                plugin.setStatus(databasePlugin.getStatus());
                plugin.setBase(false);
                plugin.setFileName(file.getOriginalFilename());
                databasePlugin.setDeleted(true);
                pluginManagementService.savePlugin(plugin);
                pluginManagementService.savePlugin(databasePlugin);
                removeResources("js", "js", plugin.getIdentifier());
                removeResources("css", "css", plugin.getIdentifier());
                removeResources("img", "img", plugin.getIdentifier());
                removeResources("jsp", "WEB-INF/jsp", plugin.getIdentifier());
                boolean success = moveFile(plugin.getFileName());
                if (!success)
                    throw new IllegalArgumentException("Move: move failed");
                return "redirect:enablePage.html";
            }
        } else {
            // TODO KRNA error
            return "redirect:page/plugins.pluginGridView.html?iframe=true";
        }

    }

}
