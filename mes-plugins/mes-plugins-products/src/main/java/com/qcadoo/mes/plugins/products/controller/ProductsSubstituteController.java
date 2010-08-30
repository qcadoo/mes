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
import com.qcadoo.mes.plugins.products.data.ListData;
import com.qcadoo.mes.plugins.products.validation.ValidationResult;
import com.qcadoo.mes.plugins.products.validation.ValidationService;

@Controller
public class ProductsSubstituteController extends CrudController {

    private static final String JSP_EDIT_SUBSTITUTE_VIEW = "editSubstitute";

    private static final String JSP_EDIT_SUBSTITUTE_COMPONENT_VIEW = "editSubstituteComponent";

    private static final String TYPE_SUBSTITUTE = "products.substitute";

    private static final String TYPE_SUBSTITUTE_COMPONENT = "products.substituteComponent";

    @Autowired
    public ProductsSubstituteController(DataDefinitionService dataDefinitionService, DataAccessService dataAccessService,
            ValidationService validationUtils) {
        super(dataDefinitionService, dataAccessService, LoggerFactory.getLogger(ProductsSubstituteController.class),
                validationUtils);
    }

    @RequestMapping(value = "/products/substitute/editSubstitute", method = RequestMethod.GET)
    public final ModelAndView getEditSubstituteView(@RequestParam Long productId,
            @RequestParam(required = false) Long substituteId) {
        return getEntityFormView(JSP_EDIT_SUBSTITUTE_VIEW, substituteId, TYPE_SUBSTITUTE, productId, "product");
    }

    @RequestMapping(value = "/products/substitute/editSubstitute/save", method = RequestMethod.POST)
    @ResponseBody
    public final ValidationResult saveSubstitute(@ModelAttribute Entity substitute, Locale locale) {
        return saveEntity(substitute, TYPE_SUBSTITUTE, locale);
    }

    @RequestMapping(value = "/products/substitute/editSubstituteComponent", method = RequestMethod.GET)
    public final ModelAndView getEditSubstituteComponentView(@RequestParam Long substituteId,
            @RequestParam(required = false) Long componentId) {
        return getEntityFormView(JSP_EDIT_SUBSTITUTE_COMPONENT_VIEW, componentId, TYPE_SUBSTITUTE_COMPONENT, substituteId,
                "substitute");
    }

    @RequestMapping(value = "/products/substitute/editSubstituteComponent/save", method = RequestMethod.POST)
    @ResponseBody
    public final ValidationResult saveSubstituteComponent(@ModelAttribute Entity substituteComponent, Locale locale) {
        return saveEntity(substituteComponent, TYPE_SUBSTITUTE_COMPONENT, locale);
    }

    @RequestMapping(value = "/products/substitute/data", method = RequestMethod.GET)
    @ResponseBody
    public final ListData getSubstitutesData(@RequestParam Long productId) {
        return getEntitiesGridData(TYPE_SUBSTITUTE, productId, "product");
    }

    @RequestMapping(value = "/products/substitute/components", method = RequestMethod.GET)
    @ResponseBody
    public final ListData getSubstituteComponentsData(@RequestParam Long productId, @RequestParam Long substituteId) {
        return getEntitiesGridData(TYPE_SUBSTITUTE_COMPONENT, substituteId, "substitute");
    }

    @RequestMapping(value = "/products/substitute/deleteSubstitute", method = RequestMethod.POST)
    @ResponseBody
    public final String deleteSubstitute(@RequestBody List<Integer> selectedRows) {
        return deleteEntity(selectedRows, TYPE_SUBSTITUTE);
    }

    @RequestMapping(value = "/products/substitute/deleteSubstituteComponent", method = RequestMethod.POST)
    @ResponseBody
    public final String deleteSubstituteComponent(@RequestBody List<Integer> selectedRows) {
        return deleteEntity(selectedRows, TYPE_SUBSTITUTE_COMPONENT);
    }

}
