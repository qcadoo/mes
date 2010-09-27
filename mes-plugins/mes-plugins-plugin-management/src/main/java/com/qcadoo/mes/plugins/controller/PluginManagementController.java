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
public class PluginManagementController {

    private static final Logger LOG = LoggerFactory.getLogger(PluginManagementController.class);

    private static final String libPath = "WEB-INF/lib/";

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

    // TODO KRNA finally delete i transakcje
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
    @Transactional
    public String handleDownload(@RequestParam("file") final MultipartFile file) {
        if (!file.isEmpty()) {
            try {
                File pluginFile = PluginUtil.transferFileToTmp(file, webappPath + tmpPath);
                PluginsPlugin plugin = PluginUtil.readDescriptor(pluginFile);
                PluginsPlugin databasePlugin = pluginManagementService.getPluginByNameAndVendor(plugin.getName(),
                        plugin.getVendor());
                if (databasePlugin != null) {
                    pluginFile.delete();
                    LOG.error("Plugin was installed");
                    return "redirect:page/plugins.pluginGridView.html?iframe=true&message=Plugin jest zainstalowany";
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
                return "redirect:page/plugins.pluginGridView.html?iframe=true&message=Blad instalacji pliku";
            } catch (IOException e) {
                LOG.error("Problem with installing file");
                return "redirect:page/plugins.pluginGridView.html?iframe=true&message=Blad instalacji pliku";
            } catch (ParserConfigurationException e) {
                LOG.error("Problem with parsing descriptor");
                return "redirect:page/plugins.pluginGridView.html?iframe=true&message=Blad odczytu deskryptora";
            } catch (SAXException e) {
                LOG.error("Problem with parsing descriptor");
                return "redirect:page/plugins.pluginGridView.html?iframe=true&message=Blad odczytu deskryptora";
            }
        } else {
            LOG.error("Chosen file is empty");
            return "redirect:page/plugins.pluginGridView.html?iframe=true&message=Plik jest pusty";
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
            return "redirect:page/plugins.pluginGridView.html?iframe=true&message=Plugin jest bazowy";
        }
        try {
            removePlugin(entityId, webappPath + tmpPath);
        } catch (PluginException e) {
            LOG.error("Problem with removing plugin file");
            return "redirect:page/plugins.pluginGridView.html?iframe=true&message=Blad usuwania pliku";
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
            return "redirect:page/plugins.pluginGridView.html?iframe=true&message=Plugin jest bazowy";
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
                return "redirect:page/plugins.pluginGridView.html?iframe=true&message=Blad przenoszenia pliku";
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
            return "redirect:page/plugins.pluginGridView.html?iframe=true&message=Blad restartu serwera";
        }
        return "ok";
    }

    @RequestMapping(value = "disable", method = RequestMethod.GET)
    @Transactional
    public String getDisablePageView(@RequestParam("entityId") final String entityId) {
        PluginsPlugin plugin = pluginManagementService.getPluginById(entityId);
        if (plugin.isBase()) {
            LOG.error("Plugin is base");
            return "redirect:page/plugins.pluginGridView.html?iframe=true&message=Plugin jest bazowy";
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
            return "redirect:page/plugins.pluginGridView.html?iframe=true&message=Plugin jest bazowy";
        }
        try {
            removePlugin(entityId, webappPath + libPath);
        } catch (PluginException e) {
            LOG.error("Problem with removing plugin file");
            return "redirect:page/plugins.pluginGridView.html?iframe=true&message=Blad usuwania pliku";
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
                File pluginFile = PluginUtil.transferFileToTmp(file, webappPath + tmpPath);
                PluginsPlugin plugin = PluginUtil.readDescriptor(pluginFile);
                PluginsPlugin databasePlugin = pluginManagementService.getPluginByNameAndVendor(plugin.getName(),
                        plugin.getVendor());
                if (databasePlugin == null) {
                    pluginFile.delete();
                    LOG.error("Plugin not found in database");
                    return "redirect:page/plugins.pluginGridView.html?iframe=true&message=Brak pluginu";
                }
                if (databasePlugin.isBase()) {
                    pluginFile.delete();
                    LOG.error("Plugin is base");
                    return "redirect:page/plugins.pluginGridView.html?iframe=true&message=Plugin jest bazowy";
                }
                if (databasePlugin.getStatus().equals(PluginStatus.DOWNLOADED.getValue())) {
                    pluginFile.delete();
                    LOG.info("Plugin hasn't apropriate status");
                    return "redirect:page/plugins.pluginGridView.html?iframe=true&message=Niepoprawny status do aktualizacji";
                }
                if (databasePlugin.getVersion().compareTo(plugin.getVersion()) >= 0) {
                    pluginFile.delete();
                    LOG.info("Plugin has actual version");
                    return "redirect:page/plugins.pluginGridView.html?iframe=true&message=Plugin jest aktualny";
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
                return "redirect:page/plugins.pluginGridView.html?iframe=true&message=Blad instalacji pliku";
            } catch (IOException e) {
                LOG.error("Problem with installing file");
                return "redirect:page/plugins.pluginGridView.html?iframe=true&message=Blad instalacji pliku";
            } catch (ParserConfigurationException e) {
                LOG.error("Problem with parsing descriptor");
                return "redirect:page/plugins.pluginGridView.html?iframe=true&message=Blad odczytu deskryptora";
            } catch (SAXException e) {
                LOG.error("Problem with parsing descriptor");
                return "redirect:page/plugins.pluginGridView.html?iframe=true&message=Blad odczytu deskryptora";
            } catch (PluginException e) {
                LOG.error("Problem with moving/removing plugin file");
                return "redirect:page/plugins.pluginGridView.html?iframe=true&message=Blad usuwania/przenoszenia pliku";
            }
        } else {
            LOG.error("Chosen file is empty");
            return "redirect:page/plugins.pluginGridView.html?iframe=true&message=Plik jest pusty";
        }

    }

    private void removePlugin(final String entityId, final String path) throws PluginException {

        PluginsPlugin databasePlugin = pluginManagementService.getPluginById(entityId);

        databasePlugin.setDeleted(true);

        pluginManagementService.savePlugin(databasePlugin);

        PluginUtil.removePluginFile(path + databasePlugin.getFileName());
    }

}
