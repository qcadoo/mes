package com.qcadoo.mes.materialFlowResources.controllers;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.qcadoo.mes.basic.controllers.dataProvider.dto.ColumnDTO;
import com.qcadoo.mes.materialFlowResources.controllers.dataProvider.ResourcesAttributesDataProvider;

@Controller
@RequestMapping("/resAttributes")
public class ResourcesAttributesController {

    @Autowired
    private ResourcesAttributesDataProvider resourcesAttributesDataProvider;

    @ResponseBody
    @RequestMapping(value = "/columns", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ColumnDTO> getColumns(final Locale locale) {
        return resourcesAttributesDataProvider.getColumns(locale);
    }

    @ResponseBody
    @RequestMapping(value = "/records", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Map<String, Object>> getRecords() {
        return resourcesAttributesDataProvider.getRecords();
    }

}
