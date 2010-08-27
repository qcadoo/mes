package com.qcadoo.mes.plugins.products.controller;

import java.util.List;
import java.util.Locale;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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
import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.definition.GridDefinition;
import com.qcadoo.mes.plugins.products.data.ListData;
import com.qcadoo.mes.plugins.products.validation.ValidationResult;
import com.qcadoo.mes.plugins.products.validation.ValidationService;

@Controller
public class ProductsController extends CrudTemplate {

    private DataDefinitionService dataDefinitionService;

    @Autowired
    public ProductsController(DataDefinitionService dataDefinitionService, DataAccessService dataAccessService,
            ValidationService validationUtils) {
        super(dataDefinitionService, dataAccessService, LoggerFactory.getLogger(ProductsController.class), validationUtils);
        this.dataDefinitionService = dataDefinitionService;
    }

    @RequestMapping(value = "/products/list", method = RequestMethod.GET)
    public ModelAndView getProductsListView(@RequestParam(required = false) String message) {
        return getEntityListView("productsGridView", "products.product", message);
    }

    @RequestMapping(value = "/products/list/data", method = RequestMethod.GET)
    @ResponseBody
    public ListData getProductsListData(@RequestParam int maxResults, @RequestParam int firstResult,
            @RequestParam(required = false) String sortColumn, @RequestParam(required = false) String sortOrder) {
        return getEntitiesGridData("products.product", maxResults, firstResult, sortColumn, sortOrder);
    }

    @RequestMapping(value = "/products/list/delete", method = RequestMethod.POST)
    @ResponseBody
    public String deleteData(@RequestBody List<Integer> selectedRows) {
        return deleteEntity(selectedRows, "products.product");
    }

    @RequestMapping(value = "/products/getEntity", method = RequestMethod.GET)
    public ModelAndView getProductFormView(@RequestParam(required = false) Long productId) {
        ModelAndView mav = getEntityFormView("productsFormView", productId, "products.product", null, null);

        DataDefinition substituteDataDefinition = dataDefinitionService.get("products.substitute");
        GridDefinition substituteGridDefinition = substituteDataDefinition.getGrids().get(0);
        mav.addObject("substituteGridDefinition", substituteGridDefinition);

        DataDefinition substituteComponentDataDefinition = dataDefinitionService.get("products.substituteComponent");
        GridDefinition substituteComponentGridDefinition = substituteComponentDataDefinition.getGrids().get(0);
        mav.addObject("substituteComponentGridDefinition", substituteComponentGridDefinition);

        return mav;
    }

    @RequestMapping(value = "/products/saveEntity", method = RequestMethod.POST)
    @ResponseBody
    public ValidationResult saveProduct(@ModelAttribute Entity product, Locale locale) {
        return saveEntity(product, "products.product", locale);
    }
}
