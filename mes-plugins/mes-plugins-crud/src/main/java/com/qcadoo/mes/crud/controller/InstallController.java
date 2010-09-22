package com.qcadoo.mes.crud.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
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

import com.qcadoo.mes.core.data.api.DataDefinitionService;
import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.internal.DataAccessServiceImpl;
import com.qcadoo.mes.core.data.internal.EntityService;

@Controller
public class InstallController {

    private static final Logger LOG = LoggerFactory.getLogger(DataAccessServiceImpl.class);

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private EntityService entityService;

    @Autowired
    private ApplicationContext applicationContext;

    String webappPath;

    private static final String[] pluginProperties = { "identifier", "name", "group", "version", "vendor", "description" };

    private static final String path = "/Users/krna/apache-tomcat-6.0.29/webapps/mes-application-0.1-SNAPSHOT/WEB-INF/lib/";

    private static final String binPath = "/Users/krna/apache-tomcat-6.0.29/bin/";

    @RequestMapping(value = "install", method = RequestMethod.GET)
    public ModelAndView getInstallPageView() {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("install");

        webappPath = ((WebApplicationContext) applicationContext).getServletContext().getRealPath("/");

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
    public ModelAndView getDeinstallPageView(@RequestParam("entityId") final String entityId,
            @RequestParam("codeId") final String codeId) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("restart");

        removeResources("js", "js", codeId);
        removeResources("css", "css", codeId);
        removeResources("img", "img", codeId);
        removeResources("jsp", "WEB-INF/jsp", codeId);

        removePlugin(entityId);

        return mav;
    }

    private void removeResources(final String type, final String targetPath, final String codeId) {
        LOG.info("Removing resources " + type + " ...");

        deleteDirectory(new File(webappPath + "/" + targetPath + "/" + codeId));
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
    public void handleRestart() {
        String[] commands = { "bash shutdown.sh", "bash startup.sh" };

        File file = new File(binPath);
        try {
            Runtime runtime = Runtime.getRuntime();

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
        DataDefinition dataDefinition = dataDefinitionService.get("plugins.plugin");

        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(dataDefinition.getClassForEntity())
                .add(Restrictions.idEq(Long.valueOf(entityId)));
        if (dataDefinition.isDeletable()) {
            entityService.addDeletedRestriction(criteria);
        }

        Object databaseEntity = criteria.uniqueResult();

        entityService.setDeleted(databaseEntity);

        sessionFactory.getCurrentSession().update(databaseEntity);

        String fileName = (String) entityService.getField(databaseEntity, dataDefinition.getField("fileName"));
        removePluginFile(fileName);
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
                Entity genericEntity = new Entity();
                for (String property : pluginProperties) {
                    String value = null;
                    Node fstNode = doc.getElementsByTagName(property).item(0);
                    if (fstNode.getNodeType() == Node.ELEMENT_NODE && fstNode.getFirstChild() != null) {
                        value = ((Element) fstNode).getFirstChild().getNodeValue();
                    }
                    if (property.equals("vendor")) {
                        genericEntity.setField("publisher", value);
                    } else if (property.equals("group")) {
                        genericEntity.setField("packageName", value);
                    } else if (property.equals("identifier")) {
                        genericEntity.setField("codeId", value);
                    } else {
                        genericEntity.setField(property, value);
                    }

                }
                genericEntity.setField("deleted", false);
                DataDefinition dataDefinition = dataDefinitionService.get("plugins.plugin");
                Criteria criteria = sessionFactory.getCurrentSession().createCriteria(dataDefinition.getClassForEntity())
                        .add(Restrictions.eq("name", genericEntity.getField("name")))
                        .add(Restrictions.eq("publisher", genericEntity.getField("publisher")))
                        .add(Restrictions.ge("version", genericEntity.getField("version")));

                if (dataDefinition.isDeletable()) {
                    entityService.addDeletedRestriction(criteria);
                }

                Object databaseEntity = criteria.uniqueResult();
                if (databaseEntity != null) {
                    pluginFile.delete();
                    return "redirect:install.html?error=2";
                } else {

                    genericEntity.setField("active", false);
                    genericEntity.setField("base", false);
                    genericEntity.setField("fileName", file.getOriginalFilename());

                    databaseEntity = entityService.convertToDatabaseEntity(dataDefinition, genericEntity, null);

                    sessionFactory.getCurrentSession().save(databaseEntity);
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

            return "redirect:install.html";
        } else {
            return "redirect:install.html?error=1";
        }

    }
}
