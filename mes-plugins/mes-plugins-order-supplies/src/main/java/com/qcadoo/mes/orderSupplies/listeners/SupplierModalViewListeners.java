/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
 * Version: 1.4
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.orderSupplies.listeners;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.ParameterFields;
import com.qcadoo.mes.orderSupplies.constants.CoverageProductFields;
import com.qcadoo.mes.orderSupplies.constants.CoverageProductSelectedFields;
import com.qcadoo.mes.orderSupplies.constants.OrderSuppliesConstants;
import com.qcadoo.mes.productCatalogNumbers.ProductCatalogNumbersService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.utils.NumberGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class SupplierModalViewListeners {

	private static final String L_FORM_SUPPLIER = "supplier";

	@Autowired
	private DataDefinitionService dataDefinitionService;

	@Autowired
	private NumberGeneratorService numberGeneratorService;

	@Autowired
	private ParameterService parameterService;

	@Autowired
	private ProductCatalogNumbersService productCatalogNumbersService;

	private DataDefinition coverageProductSelectedDataDefinition;

	private DataDefinition requestForQuotationProductDataDefinition;

	private DataDefinition orderedProductDataDefinition;

	private DataDefinition requestForQuotationDataDefinition;

	private DataDefinition deliveryDataDefinition;

	public void init() {
		coverageProductSelectedDataDefinition = dataDefinitionService.get(
				OrderSuppliesConstants.PLUGIN_IDENTIFIER,
				CoverageProductSelectedFields.ENTITY_NAME);
		requestForQuotationProductDataDefinition = dataDefinitionService.get(
				"supplyNegotiations", "requestForQuotationProduct");
		requestForQuotationDataDefinition = dataDefinitionService.get(
				"supplyNegotiations", "requestForQuotation");
		deliveryDataDefinition = dataDefinitionService.get("deliveries",
				"delivery");
		orderedProductDataDefinition = dataDefinitionService.get("deliveries",
				"orderedProduct");
	}

	/**
	 * Saving requestForQuotation and generating and attaching products
	 * 
	 * @param view
	 * @param state
	 * @param args
	 */
	private Long generateRequestProducts(final ViewDefinitionState view,
			final ComponentState state, final String[] args) {
		init();
		LookupComponent lookup = (LookupComponent) view
				.getComponentByReference(L_FORM_SUPPLIER);

		Entity supplier = lookup.getEntity();
		String number = numberGeneratorService.generateNumber(
				"supplyNegotiations", "requestForQuotation");
		Entity requestForQuotation = requestForQuotationDataDefinition.create();
		requestForQuotation.setField("number", number);
		requestForQuotation.setField("supplier", supplier);
		Entity saved = requestForQuotationDataDefinition
				.save(requestForQuotation);

		coverageProductSelectedDataDefinition
				.find()
				.list()
				.getEntities()
				.forEach(
						selected -> {
							Entity coverageProduct = selected
									.getBelongsToField(CoverageProductSelectedFields.COVERAGE_PRODUCT);
							Entity product = coverageProduct
									.getBelongsToField(CoverageProductFields.PRODUCT);
							BigDecimal reserveMissingQuantity = coverageProduct
									.getDecimalField(CoverageProductFields.RESERVE_MISSING_QUANTITY);
							Entity productCatalogNumber = productCatalogNumbersService
									.getProductCatalogNumber(product, supplier);

							Entity requestForQuotationProduct = requestForQuotationProductDataDefinition
									.create();
							requestForQuotationProduct.setField(
									"requestForQuotation", saved);
							requestForQuotationProduct.setField("product",
									product);
							requestForQuotationProduct.setField(
									"orderedQuantity", reserveMissingQuantity
											.min(BigDecimal.ZERO).abs());
							requestForQuotationProduct.setField(
									"productCatalogNumber",
									productCatalogNumber);

							requestForQuotationProductDataDefinition
									.save(requestForQuotationProduct);
						});

		return saved.getId();
	}

	/**
	 * Saving delivery and generating attached products
	 * 
	 * @param view
	 * @param state
	 * @param args
	 */
	private Long generateOfferedProducts(final ViewDefinitionState view,
			final ComponentState state, final String[] args) {
		init();
		LookupComponent lookup = (LookupComponent) view
				.getComponentByReference(L_FORM_SUPPLIER);

		Entity parameter = parameterService.getParameter();
		Entity currency = parameter.getBelongsToField(ParameterFields.CURRENCY);
		Entity supplier = lookup.getEntity();
		String number = numberGeneratorService.generateNumber("deliveries",
				"delivery");
		Entity delivery = deliveryDataDefinition.create();
		delivery.setField("number", number);
		delivery.setField("supplier", supplier);
		delivery.setField("currency", currency);
		Entity saved = deliveryDataDefinition.save(delivery);

		coverageProductSelectedDataDefinition
				.find()
				.list()
				.getEntities()
				.forEach(
						selected -> {
							Entity coverageProduct = selected
									.getBelongsToField(CoverageProductSelectedFields.COVERAGE_PRODUCT);
							Entity product = coverageProduct
									.getBelongsToField(CoverageProductFields.PRODUCT);
							BigDecimal reserveMissingQuantity = coverageProduct
									.getDecimalField(CoverageProductFields.RESERVE_MISSING_QUANTITY);
							Entity productCatalogNumber = productCatalogNumbersService
									.getProductCatalogNumber(product, supplier);

							Entity orderedProduct = orderedProductDataDefinition
									.create();
							orderedProduct.setField("delivery", saved);
							orderedProduct.setField("product", product);
							orderedProduct.setField("orderedQuantity",
									reserveMissingQuantity.min(BigDecimal.ZERO)
											.abs());
							orderedProduct.setField("productCatalogNumber",
									productCatalogNumber);

							orderedProductDataDefinition.save(orderedProduct);
						});

		return saved.getId();
	}

	/**
	 * generateProductsRequestForQuotation handler
	 * 
	 * @param view
	 * @param state
	 * @param args
	 */
	public void generateProductsRequestForQuotation(
			final ViewDefinitionState view, final ComponentState state,
			final String[] args) {
		Long id = generateRequestProducts(view, state, args);

		view.redirectTo("/page/" + OrderSuppliesConstants.PLUGIN_IDENTIFIER
				+ "/requestForQuotationDetails.html?context={\"form.id\":\""
				+ id + "\"}", false, true);

	}

	/**
	 * generateProductsDelivery handler
	 * 
	 * @param view
	 * @param state
	 * @param args
	 */
	public void generateProductsDelivery(final ViewDefinitionState view,
			final ComponentState state, final String[] args) {
		Long id = generateOfferedProducts(view, state, args);

		view.redirectTo("/page/" + OrderSuppliesConstants.PLUGIN_IDENTIFIER
				+ "/deliveryDetails.html?context={\"form.id\":\"" + id + "\"}",
				false, true);

	}

}
