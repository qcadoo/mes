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

import com.qcadoo.mes.core.data.api.DataAccessService;
import com.qcadoo.mes.core.data.api.DataDefinitionService;
import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.plugins.products.data.ListData;

@Controller
public class ProductsSubstituteController {

    private DataDefinitionService dataDefinitionService;

    private DataAccessService dataAccessService;

    private Logger logger = LoggerFactory.getLogger(ProductsSubstituteController.class);

    @Autowired
    public ProductsSubstituteController(DataDefinitionService dataDefinitionService, DataAccessService dataAccessService) {
        this.dataDefinitionService = dataDefinitionService;
        this.dataAccessService = dataAccessService;
        if (logger.isDebugEnabled()) {
            logger.debug("constructor - " + dataDefinitionService);
        }
    }

    @RequestMapping(value = "/products/substitute/data", method = RequestMethod.GET)
    @ResponseBody
    public ListData getProductSubstitutesData(@RequestParam String productId) {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("getProductSubstitutesData - PRODUCT ID: " + productId);
            }
            try {
                int pId = Integer.parseInt(productId);

                // SearchCriteriaBuilder searchCriteriaBuilder = SearchCriteriaBuilder.forEntity("products.substitute");
                // SearchCriteria searchCriteria = searchCriteriaBuilder.build();
                // ResultSet rs = dataAccessService.find("products.substitute", searchCriteria);
                // return new ListData(rs.getTotalNumberOfEntities(), rs.getResults());

                List<Entity> entities = new LinkedList<Entity>();
                for (int i = 1; i < 4; i++) {
                    Entity e = new Entity();
                    e.setId((long) i);
                    e.setField("no", "no-" + i + "-" + pId);
                    e.setField("number", "number-" + i + "-" + pId);
                    e.setField("name", "name-" + i + "-" + pId);
                    entities.add(e);
                }
                int totalNumberOfEntities = 3;
                return new ListData(totalNumberOfEntities, entities);

            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(e);
            }
        } catch (Exception e) {
            // if (printException)
            e.printStackTrace();
            return null;
        }
    }

    @RequestMapping(value = "/products/substitute/components", method = RequestMethod.GET)
    @ResponseBody
    public ListData getSubstituteComponentsData(@RequestParam String productId, @RequestParam String substituteId) {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("getSubstituteProductsData - PRODUCT ID: " + productId + ", SUBSTITUTE ID: " + substituteId);
            }
            try {
                int pId = Integer.parseInt(productId);
                int sId = Integer.parseInt(substituteId);
                // SearchCriteriaBuilder searchCriteriaBuilder = SearchCriteriaBuilder.forEntity("productSubstitute");

                // SearchCriteria searchCriteria = searchCriteriaBuilder.build();

                // ResultSet rs = dataAccessService.find("productSubstitute", searchCriteria);
                List<Entity> entities = new LinkedList<Entity>();
                for (int i = 1; i < 3; i++) {
                    Entity e = new Entity();
                    e.setId((long) i);
                    e.setField("number", "number-" + i + "-" + pId + "-" + sId);
                    e.setField("name", "name-" + i + "-" + pId + "-" + sId);
                    e.setField("quantity", "no-" + i + "-" + pId + "-" + sId);
                    entities.add(e);
                }
                int totalNumberOfEntities = 2;
                return new ListData(totalNumberOfEntities, entities);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(e);
            }
        } catch (Exception e) {
            // if (printException)
            e.printStackTrace();
            return null;
        }
    }

    @RequestMapping(value = "/products/substitute/deleteSubstitute", method = RequestMethod.POST)
    @ResponseBody
    public String deleteSubstitute(@RequestBody List<Integer> selectedRows) {
        if (logger.isDebugEnabled()) {
            logger.debug("deleteSubstitute - SELECTED SUBSTITUTES: " + selectedRows);
        }
        for (Integer recordId : selectedRows) {
            dataAccessService.delete("products.substitute", (long) recordId);
            if (logger.isDebugEnabled()) {
                logger.debug("SUBSTITUTE " + recordId + " DELETED");
            }
        }
        return "ok";
    }

    @RequestMapping(value = "/products/substitute/deleteSubstituteComponent", method = RequestMethod.POST)
    @ResponseBody
    public String deleteSubstituteComponent(@RequestBody List<Integer> selectedRows) {
        if (logger.isDebugEnabled()) {
            logger.debug("deleteSubstitute - SELECTED SUBSTITUTE COMPONENTS: " + selectedRows);
        }
        for (Integer recordId : selectedRows) {
            dataAccessService.delete("products.substituteComponent", (long) recordId);
            if (logger.isDebugEnabled()) {
                logger.debug("SUBSTITUTE COMPONENT" + recordId + " DELETED");
            }
        }
        return "ok";
    }

}
