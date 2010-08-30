package com.qcadoo.mes.plugins.products.controller;

import java.util.List;
import java.util.Locale;

import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.mes.core.data.api.DataAccessService;
import com.qcadoo.mes.core.data.api.DataDefinitionService;
import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.plugins.products.data.ListData;
import com.qcadoo.mes.plugins.products.validation.ValidationResult;
import com.qcadoo.mes.plugins.products.validation.ValidationService;

public class CrudTemplate {

    private CrudController controller;

    private String dataType;

    public CrudTemplate(String dataType, DataDefinitionService dataDefinitionService, DataAccessService dataAccessService,
            ValidationService validationUtils) {
        controller = new CrudController(dataDefinitionService, dataAccessService, LoggerFactory.getLogger(this.getClass()),
                validationUtils);
        this.dataType = dataType;
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public final ModelAndView getProductsListView(@RequestParam(required = false) String message) {
        return controller.getEntityListView(dataType + "/grid.jsp", dataType, message);
    }

    @RequestMapping(value = "/list/data", method = RequestMethod.GET)
    @ResponseBody
    public final ListData getProductsListData(@RequestParam int maxResults, @RequestParam int firstResult,
            @RequestParam(required = false) String sortColumn, @RequestParam(required = false) String sortOrder) {
        return controller.getEntitiesGridData(dataType, maxResults, firstResult, sortColumn, sortOrder);
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public final String deleteData(@RequestBody List<Integer> selectedRows) {
        return controller.deleteEntity(selectedRows, dataType);
    }

    @RequestMapping(value = "/getEntity", method = RequestMethod.GET)
    public final ModelAndView getProductFormView(@RequestParam(required = false) Long productId) {
        ModelAndView mav = controller.getEntityFormView(dataType + "/entity.jsp", productId, dataType, null, null);
        return mav;
    }

    @RequestMapping(value = "/saveEntity", method = RequestMethod.POST)
    @ResponseBody
    public final ValidationResult saveProduct(@ModelAttribute Entity product, Locale locale) {
        return controller.saveEntity(product, dataType, locale);
    }

}
