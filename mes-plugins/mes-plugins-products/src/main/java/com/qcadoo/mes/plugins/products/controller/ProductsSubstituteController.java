package com.qcadoo.mes.plugins.products.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
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
import com.qcadoo.mes.core.data.definition.FieldDefinition;
import com.qcadoo.mes.core.data.definition.FieldTypeFactory;
import com.qcadoo.mes.core.data.definition.GridDefinition;
import com.qcadoo.mes.core.data.internal.definition.BelongsToFieldType;
import com.qcadoo.mes.core.data.search.Restrictions;
import com.qcadoo.mes.core.data.search.ResultSet;
import com.qcadoo.mes.core.data.search.SearchCriteria;
import com.qcadoo.mes.core.data.search.SearchCriteriaBuilder;
import com.qcadoo.mes.plugins.products.data.ListData;
import com.qcadoo.mes.plugins.products.data.ListDataUtils;
import com.qcadoo.mes.plugins.products.validation.ValidationResult;
import com.qcadoo.mes.plugins.products.validation.ValidationUtils;

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

    @RequestMapping(value = "/products/substitute/editSubstitute", method = RequestMethod.GET)
    public ModelAndView getEditSubstituteView(@RequestParam Long productId, @RequestParam(required = false) Long substituteId) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("editSubstitute");

        DataDefinition substituteDataDefinition = dataDefinitionService.get("products.substitute");
        mav.addObject("substituteFieldsDefinition", substituteDataDefinition.getFields());

        Entity substitute = null;
        if (substituteId != null) {
            substitute = dataAccessService.get("products.substitute", substituteId);
        } else {
            substitute = new Entity();
            Entity product = new Entity(productId);
            substitute.setField("product", product);
        }
        mav.addObject("substitute", substitute);

        return mav;
    }

    @RequestMapping(value = "/products/substitute/editSubstitute/save", method = RequestMethod.POST)
    @ResponseBody
    public ValidationResult saveSubstitute(@ModelAttribute Entity substitute) {
        for (String s : substitute.getFields().keySet()) {
            System.out.println(s + " - " + substitute.getField(s));
        }
        DataDefinition substituteDataDefinition = dataDefinitionService.get("products.substitute");
        ValidationResult validationResult = ValidationUtils.validateEntity(substitute, substituteDataDefinition.getFields());
        if (validationResult.isValid()) {
            dataAccessService.save("products.substitute", validationResult.getValidEntity());
        }
        return validationResult;
    }

    @RequestMapping(value = "/products/substitute/editSubstituteComponent", method = RequestMethod.GET)
    public ModelAndView getEditSubstituteComponentView(@RequestParam Long substituteId,
            @RequestParam(required = false) Long componentId) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("editSubstituteComponent");

        DataDefinition substituteComponentDataDefinition = dataDefinitionService.get("products.substituteComponent");
        mav.addObject("substituteComponentDataDefinition", substituteComponentDataDefinition.getFields());

        Entity substituteComponent = null;
        if (componentId != null) {
            substituteComponent = dataAccessService.get("products.substituteComponent", componentId);
        } else {
            substituteComponent = new Entity();
            Entity substitute = new Entity(substituteId);
            substituteComponent.setField("substitute", substitute);
        }
        mav.addObject("substituteComponent", substituteComponent);

        Map<String, Map<Long, String>> options = new HashMap<String, Map<Long, String>>();
        for (FieldDefinition fieldDef : substituteComponentDataDefinition.getFields()) {
            if (fieldDef.getType().getNumericType() == FieldTypeFactory.NUMERIC_TYPE_BELONGS_TO) {
                BelongsToFieldType belongsToField = (BelongsToFieldType) fieldDef.getType();
                Map<Long, String> fieldOptions = belongsToField.lookup(null);
                options.put(fieldDef.getName(), fieldOptions);
            }
        }
        mav.addObject("options", options);

        return mav;
    }

    @RequestMapping(value = "/products/substitute/editSubstituteComponent/save", method = RequestMethod.POST)
    @ResponseBody
    public ValidationResult saveSubstituteComponent(@ModelAttribute Entity substituteComponent) {
        DataDefinition substituteComponentDataDefinition = dataDefinitionService.get("products.substituteComponent");
        ValidationResult validationResult = ValidationUtils.validateEntity(substituteComponent,
                substituteComponentDataDefinition.getFields());
        if (validationResult.isValid()) {
            dataAccessService.save("products.substituteComponent", validationResult.getValidEntity());
        }
        return validationResult;
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

                SearchCriteria searchCriteria = SearchCriteriaBuilder.forEntity("products.substitute")
                        .restrictedWith(Restrictions.belongsTo("product", Long.valueOf(pId))).build();
                ResultSet rs = dataAccessService.find("products.substitute", searchCriteria);

                DataDefinition dataDefinition = dataDefinitionService.get("products.substitute");
                GridDefinition gridDefinition = dataDefinition.getGrids().get(0);

                return ListDataUtils.generateListData(rs, gridDefinition);
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
                int sId = Integer.parseInt(substituteId);

                SearchCriteria searchCriteria = SearchCriteriaBuilder.forEntity("products.substituteComponent")
                        .restrictedWith(Restrictions.belongsTo("substitute", Long.valueOf(sId))).build();
                ResultSet rs = dataAccessService.find("products.substituteComponent", searchCriteria);

                DataDefinition dataDefinition = dataDefinitionService.get("products.substituteComponent");
                GridDefinition gridDefinition = dataDefinition.getGrids().get(0);

                return ListDataUtils.generateListData(rs, gridDefinition);
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
