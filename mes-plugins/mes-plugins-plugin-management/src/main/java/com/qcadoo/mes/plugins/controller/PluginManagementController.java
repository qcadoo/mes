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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.xml.sax.SAXException;

import com.qcadoo.mes.beans.plugins.PluginsPlugin;
import com.qcadoo.mes.core.api.PluginManagementService;
import com.qcadoo.mes.core.enums.PluginStatus;
import com.qcadoo.mes.plugins.exception.PluginException;
import com.qcadoo.mes.plugins.util.PluginUtil;

@Controller
public final class PluginManagementController {

    private static final Logger LOG = LoggerFactory.getLogger(PluginManagementController.class);

    private static final String libPath = "WEB-INF/lib/";

    private static final String tmpPath = "WEB-INF/tmp/";

    private static final String pluginView = "redirect:page/plugins/pluginGridView.html?iframe=true";

    @Autowired
    private PluginManagementService pluginManagementService;

    @Autowired
    private ApplicationContext applicationContext;

    private String webappPath;

    @PostConstruct
    public void init() {
        // TOMCAT
        webappPath = ((WebApplicationContext) applicationContext).getServletContext().getRealPath("/");
        // JETTY webappPath = webappPath + "/";
        LOG.debug(webappPath);
    }

    @RequestMapping(value = "download", method = RequestMethod.GET)
    public ModelAndView getDownloadPageView() {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("download");
        return mav;
    }

    @RequestMapping(value = "downloadError", method = RequestMethod.GET)
    public ModelAndView getDownloadErrorPageView() {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("downloadError");
        return mav;
    }

    @RequestMapping(value = "download", method = RequestMethod.POST)
    public String handleDownload(@RequestParam("file") final MultipartFile file) {
        if (!file.isEmpty()) {
            File pluginFile = null;
            boolean deleteFile = false;
            try {
                pluginFile = PluginUtil.transferFileToTmp(file, webappPath + tmpPath);
                PluginsPlugin plugin = PluginUtil.readDescriptor(pluginFile);
                PluginsPlugin pluginWithIdentifier = pluginManagementService.getPluginByIdentifier(plugin.getIdentifier());
                if (pluginWithIdentifier != null) {
                    deleteFile = true;
                    LOG.error("Plugin with identifier existed");
                    return pluginView + "&message=Istnieje plugin z podanym identyfikatorem";
                }
                PluginsPlugin databasePlugin = pluginManagementService.getPluginByNameAndVendor(plugin.getName(),
                        plugin.getVendor());
                if (databasePlugin != null) {
                    deleteFile = true;
                    LOG.error("Plugin was installed");
                    return pluginView + "&message=Plugin jest zainstalowany";
                } else {
                    plugin.setDeleted(false);
                    plugin.setStatus(PluginStatus.DOWNLOADED.getValue());
                    plugin.setBase(false);
                    plugin.setFileName(file.getOriginalFilename());

                    pluginManagementService.savePlugin(plugin);
                    return pluginView;
                }
            } catch (IllegalStateException e) {
                deleteFile = true;
                LOG.error("Problem with installing file");
                return pluginView + "&message=Blad instalacji pliku";
            } catch (IOException e) {
                deleteFile = true;
                LOG.error("Problem with installing file");
                return pluginView + "&message=Blad instalacji pliku";
            } catch (ParserConfigurationException e) {
                deleteFile = true;
                LOG.error("Problem with parsing descriptor");
                return pluginView + "&message=Blad odczytu deskryptora";
            } catch (SAXException e) {
                deleteFile = true;
                LOG.error("Problem with parsing descriptor");
                return pluginView + "&message=Blad odczytu deskryptora";
            } finally {
                if (deleteFile && pluginFile != null && pluginFile.exists()) {
                    pluginFile.delete();
                    LOG.debug("Plugin file delete");
                }
            }
        } else {
            LOG.error("Chosen file is empty");
            return pluginView + "&message=Plik jest pusty";
        }

    }

    @RequestMapping(value = "removePage", method = RequestMethod.GET)
    public ModelAndView getRemovePageView() {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("remove");
        return mav;
    }

