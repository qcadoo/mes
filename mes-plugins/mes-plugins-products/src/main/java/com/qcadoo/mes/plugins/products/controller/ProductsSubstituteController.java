package com.qcadoo.mes.plugins.products.controller;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.qcadoo.mes.core.data.api.DataAccessService;
import com.qcadoo.mes.core.data.api.DataDefinitionService;
import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.search.SearchCriteria;
import com.qcadoo.mes.core.data.search.SearchCriteriaBuilder;
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
                SearchCriteriaBuilder searchCriteriaBuilder = SearchCriteriaBuilder.forEntity("productSubstitute");

                SearchCriteria searchCriteria = searchCriteriaBuilder.build();

                // ResultSet rs = dataAccessService.find("productSubstitute", searchCriteria);
                List<Entity> entities = new LinkedList<Entity>();
                Entity e1 = new Entity();
                e1.setId((long) 1);
                e1.setField("f1", "t11");
                e1.setField("f2", "t12");
                entities.add(e1);
                Entity e2 = new Entity();
                e2.setId((long) 2);
                e2.setField("f1", "t21");
                e2.setField("f2", "t22");
                entities.add(e2);
                Entity e3 = new Entity();
                e3.setId((long) 3);
                e3.setField("f1", "t31");
                e3.setField("f2", "t32");
                entities.add(e3);
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
}
