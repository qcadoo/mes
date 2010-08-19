package com.qcadoo.mes.plugins.products.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
			mav.setViewName("modifyEntity");
			mav.addObject("headerContent", "Produkt:");

			DataDefinition dataDefinition = dataDefinitionService
					.get("product");
			List<FieldDefinition> fieldsDefinition = dataDefinition.getFields();
			mav.addObject("fieldsDefinition", fieldsDefinition);
			mav.addObject("entityId", entityId);
			Entity entity = dataAccessService.get("product",
					Long.parseLong(entityId));
			mav.addObject("entity", entity.getFields());

			return mav;
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException(e);
		}

	}

	@RequestMapping(value = "/products/addEntity", method = RequestMethod.GET)
	public ModelAndView addEntity(@RequestParam String number,
			@RequestParam String type, @RequestParam String typeOfMaterial,
			@RequestParam String ean, @RequestParam String category,
			@RequestParam String unit, @RequestParam String name) {
		ModelAndView mav = new ModelAndView();
		String message = "Dodano";
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("number", number);
		data.put("type", type);
		data.put("typeOfMaterial", typeOfMaterial);
		data.put("ean", ean);
		data.put("category", category);
		data.put("unit", unit);
		data.put("name", name);
		Entity entity = new Entity(null, data);

		if (number != null && number.length() != 0 && type != null
				&& type.length() != 0 && typeOfMaterial != null
				&& typeOfMaterial.length() != 0 && ean != null
				&& ean.length() != 0 && category != null
				&& category.length() != 0 && unit != null && unit.length() != 0
				&& name != null && name.length() != 0) {
			dataAccessService.save("product", entity);
		} else {
			message = "Wystapil blad";
		}

		mav.addObject("data", data);
		mav.addObject("message", message);
		DataDefinition dataDefinition = dataDefinitionService.get("product");
		List<FieldDefinition> fieldsDefinition = dataDefinition.getFields();
		mav.addObject("fieldsDefinition", fieldsDefinition);
		mav.setViewName("result");
		return mav;
	}

	@RequestMapping(value = "/products/modifyEntity", method = RequestMethod.GET)
	public ModelAndView modifyEntity(@RequestParam String number,
			@RequestParam String type, @RequestParam String typeOfMaterial,
			@RequestParam String ean, @RequestParam String category,
			@RequestParam String unit, @RequestParam String name,
			@RequestParam String entityId) {
		ModelAndView mav = new ModelAndView();
		String message = "Zedytowano";
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("number", number);
		data.put("type", type);
		data.put("typeOfMaterial", typeOfMaterial);
		data.put("ean", ean);
		data.put("category", category);
		data.put("unit", unit);
		data.put("name", name);
		Entity entity = new Entity(Long.parseLong(entityId), data);
		if (number != null && number.length() != 0 && type != null
				&& type.length() != 0 && typeOfMaterial != null
				&& typeOfMaterial.length() != 0 && ean != null
				&& ean.length() != 0 && category != null
				&& category.length() != 0 && unit != null && unit.length() != 0
				&& name != null && name.length() != 0) {
			dataAccessService.save("product", entity);
		} else {
			message = "Wystapil blad";
		}

		mav.addObject("data", data);
		mav.addObject("message", message);
		DataDefinition dataDefinition = dataDefinitionService.get("product");
		List<FieldDefinition> fieldsDefinition = dataDefinition.getFields();
		mav.addObject("fieldsDefinition", fieldsDefinition);
		mav.setViewName("result");
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