    @RequestMapping(value = "remove", method = RequestMethod.GET)
    public String getRemovePageView(@RequestParam("entityId") final String entityId) {
        PluginsPlugin databasePlugin = pluginManagementService.getPluginById(entityId);
        if (databasePlugin.isBase()) {
            LOG.error("Plugin is base");
            return pluginView + "&message=Plugin jest bazowy";
        }
        try {
            removePlugin(databasePlugin, webappPath + tmpPath);
        } catch (PluginException e) {
            LOG.error("Problem with removing plugin file");
            return pluginView + "&message=Blad usuwania pliku";
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
    public String handleEnable(@RequestParam("entityId") final String entityId) {
        PluginsPlugin plugin = pluginManagementService.getPluginById(entityId);
        if (plugin.isBase()) {
            LOG.error("Plugin is base");
            return pluginView + "&message=Plugin jest bazowy";
        }
        String pluginStatus = plugin.getStatus();
        plugin.setStatus(PluginStatus.ACTIVE.getValue());
        pluginManagementService.savePlugin(plugin);
        if (pluginStatus.equals(PluginStatus.INSTALLED.getValue())) {
            return pluginView;
        } else {
            try {
                PluginUtil.movePluginFile(webappPath + tmpPath + plugin.getFileName(), webappPath + libPath);
            } catch (PluginException e) {
                plugin.setStatus(PluginStatus.DOWNLOADED.getValue());
                pluginManagementService.savePlugin(plugin);
                LOG.error("Problem with moving plugin file");
                return pluginView + "&message=Blad przenoszenia pliku";
            }
            return "redirect:enablePage.html";
        }
    }

    @RequestMapping(value = "handleRestart", method = RequestMethod.POST)
    @ResponseBody
    public String handleRestart() {
        try {
            PluginUtil.restartServer(webappPath);

        } catch (PluginException e) {
            LOG.error("Problem with restart server");
            return pluginView + "&message=Blad restartu serwera";
        }

        return "ok";
    }

    @RequestMapping(value = "disable", method = RequestMethod.GET)
    public String getDisablePageView(@RequestParam("entityId") final String entityId) {
        PluginsPlugin plugin = pluginManagementService.getPluginById(entityId);
        if (plugin.isBase()) {
            LOG.error("Plugin is base");
            return pluginView + "&message=Plugin jest bazowy";
        }
        plugin.setStatus(PluginStatus.INSTALLED.getValue());
        pluginManagementService.savePlugin(plugin);
        return pluginView;
    }

    @RequestMapping(value = "deinstallPage", method = RequestMethod.GET)
    public ModelAndView getDeinstallPageView() {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("enable");
        return mav;
    }

    @RequestMapping(value = "deinstall", method = RequestMethod.GET)
    public String handleDeinstall(@RequestParam("entityId") final String entityId) {
        PluginsPlugin databasePlugin = pluginManagementService.getPluginById(entityId);
        if (databasePlugin.isBase()) {
            LOG.error("Plugin is base");
            return pluginView + "&message=Plugin jest bazowy";
        }
        try {
            removePlugin(databasePlugin, webappPath + libPath);
        } catch (PluginException e) {
            LOG.error("Problem with removing plugin file");
            return pluginView + "&message=Blad usuwania pliku";
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
    public String handleUpdate(@RequestParam("file") final MultipartFile file) {
        if (!file.isEmpty()) {
            File pluginFile = null;
            boolean deleteFile = false;
            try {
                pluginFile = PluginUtil.transferFileToTmp(file, webappPath + tmpPath);
                PluginsPlugin plugin = PluginUtil.readDescriptor(pluginFile);
                PluginsPlugin databasePlugin = pluginManagementService.getPluginByNameAndVendor(plugin.getName(),
                        plugin.getVendor());
                if (databasePlugin == null) {
                    deleteFile = true;
                    LOG.error("Plugin not found in database");
                    return pluginView + "&message=Brak pluginu";
                } else if (databasePlugin.isBase()) {
                    deleteFile = true;
                    LOG.error("Plugin is base");
                    return pluginView + "&message=Plugin jest bazowy";
                } else if (databasePlugin.getStatus().equals(PluginStatus.DOWNLOADED.getValue())) {
                    deleteFile = true;
                    LOG.error("Plugin hasn't apropriate status");
                    return pluginView + "&message=Niepoprawny status do aktualizacji";
                } else if (databasePlugin.getVersion().compareTo(plugin.getVersion()) >= 0) {
                    deleteFile = true;
                    LOG.error("Plugin has actual version");
                    return pluginView + "&message=Plugin jest aktualny";
                } else {
                    plugin.setFileName(file.getOriginalFilename());
                    movePlugin(plugin, databasePlugin);
                    return "redirect:enablePage.html";
                }
            } catch (IllegalStateException e) {
                deleteFile = true;
                LOG.error("Problem with installing file");
                return pluginView + "&message=Blad instalacji pliku";
            } catch (IOException e) {
                deleteFile = true;
                LOG.error("Problem with installing file");
                return pluginView + "&message=Blad instalacji pliku";
            } catch (ParserConfigurationException e) {
                deleteFile = true;
                LOG.error("Problem with parsing descriptor");
                return pluginView + "&message=Blad odczytu deskryptora";
            } catch (SAXException e) {
                deleteFile = true;
                LOG.error("Problem with parsing descriptor");
                return pluginView + "&message=Blad odczytu deskryptora";
            } catch (PluginException e) {
                deleteFile = true;
                LOG.error("Problem with moving/removing plugin file");
                return pluginView + "&message=Blad usuwania/przenoszenia pliku";
            } finally {
                if (deleteFile && pluginFile != null && pluginFile.exists()) {
                    pluginFile.delete();
                    LOG.debug("Plugin file delete");
                }
            }
        } else {
            LOG.error("Chosen file is empty");
            return pluginView + "&message=Plik jest pusty";
        }

    }

    private void removePlugin(final PluginsPlugin databasePlugin, final String path) throws PluginException {

        databasePlugin.setDeleted(true);

        pluginManagementService.savePlugin(databasePlugin);

        PluginUtil.removePluginFile(path + databasePlugin.getFileName());
    }

    private void movePlugin(final PluginsPlugin plugin, final PluginsPlugin databasePlugin) throws PluginException {
        plugin.setDeleted(false);
        plugin.setStatus(databasePlugin.getStatus());
        plugin.setBase(false);
        databasePlugin.setDeleted(true);
        pluginManagementService.savePlugin(databasePlugin);

        pluginManagementService.savePlugin(plugin);

        PluginUtil.removePluginFile(webappPath + libPath + databasePlugin.getFileName());

        PluginUtil.movePluginFile(webappPath + tmpPath + plugin.getFileName(), webappPath + libPath);

        PluginUtil.removeResources("js", webappPath + "/" + "js" + "/" + databasePlugin.getIdentifier());
        PluginUtil.removeResources("css", webappPath + "/" + "css" + "/" + databasePlugin.getIdentifier());
        PluginUtil.removeResources("img", webappPath + "/" + "img" + "/" + databasePlugin.getIdentifier());
        PluginUtil.removeResources("jsp", webappPath + "/" + "WEB-INF/jsp" + "/" + databasePlugin.getIdentifier());
    }

}
