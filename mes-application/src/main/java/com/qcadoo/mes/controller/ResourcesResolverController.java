package com.qcadoo.mes.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.qcadoo.mes.view.internal.module.resourceModule.ResourceService;

@Controller
public class ResourcesResolverController {

    @Autowired
    private ResourceService resourceService;

    @RequestMapping(value = { "js/{pluginIdentifier}/**", "css/{pluginIdentifier}/**", "img/{pluginIdentifier}/**" }, method = RequestMethod.GET)
    public void gerResource(@PathVariable("pluginIdentifier") final String pluginIdentifier, HttpServletRequest request,
            HttpServletResponse response) {

        resourceService.serveResource(request, response);
    }

}
