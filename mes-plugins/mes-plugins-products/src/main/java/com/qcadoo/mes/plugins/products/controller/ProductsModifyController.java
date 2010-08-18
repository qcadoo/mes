package com.qcadoo.mes.plugins.products.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.mes.core.data.api.DataAccessService;
import com.qcadoo.mes.core.data.api.DataDefinitionService;
import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.definition.FieldDefinition;

@Controller
public class ProductsModifyController {

	private DataDefinitionService dataDefinitionService;

	private DataAccessService dataAccessService;

	private Logger logger = LoggerFactory
			.getLogger(ProductsListControler.class);

	@Autowired
	public ProductsModifyController(
			DataDefinitionService dataDefinitionService,
			DataAccessService dataAccessService) {
		this.dataDefinitionService = dataDefinitionService;
		this.dataAccessService = dataAccessService;
		logger.info("constructor - " + dataDefinitionService);
	}

	@RequestMapping(value = "/products/editEntity", method = RequestMethod.GET)
	public ModelAndView editEntity(@RequestParam String entityId) {
		try {
			ModelAndView mav = new ModelAndView();
			mav.setViewName("modifyEntity");
			mav.addObject("headerContent", "Produkt:");

			DataDefinition dataDefinition = dataDefinitionService
					.get("product");
			List<FieldDefinition> fieldsDefinition = dataDefinition.getFields();
			mav.addObject("fieldsDefinition", fieldsDefinition);
			Entity entity = dataAccessService.get("product",
					Long.parseLong(entityId));
			mav.addObject("entity", entity.getFields());
			return mav;
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException(e);
		}

	}

	@RequestMapping(value = "/products/modifyEntity", method = RequestMethod.GET)
	public ModelAndView modifyEntity() {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("productsGridView");
		return mav;
	}

	@RequestMapping(value = "/products/newEntity", method = RequestMethod.GET)
	public ModelAndView newEntity() {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("newEntity");
		mav.addObject("headerContent", "Produkt:");

		DataDefinition dataDefinition = dataDefinitionService.get("product");
		List<FieldDefinition> fieldsDefinition = dataDefinition.getFields();
		mav.addObject("fieldsDefinition", fieldsDefinition);
		return mav;
	}
}