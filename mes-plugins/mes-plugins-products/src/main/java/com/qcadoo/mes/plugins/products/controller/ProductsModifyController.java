package com.qcadoo.mes.plugins.products.controller;

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
import com.qcadoo.mes.core.data.definition.FieldDefinition;

@Controller
public class ProductsModifyController {

	private DataDefinitionService dataDefinitionService;

	private DataAccessService dataAccessService;

	private Logger logger = LoggerFactory
			.getLogger(ProductsModifyController.class);

	@Autowired
	public ProductsModifyController(
			DataDefinitionService dataDefinitionService,
			DataAccessService dataAccessService) {
		this.dataAccessService = dataAccessService;
		this.dataDefinitionService = dataDefinitionService;
		logger.info("constructor - " + dataDefinitionService);
	}

	@RequestMapping(value = "/products/editEntity", method = RequestMethod.GET)
	public ModelAndView editEntity(@RequestParam String entityId) {
		try {
			ModelAndView mav = new ModelAndView();
			mav.setViewName("addModifyEntity");
			mav.addObject("headerContent", "Produkt:");

			DataDefinition dataDefinition = dataDefinitionService
					.get("product");
			List<FieldDefinition> fieldsDefinition = dataDefinition.getFields();
			mav.addObject("fieldsDefinition", fieldsDefinition);
			mav.addObject("entityId", entityId);
			Entity entity = dataAccessService.get("product",
					Long.parseLong(entityId));
			mav.addObject("entity", entity.getFields());
			mav.addObject("button", "Zatwierdz");

			return mav;
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException(e);
		}

	}

	@RequestMapping(value = "/products/addModifyEntity", method = RequestMethod.POST)
	public ModelAndView addModifyEntity(@ModelAttribute Entity entity) {

		String message = "";
		if (checkFields(entity)) {
			dataAccessService.save("product", entity);
			if (entity.getId() == null) {
				message = "Dodano";
			} else {
				message = "Zmodyfikowano";
			}
			return new ModelAndView("redirect:list.html?message=" + message);
		} else {
			ModelAndView mav = new ModelAndView();
			mav.setViewName("addModifyEntity");
			mav.addObject("headerContent", "Produkt:");
			mav.addObject("entity", entity.getFields());
			DataDefinition dataDefinition = dataDefinitionService
					.get("product");
			List<FieldDefinition> fieldsDefinition = dataDefinition.getFields();
			mav.addObject("fieldsDefinition", fieldsDefinition);
			mav.addObject("entityId", entity.getId());
			mav.addObject("button", "Zatwierdz");
			return mav;
		}

	}

	@RequestMapping(value = "/products/newEntity", method = RequestMethod.GET)
	public ModelAndView newEntity() {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("addModifyEntity");
		mav.addObject("headerContent", "Produkt:");

		DataDefinition dataDefinition = dataDefinitionService.get("product");
		List<FieldDefinition> fieldsDefinition = dataDefinition.getFields();
		mav.addObject("fieldsDefinition", fieldsDefinition);
		mav.addObject("button", "Zatwierdz");
		return mav;
	}

	public boolean checkFields(Entity entity) {
		boolean result = true;
		Map<String, Object> map = entity.getFields();
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			if (entry.getValue() == null || entry.getValue() == "")
				result = false;
		}
		return result;

	}

}