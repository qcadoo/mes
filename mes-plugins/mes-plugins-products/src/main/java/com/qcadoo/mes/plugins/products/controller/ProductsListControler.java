package com.qcadoo.mes.plugins.products.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.mes.core.data.api.DataAccessService;
import com.qcadoo.mes.core.data.api.DataDefinitionService;
import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.definition.GridDefinition;
import com.qcadoo.mes.core.data.search.ResultSet;
import com.qcadoo.mes.core.data.search.SearchCriteria;
import com.qcadoo.mes.plugins.products.data.mock.SearchCriteriaMock;

@Controller
public class ProductsListControler {

	private DataDefinitionService dataDefinitionService;

	private DataAccessService dataAccessService;

	private Logger logger = LoggerFactory
			.getLogger(ProductsListControler.class);

	@Autowired
	public ProductsListControler(DataDefinitionService dataDefinitionService,
			DataAccessService dataAccessService) {
		this.dataDefinitionService = dataDefinitionService;
		this.dataAccessService = dataAccessService;
		logger.info("constructor - " + dataDefinitionService);
	}

	@RequestMapping(value = "/products/list", method = RequestMethod.GET)
	public ModelAndView productsList() {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("productsGridView");
		mav.addObject("headerContent", "Produkty:");

		DataDefinition dataDefinition = dataDefinitionService.get("product");
		List<GridDefinition> grids = dataDefinition.getGrids();
		GridDefinition gridDefinition = grids.get(0);
		mav.addObject("gridDefinition", gridDefinition);

		SearchCriteria searchCriteria = new SearchCriteriaMock("product", 100,
				0);
		ResultSet rs = dataAccessService.find("product", searchCriteria);
		List<Entity> entities = rs.getResults();

		mav.addObject("entities", entities);

		return mav;
	}

}
