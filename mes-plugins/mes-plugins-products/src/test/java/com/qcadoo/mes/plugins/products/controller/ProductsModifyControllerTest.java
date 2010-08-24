package com.qcadoo.mes.plugins.products.controller;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.mes.core.data.api.DataAccessService;
import com.qcadoo.mes.core.data.api.DataDefinitionService;
import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.plugins.products.data.mock.DataAccessServiceMock;
import com.qcadoo.mes.plugins.products.data.mock.DataDefinitionServiceMock;

public class ProductsModifyControllerTest extends TestCase {

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
	public void testEditEntity() {
		testEditEntity("1");
		testEditEntity("2");
	}

	private void testEditEntity(String entityId) {
		ModelAndView modelAndView = controller.editEntity(entityId);
		assertEquals("addModifyEntity", modelAndView.getViewName());

		assertNotNull(modelAndView.getModel().get("headerContent"));
		assertEquals("Produkt:", modelAndView.getModel().get("headerContent"));

		assertNotNull(modelAndView.getModel().get("fieldsDefinition"));
		assertEquals(das.get("product", Long.parseLong(entityId)).getFields(),
				modelAndView.getModel().get("entity"));

	}

	@Test
	public void testAddModifyEntity() {
		Entity entity = new Entity();
		entity.setField("number", "number");
		entity.setField("type", "type");
		entity.setField("typeOfMaterial", "typeOfMaterial");
		entity.setField("ean", "ean");
		entity.setField("category", "category");
		entity.setField("unit", "unit");
		ModelAndView modelAndView = controller.addModifyEntity(entity);
		assertNull(modelAndView.getModel().get("headerContent"));
		assertEquals("redirect:list.html?message=Dodano",
				modelAndView.getViewName());
		entity = new Entity((long) 1);
		entity.setField("number", "number");
		entity.setField("type", "type");
		entity.setField("typeOfMaterial", "typeOfMaterial");
		entity.setField("ean", "ean");
		entity.setField("category", "category");
		entity.setField("unit", "unit");
		modelAndView = controller.addModifyEntity(entity);
		assertNull(modelAndView.getModel().get("headerContent"));
		assertEquals("redirect:list.html?message=Zmodyfikowano",
				modelAndView.getViewName());
		entity.setField("number", null);
		modelAndView = controller.addModifyEntity(entity);
		assertNotNull(modelAndView.getModel().get("headerContent"));

	}

	@Test
	public void testNewEntity() {
		ModelAndView modelAndView = controller.newEntity();
		assertEquals("addModifyEntity", modelAndView.getViewName());

		assertNotNull(modelAndView.getModel().get("headerContent"));
		assertEquals("Produkt:", modelAndView.getModel().get("headerContent"));

		assertNotNull(modelAndView.getModel().get("fieldsDefinition"));

	}

}
