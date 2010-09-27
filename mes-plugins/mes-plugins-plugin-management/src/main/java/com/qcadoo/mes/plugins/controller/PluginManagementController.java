package com.qcadoo.mes.plugins.controller;

import java.io.File;
import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.xml.sax.SAXException;

import com.qcadoo.mes.beans.plugins.PluginsPlugin;
import com.qcadoo.mes.core.api.PluginManagementService;
import com.qcadoo.mes.core.enums.PluginStatus;
import com.qcadoo.mes.plugins.exception.PluginException;
import com.qcadoo.mes.plugins.util.PluginUtil;

@Controller
public class PluginManagementController {

    private static final Logger LOG = LoggerFactory.getLogger(PluginManagementController.class);

    private static final String libPath = "WEB-INF/lib/";

    private static final String binPath = "../bin/";

    private static final String tmpPath = "WEB-INF/tmp/";

    @Autowired
    private PluginManagementService pluginManagementService;

    @Autowired
    private ApplicationContext applicationContext;

    private String webappPath;

    @PostConstruct
    public void init() {
        webappPath = ((WebApplicationContext) applicationContext).getServletContext().getRealPath("/");
        webappPath = webappPath + "/";
        LOG.debug(webappPath);
    }

    // TODO KRNA show errors
    @RequestMapping(value = "download", method = RequestMethod.GET)
    public ModelAndView getDownloadPageView() {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("download");
        return mav;
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ModelAndView handleMaxUploadSizeExceededException(final MaxUploadSizeExceededException ex) {
        return new ModelAndView("downloadError");
    }

    @RequestMapping(value = "download", method = RequestMethod.POST)
    @Transactional
    public String handleDownload(@RequestParam("file") final MultipartFile file) {
        if (!file.isEmpty()) {
            try {
                // TODO KRNA max upload
                File pluginFile = PluginUtil.transferFileToTmp(file, webappPath + tmpPath);
                PluginsPlugin plugin = PluginUtil.readDescriptor(pluginFile);
                PluginsPlugin databasePlugin = pluginManagementService.getPluginByNameAndVendor(plugin.getName(),
                        plugin.getVendor());
                if (databasePlugin != null) {
                    pluginFile.delete();
                    LOG.error("Plugin was installed");
                    return "redirect:page/plugins.pluginGridView.html?iframe=true";
                } else {
                    plugin.setDeleted(false);
                    plugin.setStatus(PluginStatus.DOWNLOADED.getValue());
                    plugin.setBase(false);
                    plugin.setFileName(file.getOriginalFilename());

                    pluginManagementService.savePlugin(plugin);
                    return "redirect:page/plugins.pluginGridView.html?iframe=true";
                }
            } catch (IllegalStateException e) {
                LOG.error("Problem with installing file");
                return "redirect:page/plugins.pluginGridView.html?iframe=true";
            } catch (IOException e) {
                LOG.error("Problem with installing file");
                return "redirect:page/plugins.pluginGridView.html?iframe=true";
            } catch (ParserConfigurationException e) {
                LOG.error("Problem with parsing descriptor");
                return "redirect:page/plugins.pluginGridView.html?iframe=true&message='Problem z parsowaniem deskryptora'";
            } catch (SAXException e) {
                LOG.error("Problem with parsing descriptor");
                return "redirect:page/plugins.pluginGridView.html?iframe=true";
            }
        } else {
            LOG.error("Chosen file is empty");
            return "redirect:page/plugins.pluginGridView.html?iframe=true";
        }

    }

    @RequestMapping(value = "removePage", method = RequestMethod.GET)
    public ModelAndView getRemovePageView() {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("remove");
        return mav;
    }

    @RequestMapping(value = "remove", method = RequestMethod.GET)
    @Transactional
    public String getRemovePageView(@RequestParam("entityId") final String entityId) {
        PluginsPlugin databasePlugin = pluginManagementService.getPluginById(entityId);
        if (databasePlugin.isBase()) {
            LOG.error("Plugin is base");
            return "redirect:page/plugins.pluginGridView.html?iframe=true";
        }
        try {
            removePlugin(entityId, webappPath + tmpPath);
        } catch (PluginException e) {
            LOG.error("Problem with removing plugin file");
            return "redirect:page/plugins.pluginGridView.html?iframe=true";
        }

        return "redirect:removePage.html";
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

        PluginsPlugin plugin = pluginManagementService.getPluginById(entityId);
        if (plugin.isBase()) {
            LOG.error("Plugin is base");
            return "redirect:page/plugins.pluginGridView.html?iframe=true";
        }
        String pluginStatus = plugin.getStatus();
        plugin.setStatus(PluginStatus.ACTIVE.getValue());
        pluginManagementService.savePlugin(plugin);
        if (pluginStatus.equals(PluginStatus.INSTALLED.getValue())) {
            return "redirect:page/plugins.pluginGridView.html?iframe=true";
        } else {
            try {
                PluginUtil.moveFile(webappPath + tmpPath + plugin.getFileName(), webappPath + libPath);
            } catch (PluginException e) {
                LOG.error("Problem with moving plugin file");
                return "redirect:page/plugins.pluginGridView.html?iframe=true";
            }
            return "redirect:enablePage.html";
        }
    }

    @RequestMapping(value = "handleRestart", method = RequestMethod.POST)
    @ResponseBody
    public String handleRestart() {
        String[] commandsStop = { "bash cd " + webappPath, "bash cd " + binPath, "bash shutdown.sh" };
        String[] commandsStart = { "bash cd " + webappPath, "bash cd " + binPath, "bash startup.sh" };
        try {
            Runtime runtime = Runtime.getRuntime();

            Process shutdownProcess = runtime.exec(commandsStop);
            // TODO KRNA waiting
            shutdownProcess.waitFor();
            LOG.debug("Shutdown exit value: " + shutdownProcess.exitValue());
            Thread.sleep(3000);
            Process startupProcess = runtime.exec(commandsStart);
            LOG.debug("Startup exit value: " + startupProcess.exitValue());
        } catch (IOException e) {
            // TODO KRNA error
            LOG.error("Problem with restart server");
        } catch (InterruptedException e) {
            // TODO KRNA error
            LOG.error("Problem with restart server");
        }
        return "ok";
    }

    @RequestMapping(value = "disable", method = RequestMethod.GET)
    @Transactional
    public String getDisablePageView(@RequestParam("entityId") final String entityId) {
        PluginsPlugin plugin = pluginManagementService.getPluginById(entityId);
        if (plugin.isBase()) {
            LOG.error("Plugin is base");
            return "redirect:page/plugins.pluginGridView.html?iframe=true";
        }
        plugin.setStatus(PluginStatus.INSTALLED.getValue());
        pluginManagementService.savePlugin(plugin);
        return "redirect:page/plugins.pluginGridView.html?iframe=true";
    }

    @RequestMapping(value = "deinstallPage", method = RequestMethod.GET)
    public ModelAndView getDeinstallPageView() {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("enable");
        return mav;
    }

    @RequestMapping(value = "deinstall", method = RequestMethod.GET)
    @Transactional
    public String handleDeinstall(@RequestParam("entityId") final String entityId) {
        PluginsPlugin databasePlugin = pluginManagementService.getPluginById(entityId);
        if (databasePlugin.isBase()) {
            LOG.error("Plugin is base");
            return "redirect:page/plugins.pluginGridView.html?iframe=true";
        }
        try {
            removePlugin(entityId, webappPath + libPath);
        } catch (PluginException e) {
            LOG.error("Problem with removing plugin file");
            return "redirect:page/plugins.pluginGridView.html?iframe=true";
        }

        PluginUtil.removeResources("js", webappPath + "/" + "js" + "/" + databasePlugin.getIdentifier());
        PluginUtil.removeResources("css", webappPath + "/" + "css" + "/" + databasePlugin.getIdentifier());
        PluginUtil.removeResources("img", webappPath + "/" + "img" + "/" + databasePlugin.getIdentifier());
        PluginUtil.removeResources("jsp", webappPath + "/" + "WEB-INF/jsp" + "/" + databasePlugin.getIdentifier());

        return "redirect:deinstallPage.html";
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
        if (!file.isEmpty()) {
            try {
                // TODO KRNA max upload
                File pluginFile = PluginUtil.transferFileToTmp(file, webappPath + tmpPath);
                PluginsPlugin plugin = PluginUtil.readDescriptor(pluginFile);
                PluginsPlugin databasePlugin = pluginManagementService.getPluginByNameAndVendor(plugin.getName(),
                        plugin.getVendor());
                if (databasePlugin == null) {
                    pluginFile.delete();
                    LOG.error("Plugin not found in database");
                    return "redirect:page/plugins.pluginGridView.html?iframe=true";
                }
                if (databasePlugin.isBase()) {
                    pluginFile.delete();
                    LOG.error("Plugin is base");
                    return "redirect:page/plugins.pluginGridView.html?iframe=true";
                }
                if (databasePlugin.getStatus().equals(PluginStatus.DOWNLOADED.getValue())) {
                    pluginFile.delete();
                    LOG.info("Plugin hasn't apropriate status");
                    return "redirect:page/plugins.pluginGridView.html?iframe=true";
                }
                if (databasePlugin.getVersion().compareTo(plugin.getVersion()) >= 0) {
                    pluginFile.delete();
                    LOG.info("Plugin has actual version");
                    return "redirect:page/plugins.pluginGridView.html?iframe=true";
                } else {
                    plugin.setDeleted(false);
                    plugin.setStatus(databasePlugin.getStatus());
                    plugin.setBase(false);
                    plugin.setFileName(file.getOriginalFilename());
                    databasePlugin.setDeleted(true);
                    pluginManagementService.savePlugin(plugin);
                    pluginManagementService.savePlugin(databasePlugin);
                    PluginUtil.removeResources("js", webappPath + "/" + "js" + "/" + databasePlugin.getIdentifier());
                    PluginUtil.removeResources("css", webappPath + "/" + "css" + "/" + databasePlugin.getIdentifier());
                    PluginUtil.removeResources("img", webappPath + "/" + "img" + "/" + databasePlugin.getIdentifier());
                    PluginUtil.removeResources("jsp", webappPath + "/" + "WEB-INF/jsp" + "/" + databasePlugin.getIdentifier());
                    PluginUtil.removePluginFile(webappPath + tmpPath + databasePlugin.getFileName());
                    PluginUtil.moveFile(webappPath + tmpPath + plugin.getFileName(), webappPath + libPath);
                    return "redirect:enablePage.html";
                }
            } catch (IllegalStateException e) {
                LOG.error("Problem with installing file");
                return "redirect:page/plugins.pluginGridView.html?iframe=true";
            } catch (IOException e) {
                LOG.error("Problem with installing file");
                return "redirect:page/plugins.pluginGridView.html?iframe=true";
            } catch (ParserConfigurationException e) {
                LOG.error("Problem with parsing descriptor");
                return "redirect:page/plugins.pluginGridView.html?iframe=true";
            } catch (SAXException e) {
                LOG.error("Problem with parsing descriptor");
                return "redirect:page/plugins.pluginGridView.html?iframe=true";
            } catch (PluginException e) {
                LOG.error("Problem with moving/removing plugin file");
                return "redirect:page/plugins.pluginGridView.html?iframe=true";
            }
        } else {
            LOG.error("Chosen file is empty");
            return "redirect:page/plugins.pluginGridView.html?iframe=true";
        }

    }

    private void removePlugin(final String entityId, final String path) throws PluginException {

        PluginsPlugin databasePlugin = pluginManagementService.getPluginById(entityId);

        databasePlugin.setDeleted(true);

        pluginManagementService.savePlugin(databasePlugin);

        PluginUtil.removePluginFile(path + databasePlugin.getFileName());
    }

}
