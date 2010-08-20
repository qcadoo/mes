package com.qcadoo.mes.plugins.products.controller;

import java.util.LinkedList;
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
import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.definition.GridDefinition;
import com.qcadoo.mes.core.data.search.ResultSet;
import com.qcadoo.mes.core.data.search.SearchCriteria;
import com.qcadoo.mes.core.data.search.SearchCriteriaBuilder;
import com.qcadoo.mes.plugins.products.data.ListData;

@Controller
public class ProductsListController {

    private DataDefinitionService dataDefinitionService;

    private DataAccessService dataAccessService;

    private Logger logger = LoggerFactory.getLogger(ProductsListController.class);

    private boolean printException = true;

    @Autowired
    public ProductsListController(DataDefinitionService dataDefinitionService, DataAccessService dataAccessService) {
        this.dataDefinitionService = dataDefinitionService;
        this.dataAccessService = dataAccessService;
        logger.info("constructor - " + dataDefinitionService);
    }

    @RequestMapping(value = "/products/list", method = RequestMethod.GET)
    public ModelAndView productsList(@RequestParam(required = false) String message) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("productsGridView");
        mav.addObject("headerContent", "Produkty:");
        if (message != null) {
            mav.addObject("message", message);
        }
        mav.addObject("message", message);
        DataDefinition dataDefinition = dataDefinitionService.get("product");
        List<GridDefinition> grids = dataDefinition.getGrids();
        GridDefinition gridDefinition = grids.get(0);
        mav.addObject("gridDefinition", gridDefinition);

        return mav;
    }

    @RequestMapping(value = "/products/listData", method = RequestMethod.GET)
    @ResponseBody
    public ListData getListData(@RequestParam String maxResults, @RequestParam String firstResult) {
        try {
            logger.debug("MAX RES: " + maxResults);
            logger.debug("FIRST RES: " + firstResult);
            try {
                int max = Integer.parseInt(maxResults);
                int first = Integer.parseInt(firstResult);
                if (max < 0 || first < 0) {
                    throw new IllegalArgumentException();
                }
                SearchCriteria searchCriteria = SearchCriteriaBuilder.forEntity("product").withMaxResults(max)
                        .withFirstResult(first).build();

                ResultSet rs = dataAccessService.find("product", searchCriteria);
                List<Entity> entities = rs.getResults();
                int totalNumberOfEntities = rs.getTotalNumberOfEntities();
                return new ListData(totalNumberOfEntities, entities);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(e);
            }
        } catch (Exception e) {
            if (printException)
                e.printStackTrace();
            return null;
        }
    }

    @RequestMapping(value = "/products/deleteData", method = RequestMethod.POST)
    @ResponseBody
    public String deleteData(@RequestBody List<String> selectedRows) {
        logger.debug("SELECTED ROWS: " + selectedRows);
        try {
            List<Long> rowsToDelete = new LinkedList<Long>();
            for (String recordIdStr : selectedRows) {
                rowsToDelete.add(Long.parseLong(recordIdStr));
            }
            for (Long recordId : rowsToDelete) {
                dataAccessService.delete("product", recordId);
                logger.debug("ROW " + recordId + " DELETED");
            }
        } catch (Exception e) {
            if (printException)
                e.printStackTrace();
            return "error";
        }

        return "ok";
    }

    public void setPrintException(boolean printException) {
        this.printException = printException;
    }
}
