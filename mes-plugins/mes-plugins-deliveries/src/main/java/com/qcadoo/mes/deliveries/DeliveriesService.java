/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
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
package com.qcadoo.mes.deliveries;

import com.qcadoo.mes.deliveries.print.DeliveryProduct;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.GridComponent;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public interface DeliveriesService {

    /**
     * Gets delivery
     *
     * @param deliveryId
     * @return delivery
     */
    Entity getDelivery(final Long deliveryId);

    /**
     * Gets ordered product
     *
     * @param orderedProductId
     * @return ordered product
     */
    Entity getOrderedProduct(final Long orderedProductId);

    /**
     * Gets delivered product
     *
     * @param deliveredProductId
     * @return delivered product
     */
    Entity getDeliveredProduct(final Long deliveredProductId);

    /**
     * Gets company product
     *
     * @param companyProductId
     * @return company product
     */
    Entity getCompanyProduct(final Long companyProductId);

    /**
     * Gets list of columns for deliveries
     *
     * @return list of columns for deliveries
     */
    List<Entity> getColumnsForDeliveries();

    /**
     * Gets list of columns for orders
     *
     * @return list of columns for orders
     */
    List<Entity> getColumnsForOrders();

    /**
     * Gets delivery data definition
     *
     * @return delivery data definition
     */
    DataDefinition getDeliveryDD();

    /**
     * Gets ordered product data definition
     *
     * @return ordered product data definition
     */
    DataDefinition getOrderedProductDD();

    /**
     * Gets delivered product data definition
     *
     * @return delivered product data definition
     */
    DataDefinition getDeliveredProductDD();

    /**
     * Gets company product data definition
     *
     * @return company product data definition
     */
    DataDefinition getCompanyProductDD();

    /**
     * Gets column for deliveries data definition
     *
     * @return column for deliveries data definition
     */
    DataDefinition getColumnForDeliveriesDD();

    /**
     * Gets columns for orders data definition
     *
     * @return column for orders data definition
     */
    DataDefinition getParameterDeliveryOrderColumnDD();

    /**
     * Gets product for given delivery product
     *
     * @return deliveryProduct
     */
    Entity getProduct(final DeliveryProduct deliveryProduct);

    /**
     * Gets description default value
     *
     * @return description
     */
    String getDescriptionDefaultValue();

    /**
     * Gets delivery address default value
     *
     * @return delivery adresss
     */
    String getDeliveryAddressDefaultValue();

    /**
     * Gets address from company
     *
     * @param company company
     * @return adresss
     */
    String generateAddressFromCompany(final Entity company);

    /**
     * Fills unit fields
     *
     * @param view           view
     * @param productName    product lookup reference name
     * @param referenceNames reference names to unit fields
     */
    void fillUnitFields(final ViewDefinitionState view, final String productName, final List<String> referenceNames);

    /**
     * Fills unit fields
     *
     * @param view                view
     * @param product             product entity
     * @param referenceNames      reference names to unit fields
     * @param additionalUnitNames
     */
    void fillUnitFields(final ViewDefinitionState view, final Entity product, final List<String> referenceNames,
                        final List<String> additionalUnitNames);

    /**
     * Fills unit fields
     *
     * @param view                view
     * @param productName         product lookup reference name
     * @param referenceNames      reference names to unit fields
     * @param additionalUnitNames reference names to additional unit fields
     */
    void fillUnitFields(final ViewDefinitionState view, final String productName, final List<String> referenceNames,
                        final List<String> additionalUnitNames);

    /**
     * Fills currency fields
     *
     * @param view           view
     * @param referenceNames reference names to unit fields
     */
    void fillCurrencyFields(final ViewDefinitionState view, final List<String> referenceNames);

    /**
     * Fills currency fields
     *
     * @param view           view
     * @param referenceNames reference names to unit fields
     */
    void fillCurrencyFieldsForDelivery(final ViewDefinitionState view, final List<String> referenceNames,
                                       final Entity delivery);

    /**
     * Gets currency for delivery
     *
     * @param delivery delivery entity
     * @return selected or default currency
     */
    String getCurrency(final Entity delivery);

    /**
     * Recalculate price form total price
     *
     * @param view                   view
     * @param quantityFieldReference quantity field reference
     */
    void recalculatePriceFromTotalPrice(final ViewDefinitionState view, final String quantityFieldReference);

    /**
     * Recalculate price from price per unit
     *
     * @param view                   view
     * @param quantityFieldReference quantity field reference
     */
    void recalculatePriceFromPricePerUnit(final ViewDefinitionState view, final String quantityFieldReference);

    /**
     * Recalculate price
     *
     * @param view                   view
     * @param quantityFieldReference quantity field reference
     */
    void recalculatePrice(final ViewDefinitionState view, final String quantityFieldReference);

    /**
     * Gets big decimal from field
     *
     * @param fieldComponent field component
     * @param locale         locale
     * @return BigDecimal
     */
    BigDecimal getBigDecimalFromField(final FieldComponent fieldComponent, final Locale locale);

    /**
     * Calculate price per unit
     *
     * @param entity            entity
     * @param quantityFieldName quantity field name
     */
    void calculatePricePerUnit(final Entity entity, final String quantityFieldName);

    /**
     * Filters currency column
     *
     * @param columns columnsForOrders, columnsForDeliveries
     * @return list of filtered columns
     */
    List<Entity> getColumnsWithFilteredCurrencies(final List<Entity> columns);

    /**
     * Disables show product button
     *
     * @param view
     */
    void disableShowProductButton(final ViewDefinitionState view);

    Optional<Entity> getDefaultSupplier(final Long productId);

    Optional<Entity> getDefaultSupplierWithIntegration(final Long productId);

    List<Entity> getSuppliersWithIntegration(final Long productId);

    List<Entity> getCompanyProducts(final Set<Long> productIds);

    Optional<Entity> getCompanyProduct(final List<Entity> companyProducts, final Long productId);

    List<Entity> getSelectedOrderedProducts(final GridComponent orderedProductsGrid);

    Optional<Entity> getOrderedProductForDeliveredProduct(final Entity deliveredProduct);

    Optional<Entity> getSuitableOrderedProductForDeliveredProduct(final Entity deliveredProduct);

    Optional<Entity> getOrderedProductForDeliveredProduct(final Entity deliveredProduct,
                                                          final SearchCriterion batchCustomSearchCriterion,
                                                          final SearchCriterion offerCustomSearchCriterion,
                                                          final SearchCriterion operationCustomSearchCriterion);

    SearchCriteriaBuilder getSearchCriteriaBuilderForOrderedProduct(final SearchCriteriaBuilder searchCriteriaBuilder,
                                                                    final Entity deliveredProduct);

    SearchCriteriaBuilder getSearchCriteriaBuilderForOrderedProduct(final SearchCriteriaBuilder searchCriteriaBuilder,
                                                                    final Entity deliveredProduct,
                                                                    final SearchCriterion batchCustomSearchCriterion,
                                                                    final SearchCriterion offerCustomSearchCriterion,
                                                                    final SearchCriterion operationCustomSearchCriterion);

    SearchCriteriaBuilder getSearchCriteriaBuilderForDeliveredProduct(final SearchCriteriaBuilder searchCriteriaBuilder,
                                                                      final Entity deliveredProduct);

    SearchCriteriaBuilder getSearchCriteriaBuilderForDeliveredProduct(final SearchCriteriaBuilder searchCriteriaBuilder,
                                                                      final Entity deliveredProduct,
                                                                      final boolean checkOther,
                                                                      final SearchCriterion batchCustomSearchCriterion,
                                                                      final SearchCriterion offerCustomSearchCriterion,
                                                                      final SearchCriterion operationCustomSearchCriterion);

    SearchCriterion getBatchCustomSearchCriterion(final Entity deliveredProduct);

    SearchCriterion getOfferCustomSearchCriterion(final Entity deliveredProduct);

    SearchCriterion getOperationCustomSearchCriterion(final Entity deliveredProduct);

    BigDecimal getConversion(final Entity product, String unit, String additionalUnit, BigDecimal dbConversion);

}
