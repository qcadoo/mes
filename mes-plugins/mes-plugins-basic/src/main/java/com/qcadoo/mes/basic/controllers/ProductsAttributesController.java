package com.qcadoo.mes.basic.controllers;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.qcadoo.mes.basic.controllers.dataProvider.ProductsAttributesDataProvider;
import com.qcadoo.mes.basic.controllers.dataProvider.dto.ColumnDTO;

@Controller
@RequestMapping("/prodAttributes")
public class ProductsAttributesController {

    @Autowired
    private ProductsAttributesDataProvider productsAttributesDataProvider;

    @ResponseBody
    @RequestMapping(value = "/columns", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ColumnDTO> getColumns(final Locale locale) {
        return productsAttributesDataProvider.getColumns(locale);
    }

    @ResponseBody
    @RequestMapping(value = "/records", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Map<String, Object>> getRecords() {
        return productsAttributesDataProvider.getRecords();
    }

}
