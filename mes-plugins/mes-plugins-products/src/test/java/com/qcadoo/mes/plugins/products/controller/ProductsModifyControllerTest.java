package com.qcadoo.mes.plugins.products.controller;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.mes.core.data.api.DataAccessService;
import com.qcadoo.mes.core.data.api.DataDefinitionService;
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
		assertEquals("modifyEntity", modelAndView.getViewName());

		assertNotNull(modelAndView.getModel().get("headerContent"));
		assertEquals("Produkt:", modelAndView.getModel().get("headerContent"));

		assertNotNull(modelAndView.getModel().get("fieldsDefinition"));
		assertEquals(das.get("product", Long.parseLong(entityId)).getFields(),
				modelAndView.getModel().get("entity"));

	}

	@Test
	public void testAddEntity() {
		assertEquals(testAddEntity("1", "a", "a", "a", "a", "a"), "Dodano");
		assertEquals(testAddEntity("2", "a", "", "a", "a", "a"),
				"Wystapil blad");
	}

	private String testAddEntity(String number, String type,
			String typeOfMaterial, String ean, String category, String unit) {
		ModelAndView modelAndView = controller.addEntity(number, type,
				typeOfMaterial, ean, category, unit);
		assertNotNull(modelAndView.getModel().get("data"));
		assertNotNull(modelAndView.getModel().get("message"));
		assertNotNull(modelAndView.getModel().get("fieldsDefinition"));
		return (String) modelAndView.getModel().get("message");
	}

	@Test
	public void testModifyEntity() {
		assertEquals(testModifyEntity("1", "a", "a", "a", "a", "a"),
				"Zedytowano");
		assertEquals(testModifyEntity("2", "a", "", "a", "a", "a"),
				"Wystapil blad");
	}

	private String testModifyEntity(String number, String type,
			String typeOfMaterial, String ean, String category, String unit) {
		ModelAndView modelAndView = controller.modifyEntity(number, type,
				typeOfMaterial, ean, category, unit);
		assertNotNull(modelAndView.getModel().get("data"));
		assertNotNull(modelAndView.getModel().get("message"));
		assertNotNull(modelAndView.getModel().get("fieldsDefinition"));
		return (String) modelAndView.getModel().get("message");
	}

	@Test
	public void testNewEntity() {
		ModelAndView modelAndView = controller.newEntity();
		assertEquals("newEntity", modelAndView.getViewName());

		assertNotNull(modelAndView.getModel().get("headerContent"));
		assertEquals("Produkt:", modelAndView.getModel().get("headerContent"));

		assertNotNull(modelAndView.getModel().get("fieldsDefinition"));

	}
}
