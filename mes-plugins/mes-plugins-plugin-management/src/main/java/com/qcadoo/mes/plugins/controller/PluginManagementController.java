package com.qcadoo.mes.plugins.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
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
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.qcadoo.mes.core.data.api.PluginManagementService;
import com.qcadoo.mes.core.data.beans.Plugin;

@Controller
public class PluginManagementController {

    private static final Logger LOG = LoggerFactory.getLogger(PluginManagementController.class);

    @Autowired
    private PluginManagementService pluginManagementService;

    @Autowired
    private ApplicationContext applicationContext;

    String webappPath;

    private static final String[] pluginProperties = { "identifier", "name", "packageName", "version", "vendor", "description" };

    private static final String path = "/Users/krna/apache-tomcat-6.0.29/webapps/mes-application-0.1-SNAPSHOT/WEB-INF/lib/";

    private static final String binPath = "/Users/krna/apache-tomcat-6.0.29/bin/";

    @RequestMapping(value = "install", method = RequestMethod.GET)
    public ModelAndView getInstallPageView() {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("install");

        webappPath = ((WebApplicationContext) applicationContext).getServletContext().getRealPath("/");
        System.out.println(webappPath);
        return mav;
    }

    @RequestMapping(value = "upload", method = RequestMethod.GET)
    public ModelAndView getUploadPageView() {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("upload");

        return mav;
    }

    @RequestMapping(value = "restart", method = RequestMethod.GET)
    public ModelAndView getRestartPageView() {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("restart");
        return mav;
    }

    @RequestMapping(value = "deinstall", method = RequestMethod.GET)
    @Transactional
    public ModelAndView getDeinstallPageView(@RequestParam("entityId") final String entityId) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("restart");
        Plugin databaseEntity = pluginManagementService.getPlugin(entityId);

        removeResources("js", "js", databaseEntity.getIdentifier());
        removeResources("css", "css", databaseEntity.getIdentifier());
        removeResources("img", "img", databaseEntity.getIdentifier());
        removeResources("jsp", "WEB-INF/jsp", databaseEntity.getIdentifier());

        removePlugin(entityId);

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

    @RequestMapping(value = "handleRestart", method = RequestMethod.POST)
    @Transactional
    public void handleRestart() {
        String[] commands = { "bash shutdown.sh", "bash startup.sh" };

        File file = new File(binPath);
        try {
            Runtime runtime = Runtime.getRuntime();
            List<Plugin> pluginList = pluginManagementService.getPluginsWithStatus("downloaded");
            for (Plugin plugin : pluginList) {
                plugin.setStatus("installed");
                pluginManagementService.savePlugin(plugin);
            }

            Process shutdownProcess = runtime.exec(commands[0], null, file);
            shutdownProcess.waitFor();
            System.out.println("Shutdown exit value: " + shutdownProcess.exitValue());
            Thread.sleep(3000);
            Process startupProcess = runtime.exec(commands[1], null, file);
            System.out.println("Startup exit value: " + startupProcess.exitValue());

        } catch (IOException e) {
            // TODO KRNA error
        } catch (InterruptedException e) {
            // TODO KRNA error

        }
    }

    @RequestMapping(value = "remove", method = RequestMethod.GET)
    @Transactional
    public ModelAndView getRemovePageView(@RequestParam("entityId") final String entityId) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("remove");

        removePlugin(entityId);

        return mav;
    }

    private void removePlugin(final String entityId) {
        Plugin databasePlugin = pluginManagementService.getPlugin(entityId);

        databasePlugin.setDeleted(true);

        pluginManagementService.savePlugin(databasePlugin);

        removePluginFile(databasePlugin.getFileName());
    }

    private void removePluginFile(final String fileName) {
        // A File object to represent the filename
        File f = new File(path + fileName);

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

    @RequestMapping(value = "upload", method = RequestMethod.POST)
    @Transactional
    public String handleUpload(@RequestParam("file") final MultipartFile file) {

        if (!file.isEmpty()) {
            try {
                File pluginFile = new File(path + file.getOriginalFilename());
                file.transferTo(pluginFile);
                JarFile jarFile = new JarFile(pluginFile);
                InputStream in = jarFile.getInputStream(jarFile.getEntry("plugin.xml"));

                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(in);
                doc.getDocumentElement().normalize();
                Plugin plugin = new Plugin();
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

                Plugin databasePlugin = pluginManagementService.getInstalledPlugin(plugin);
                if (databasePlugin != null) {
                    pluginFile.delete();
                    return "redirect:install.html?error=2";
                } else {
                    plugin.setDeleted(false);
                    plugin.setStatus("downloaded");
                    plugin.setBase(false);
                    plugin.setFileName(file.getOriginalFilename());

                    pluginManagementService.savePlugin(plugin);
                }

            } catch (IOException e) {
                return "redirect:install.html?error=1";
            } catch (ParserConfigurationException e) {
                return "redirect:install.html?error=1";
            } catch (SAXException e) {
                return "redirect:install.html?error=1";
            } catch (MaxUploadSizeExceededException e) {
                return "redirect:install.html?error=3";
            }

            return "redirect:page/plugins.pluginGridView.html?iframe=true";
        } else {
            return "redirect:install.html?error=1";
        }

    }
}
