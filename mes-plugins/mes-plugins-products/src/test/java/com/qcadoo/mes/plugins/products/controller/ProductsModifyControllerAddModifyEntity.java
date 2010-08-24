package com.qcadoo.mes.plugins.products.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.mes.core.data.api.DataAccessService;
import com.qcadoo.mes.core.data.api.DataDefinitionService;
import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.plugins.products.data.mock.DataAccessServiceMock;
import com.qcadoo.mes.plugins.products.data.mock.DataDefinitionServiceMock;

public class ProductsModifyControllerAddModifyEntity {

	private ProductsModifyController controller;

	private DataDefinitionService dds;
	private DataAccessService das;

	@Before
	public void setUp() {
		dds = new DataDefinitionServiceMock();
		das = new DataAccessServiceMock();
		controller = new ProductsModifyController(dds, das);
	}

	@Test
	public void shouldReturnAddConfirmationWhenRequiredDataIsGiven() {
		// given
		Entity entity = new Entity();
		entity.setField("number", "number");
		entity.setField("type", "type");
		entity.setField("typeOfMaterial", "typeOfMaterial");
		entity.setField("ean", "ean");
		entity.setField("category", "category");
		entity.setField("unit", "unit");
		// when
		ModelAndView modelAndView = controller.addModifyEntity(entity);
		// then
		assertEquals("redirect:list.html?message=Dodano",
				modelAndView.getViewName());

	}

	@Test
	public void shouldReturnEditConfirmationWhenRequiredDataIsGiven() {
		// given
		Entity entity = new Entity((long) 1);
		entity.setField("number", "number");
		entity.setField("type", "type");
		entity.setField("typeOfMaterial", "typeOfMaterial");
		entity.setField("ean", "ean");
		entity.setField("category", "category");
		entity.setField("unit", "unit");
		// when
		ModelAndView modelAndView = controller.addModifyEntity(entity);
		// then
		assertEquals("redirect:list.html?message=Zmodyfikowano",
				modelAndView.getViewName());

	}

	@Test
	public void shouldRedirectToFormIfRequiredDataIsIncompleteAndOnNewForm() {
		// given
		Entity entity = new Entity((long) 1);
		entity.setField("number", null);
		entity.setField("type", "type");
		entity.setField("typeOfMaterial", "typeOfMaterial");
		entity.setField("ean", "ean");
		entity.setField("category", "category");
		entity.setField("unit", "unit");
		// when
		ModelAndView modelAndView = controller.addModifyEntity(entity);
		// then
		assertNotNull(modelAndView.getModel().get("message"));
		assertNotNull(modelAndView.getModel().get("fieldsDefinition"));
		assertNotNull(modelAndView.getModel().get("entity"));
		assertNotNull(modelAndView.getModel().get("entityId"));
	}

	@Test
	public void shouldRedirectToFormIfRequiredDataIsIncompleteAndOnModifyForm() {
		// given
		Entity entity = new Entity();
		entity.setField("number", null);
		entity.setField("type", "type");
		entity.setField("typeOfMaterial", "typeOfMaterial");
		entity.setField("ean", "ean");
		entity.setField("category", "category");
		entity.setField("unit", "unit");
		// when
		ModelAndView modelAndView = controller.addModifyEntity(entity);
		// then
		assertNotNull(modelAndView.getModel().get("message"));
		assertNotNull(modelAndView.getModel().get("fieldsDefinition"));

	}
}
