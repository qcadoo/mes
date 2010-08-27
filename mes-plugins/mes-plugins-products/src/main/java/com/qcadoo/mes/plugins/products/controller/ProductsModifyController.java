package com.qcadoo.mes.plugins.products.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.mes.core.data.api.DataAccessService;
import com.qcadoo.mes.core.data.api.DataDefinitionService;
import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.definition.EnumeratedFieldType;
import com.qcadoo.mes.core.data.definition.FieldDefinition;
import com.qcadoo.mes.core.data.definition.GridDefinition;

@Controller
public class ProductsModifyController {

    private DataDefinitionService dataDefinitionService;

    private DataAccessService dataAccessService;

    private Logger logger = LoggerFactory.getLogger(ProductsModifyController.class);

    @Autowired
    public ProductsModifyController(DataDefinitionService dataDefinitionService, DataAccessService dataAccessService) {
        this.dataAccessService = dataAccessService;
        this.dataDefinitionService = dataDefinitionService;
        logger.info("constructor - " + dataDefinitionService);
    }

    @RequestMapping(value = "/products/getEntity", method = RequestMethod.GET)
    public ModelAndView getEntity(@RequestParam(required = false) Long entityId) {
        if (logger.isDebugEnabled()) {
            logger.debug("Get product, product id: " + entityId);
        }
        ModelAndView mav = new ModelAndView();
        mav.setViewName("productsFormView");

        DataDefinition dataDefinitionProduct = dataDefinitionService.get("products.product");
        List<FieldDefinition> fieldsDefinition = dataDefinitionProduct.getFields();
        mav.addObject("fieldsDefinition", fieldsDefinition);

        insertCommonsModelData(mav, fieldsDefinition);

        if (entityId != null && !entityId.equals("")) {
            mav.addObject("entityId", entityId);
            Entity entity = dataAccessService.get("products.product", entityId);
            mav.addObject("entity", entity.getFields());
        }

        return mav;

    }

    @RequestMapping(value = "/products/saveEntity", method = RequestMethod.POST)
    public ModelAndView saveEntity(@ModelAttribute Entity entity) {

        DataDefinition dataDefinitionProduct = dataDefinitionService.get("products.product");
        List<FieldDefinition> fieldsDefinition = dataDefinitionProduct.getFields();

        ModelAndView mav = new ModelAndView();
        if (checkFields(entity, fieldsDefinition, mav)) {

            String message = "";
            dataAccessService.save("products.product", entity);
            if (entity.getId() == null) {
                message = "added";
            } else {
                message = "modified";
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Product had been saved, product fields: " + entity.getFields());
            }
            return new ModelAndView("redirect:list.html?message=" + message);
        } else {

            insertCommonsModelData(mav, fieldsDefinition);
            mav.setViewName("productsFormView");
            mav.addObject("message", "fullFillFields");
            mav.addObject("entity", entity.getFields());
            mav.addObject("fieldsDefinition", fieldsDefinition);
            mav.addObject("entityId", entity.getId());
            if (logger.isDebugEnabled()) {
                logger.debug("Product had not been saved due to fail in fields validation");
            }
            return mav;
        }

    }

    public boolean checkFields(Entity entity, List<FieldDefinition> fieldsDefinition, ModelAndView mav) {

        boolean result = true;
        Map<String, String> fieldsValidationInfo = new HashMap<String, String>();
        for (FieldDefinition field : fieldsDefinition) {
            String formField = (String) entity.getField(field.getName());
            if (field.isRequired()) {
                if (formField == null || formField.trim().equals("")) {
                    String fieldValidationInfo = "requiredField";
                    fieldsValidationInfo.put(field.getName(), fieldValidationInfo);
                    result = false;
                }
            } else {
                if (formField == null || formField.equals("")) {
                    entity.setField(field.getName(), null);
                }
            }

        }
        mav.addObject("fieldsValidationInfo", fieldsValidationInfo);

        return result;
    }

    private void insertCommonsModelData(ModelAndView mav, List<FieldDefinition> fieldsDefinition) {

        Map<String, List<String>> lists = new HashMap<String, List<String>>();
        Map<String, Integer> fieldsTypes = new HashMap<String, Integer>();
        for (FieldDefinition fieldDef : fieldsDefinition) {
            fieldsTypes.put(fieldDef.getName(), fieldDef.getType().getNumericType());
            if (fieldDef.getType().getNumericType() == 4 || fieldDef.getType().getNumericType() == 5) {
                EnumeratedFieldType enumeratedField = (EnumeratedFieldType) fieldDef.getType();
                List<String> options = enumeratedField.values();
                lists.put(fieldDef.getName(), options);
            }
        }

        DataDefinition substituteDataDefinition = dataDefinitionService.get("products.substitute");
        GridDefinition substituteGridDefinition = substituteDataDefinition.getGrids().get(0);
        mav.addObject("substituteGridDefinition", substituteGridDefinition);

        DataDefinition substituteComponentDataDefinition = dataDefinitionService.get("products.substituteComponent");
        GridDefinition substituteComponentGridDefinition = substituteComponentDataDefinition.getGrids().get(0);
        mav.addObject("substituteComponentGridDefinition", substituteComponentGridDefinition);

        mav.addObject("fieldsTypes", fieldsTypes);
        mav.addObject("lists", lists);

    }
}