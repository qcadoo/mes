package com.qcadoo.mes.plugins.products.controller;

import java.util.List;
import java.util.Locale;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.definition.GridDefinition;
import com.qcadoo.mes.plugins.products.data.ListData;
import com.qcadoo.mes.plugins.products.validation.ValidationResult;

@Controller
public class ProductsController extends CrudTemplate {

    private static final String JSP_VIEW_GRID = "productsGridView";

    private static final String JSP_VIEW_FORM = "productsFormView";

    private static final String TYPE_PRODUCT = "products.product";

    private static final String TYPE_SUBSTITUTE = "products.substitute";

    private static final String TYPE_SUBSTITUTE_COMPONENT = "products.substituteComponent";

    public ProductsController() {
        super(LoggerFactory.getLogger(ProductsController.class));
    }

    @RequestMapping(value = "/products/list", method = RequestMethod.GET)
    public ModelAndView getProductsListView(@RequestParam(required = false) String message) {
        return getEntityListView(JSP_VIEW_GRID, TYPE_PRODUCT, message);
    }

    @RequestMapping(value = "/products/list/data", method = RequestMethod.GET)
    @ResponseBody
    public ListData getProductsListData(@RequestParam int maxResults, @RequestParam int firstResult,
            @RequestParam(required = false) String sortColumn, @RequestParam(required = false) String sortOrder) {
        return getEntitiesGridData(TYPE_PRODUCT, maxResults, firstResult, sortColumn, sortOrder);
    }

    @RequestMapping(value = "/products/list/delete", method = RequestMethod.POST)
    @ResponseBody
    public String deleteData(@RequestBody List<Integer> selectedRows) {
        return deleteEntity(selectedRows, TYPE_PRODUCT);
    }

    @RequestMapping(value = "/products/getEntity", method = RequestMethod.GET)
    public ModelAndView getProductFormView(@RequestParam(required = false) Long productId) {
        ModelAndView mav = getEntityFormView(JSP_VIEW_FORM, productId, TYPE_PRODUCT, null, null);

        DataDefinition substituteDataDefinition = dataDefinitionService.get(TYPE_SUBSTITUTE);
        GridDefinition substituteGridDefinition = substituteDataDefinition.getGrids().get(0);
        mav.addObject("substituteGridDefinition", substituteGridDefinition);

        DataDefinition substituteComponentDataDefinition = dataDefinitionService.get(TYPE_SUBSTITUTE_COMPONENT);
        GridDefinition substituteComponentGridDefinition = substituteComponentDataDefinition.getGrids().get(0);
        mav.addObject("substituteComponentGridDefinition", substituteComponentGridDefinition);

        return mav;
    }

    @RequestMapping(value = "/products/saveEntity", method = RequestMethod.POST)
    @ResponseBody
    public ValidationResult saveProduct(@ModelAttribute Entity product, Locale locale) {
        return saveEntity(product, TYPE_PRODUCT, locale);
    }
}
