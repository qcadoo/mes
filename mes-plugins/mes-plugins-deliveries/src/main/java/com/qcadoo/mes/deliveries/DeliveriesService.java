/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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

import java.math.BigDecimal;
import java.util.List;

import com.qcadoo.mes.deliveries.print.DeliveryProduct;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;

public interface DeliveriesService {

    /**
     * Gets delivery
     * 
     * @param deliveryId
     * 
     * @return delivery
     */
    Entity getDelivery(final Long deliveryId);

    /**
     * Gets ordered product
     * 
     * @param deliveredProductId
     * 
     * @return ordered product
     */
    Entity getOrderedProduct(final Long deliveredProductId);

    /**
     * Gets delivered product
     * 
     * @param deliveredProductId
     * 
     * @return delivered product
     */
    Entity getDeliveredProduct(final Long deliveredProductId);

    /**
     * Gets company product
     * 
     * @param companyProductId
     * 
     * @return company product
     */
    Entity getCompanyProduct(final Long companyProductId);

    /**
     * Gets company products family
     * 
     * @param companyProductsFamilyId
     * 
     * @return company products family
     */
    Entity getCompanyProductsFamily(final Long companyProductsFamilyId);

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
     * Gets company products family data definition
     * 
     * @return company products family data definition
     */
    DataDefinition getCompanyProductsFamilyDD();

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
    DataDefinition getColumnForOrdersDD();

    /**
     * Fills unit fields
     * 
     * @param view
     *            view
     * 
     * @param productName
     *            product lookup reference name
     * 
     * @param referenceNames
     *            reference names to unit fields
     */
    void fillUnitFields(final ViewDefinitionState view, final String productName, final List<String> referenceNames);

    /**
     * Gets delivery address default value
     * 
     * @return delivery adresss
     * 
     */
    String getDeliveryAddressDefaultValue();

    /**
     * Gets description default value
     * 
     * @return description
     * 
     */
    String getDescriptionDefaultValue();

    /**
     * Gets product for given delivery product
     * 
     * @return deliveryProduct
     */
    Entity getProduct(final DeliveryProduct deliveryProduct);

    /**
     * 
     * @param entity
     */
    void calculatePricePerUnit(final Entity entity, final String quantityFieldName);

    /**
     * Fills currency fields
     * 
     * @param view
     *            view
     * 
     * @param referenceNames
     *            reference names to unit fields
     */
    void fillCurrencyFields(final ViewDefinitionState view, final List<String> referenceNames);

    /**
     * Gets cost per unit for given parameter
     * 
     * @param entityDD
     *            data definitions
     * 
     * @param entity
     *            entity
     * 
     * @param entityName
     *            entity name
     * 
     * @param product
     *            product
     * 
     * @return pricePerUnit
     */
    BigDecimal getPricePerUnit(final DataDefinition entityDD, final Entity entity, final String entityName, final Entity product);

    /**
     * Filters currency column
     * 
     * @param columns
     *            columnsForOrders, columnsForDeliveries
     * 
     * @return list of filtered columns
     */
    List<Entity> getColumnsWithFilteredCurrencies(final List<Entity> columns);

}
