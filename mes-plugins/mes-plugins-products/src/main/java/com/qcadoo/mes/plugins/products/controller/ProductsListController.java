package com.qcadoo.mes.plugins.products.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.mes.core.data.api.DataAccessService;
import com.qcadoo.mes.core.data.api.DataDefinitionService;
import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.definition.GridDefinition;
import com.qcadoo.mes.core.data.search.Order;
import com.qcadoo.mes.core.data.search.ResultSet;
import com.qcadoo.mes.core.data.search.SearchCriteria;
import com.qcadoo.mes.core.data.search.SearchCriteriaBuilder;
import com.qcadoo.mes.plugins.products.data.ListData;
import com.qcadoo.mes.plugins.products.data.ListDataUtils;

@Controller
public class ProductsListController {

    private DataDefinitionService dataDefinitionService;

    private DataAccessService dataAccessService;

    private Logger logger = LoggerFactory.getLogger(ProductsListController.class);

    @Autowired
    public ProductsListController(DataDefinitionService dataDefinitionService, DataAccessService dataAccessService) {
        this.dataDefinitionService = dataDefinitionService;
        this.dataAccessService = dataAccessService;
        if (logger.isDebugEnabled()) {
            logger.info("constructor - " + dataDefinitionService);
        }
    }

    @RequestMapping(value = "/products/list", method = RequestMethod.GET)
    public ModelAndView getProductsListView(@RequestParam(required = false) String message) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("productsGridView");
        mav.addObject("headerContent", "Produkty:");
        if (message != null) {
            mav.addObject("message", message);
        }
        mav.addObject("message", message);
        DataDefinition dataDefinition = dataDefinitionService.get("products.product");
        List<GridDefinition> grids = dataDefinition.getGrids();
        GridDefinition gridDefinition = grids.get(0);
        mav.addObject("gridDefinition", gridDefinition);

        return mav;
    }

    @RequestMapping(value = "/products/list/data", method = RequestMethod.GET)
    @ResponseBody
    public ListData getProductsListData(@RequestParam int maxResults, @RequestParam int firstResult,
            @RequestParam(required = false) String sortColumn, @RequestParam(required = false) String sortOrder) {

        if (logger.isDebugEnabled()) {
            logger.debug("getListData - MAX RES: " + maxResults + ", FIRST RES: " + firstResult + ", SORT COL: " + sortColumn
                    + ", SORT ORDER: " + sortOrder);
        }

        if (maxResults < 0 || firstResult < 0) {
            throw new IllegalArgumentException();
        }
        SearchCriteriaBuilder searchCriteriaBuilder = SearchCriteriaBuilder.forEntity("products.product")
                .withMaxResults(maxResults).withFirstResult(firstResult);
        if (sortColumn != null && sortOrder != null) {
            if ("desc".equals(sortOrder)) {
                searchCriteriaBuilder = searchCriteriaBuilder.orderBy(Order.desc(sortColumn));
            } else {
                searchCriteriaBuilder = searchCriteriaBuilder.orderBy(Order.asc(sortColumn));
            }
        }
        SearchCriteria searchCriteria = searchCriteriaBuilder.build();

        ResultSet rs = dataAccessService.find("products.product", searchCriteria);

        DataDefinition dataDefinition = dataDefinitionService.get("products.product");
        GridDefinition gridDefinition = dataDefinition.getGrids().get(0);

        return ListDataUtils.generateListData(rs, gridDefinition);

    }

    @RequestMapping(value = "/products/list/delete", method = RequestMethod.POST)
    @ResponseBody
    public String deleteData(@RequestBody List<Integer> selectedRows) {
        if (logger.isDebugEnabled()) {
            logger.debug("SELECTED ROWS: " + selectedRows);
        }
        for (Integer recordId : selectedRows) {
            dataAccessService.delete("products.product", (long) recordId);
            if (logger.isDebugEnabled()) {
                logger.debug("ROW " + recordId + " DELETED");
            }
        }
        return "ok";
    }
}
