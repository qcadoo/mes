package com.qcadoo.mes.plugins.products.controller;

import org.springframework.stereotype.Controller;

@Controller
public class ProductsSubstituteController extends CrudTemplate {

    // private static final String JSP_EDIT_SUBSTITUTE_VIEW = "editSubstitute";
    //
    // private static final String JSP_EDIT_SUBSTITUTE_COMPONENT_VIEW = "editSubstituteComponent";
    //
    // private static final String TYPE_SUBSTITUTE = "products.substitute";
    //
    // private static final String TYPE_SUBSTITUTE_COMPONENT = "products.substituteComponent";
    //
    // public ProductsSubstituteController() {
    // super(LoggerFactory.getLogger(ProductsSubstituteController.class));
    // }
    //
    // @RequestMapping(value = "/products/substitute/editSubstitute", method = RequestMethod.GET)
    // public ModelAndView getEditSubstituteView(@RequestParam Long productId, @RequestParam(required = false) Long substituteId)
    // {
    // return getEntityFormView(JSP_EDIT_SUBSTITUTE_VIEW, substituteId, TYPE_SUBSTITUTE, productId, "product");
    // }
    //
    // @RequestMapping(value = "/products/substitute/editSubstitute/save", method = RequestMethod.POST)
    // @ResponseBody
    // public ValidationResult saveSubstitute(@ModelAttribute Entity substitute, Locale locale) {
    // return saveEntity(substitute, TYPE_SUBSTITUTE, locale);
    // }
    //
    // @RequestMapping(value = "/products/substitute/editSubstituteComponent", method = RequestMethod.GET)
    // public ModelAndView getEditSubstituteComponentView(@RequestParam Long substituteId,
    // @RequestParam(required = false) Long componentId) {
    // return getEntityFormView(JSP_EDIT_SUBSTITUTE_COMPONENT_VIEW, componentId, TYPE_SUBSTITUTE_COMPONENT, substituteId,
    // "substitute");
    // }
    //
    // @RequestMapping(value = "/products/substitute/editSubstituteComponent/save", method = RequestMethod.POST)
    // @ResponseBody
    // public ValidationResult saveSubstituteComponent(@ModelAttribute Entity substituteComponent, Locale locale) {
    // return saveEntity(substituteComponent, TYPE_SUBSTITUTE_COMPONENT, locale);
    // }
    //
    // @RequestMapping(value = "/products/substitute/data", method = RequestMethod.GET)
    // @ResponseBody
    // public ListData getSubstitutesData(@RequestParam Long productId) {
    // return getEntitiesGridData(TYPE_SUBSTITUTE, productId, "product");
    // }
    //
    // @RequestMapping(value = "/products/substitute/components", method = RequestMethod.GET)
    // @ResponseBody
    // public ListData getSubstituteComponentsData(@RequestParam Long productId, @RequestParam Long substituteId) {
    // return getEntitiesGridData(TYPE_SUBSTITUTE_COMPONENT, substituteId, "substitute");
    // }
    //
    // @RequestMapping(value = "/products/substitute/deleteSubstitute", method = RequestMethod.POST)
    // @ResponseBody
    // public String deleteSubstitute(@RequestBody List<Integer> selectedRows) {
    // return deleteEntity(selectedRows, TYPE_SUBSTITUTE);
    // }
    //
    // @RequestMapping(value = "/products/substitute/deleteSubstituteComponent", method = RequestMethod.POST)
    // @ResponseBody
    // public String deleteSubstituteComponent(@RequestBody List<Integer> selectedRows) {
    // return deleteEntity(selectedRows, TYPE_SUBSTITUTE_COMPONENT);
    // }

}
