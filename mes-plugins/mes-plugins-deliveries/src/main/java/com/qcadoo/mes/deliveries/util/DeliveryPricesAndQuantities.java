/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
package com.qcadoo.mes.deliveries.util;

import java.math.BigDecimal;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.google.common.base.Preconditions;
import com.qcadoo.mes.deliveries.constants.DeliveredProductFields;
import com.qcadoo.mes.deliveries.constants.DeliveriesConstants;
import com.qcadoo.mes.deliveries.constants.OrderedProductFields;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchQueryBuilder;

public class DeliveryPricesAndQuantities {

    private static final String L_SUM_RESULT = "sumResult";

    private static final String SUM_QUERY_TEMPLATE = "SELECT '' as nullResultProtector, SUM(m.%s) as %s FROM #%s_%s as m where m.%s.id = :deliveryId";

    private static final String QUERY_FOR_QUANTITY_ORDERED_PRODUCT = fillSumQueryTemplate(OrderedProductFields.ORDERED_QUANTITY,
            DeliveriesConstants.MODEL_ORDERED_PRODUCT, OrderedProductFields.DELIVERY);

    private static final String QUERY_FOR_QUANTITY_DELIVERED_PRODUCT = fillSumQueryTemplate(
            DeliveredProductFields.DELIVERED_QUANTITY, DeliveriesConstants.MODEL_DELIVERED_PRODUCT,
            DeliveredProductFields.DELIVERY);

    private static final String QUERY_FOR_TOTAL_PRICE_ORDERED_PRODUCT = fillSumQueryTemplate(OrderedProductFields.TOTAL_PRICE,
            DeliveriesConstants.MODEL_ORDERED_PRODUCT, OrderedProductFields.DELIVERY);

    private static final String QUERY_FOR_TOTAL_PRICE_DELIVERED_PRODUCT = fillSumQueryTemplate(
            DeliveredProductFields.TOTAL_PRICE, DeliveriesConstants.MODEL_DELIVERED_PRODUCT, DeliveredProductFields.DELIVERY);

    private final Entity delivery;

    private final transient NumberService numberService;

    private final BigDecimal deliveredProductsQuantity;

    private final BigDecimal deliveredProductsTotalPrice;

    private final BigDecimal orderedProductsQuantity;

    private final BigDecimal orderedProductsTotalPrice;

    public DeliveryPricesAndQuantities(final Entity delivery, final NumberService numberService) {
        Preconditions.checkNotNull(delivery);
        Preconditions.checkNotNull(numberService);

        this.delivery = delivery;
        this.numberService = numberService;
        this.deliveredProductsQuantity = executeQueryWithDelivery(QUERY_FOR_QUANTITY_DELIVERED_PRODUCT);
        this.deliveredProductsTotalPrice = executeQueryWithDelivery(QUERY_FOR_TOTAL_PRICE_DELIVERED_PRODUCT);
        this.orderedProductsQuantity = executeQueryWithDelivery(QUERY_FOR_QUANTITY_ORDERED_PRODUCT);
        this.orderedProductsTotalPrice = executeQueryWithDelivery(QUERY_FOR_TOTAL_PRICE_ORDERED_PRODUCT);
    }

    private BigDecimal extractSumValueFromResultEntity(final Entity dynamicEntity) {
        BigDecimal sum = dynamicEntity.getDecimalField(L_SUM_RESULT);
        return numberService.setScale(BigDecimalUtils.convertNullToZero(sum));
    }

    private BigDecimal executeQueryWithDelivery(final String query) {
        Entity resultEntity = createQueryBuilder(query).uniqueResult();
        return extractSumValueFromResultEntity(resultEntity);
    }

    private SearchQueryBuilder createQueryBuilder(final String queryStr) {
        return delivery.getDataDefinition().find(queryStr).setLong("deliveryId", delivery.getId()).setMaxResults(1);
    }

    private static String fillSumQueryTemplate(final String nameOfFieldToSum, final String modelName,
            final String deliveryFieldName) {
        return String.format(SUM_QUERY_TEMPLATE, nameOfFieldToSum, L_SUM_RESULT, DeliveriesConstants.PLUGIN_IDENTIFIER,
                modelName, deliveryFieldName);
    }

    public BigDecimal getDeliveredCumulatedQuantity() {
        return deliveredProductsQuantity;
    }

    public BigDecimal getDeliveredTotalPrice() {
        return deliveredProductsTotalPrice;
    }

    public BigDecimal getOrderedCumulatedQuantity() {
        return orderedProductsQuantity;
    }

    public BigDecimal getOrderedTotalPrice() {
        return orderedProductsTotalPrice;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(1, 31).append(delivery.getId()).append(deliveredProductsQuantity)
                .append(deliveredProductsTotalPrice).append(orderedProductsQuantity).append(orderedProductsTotalPrice)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DeliveryPricesAndQuantities rhs = (DeliveryPricesAndQuantities) obj;
        return new EqualsBuilder().append(delivery.getId(), rhs.delivery.getId())
                .append(deliveredProductsQuantity, rhs.deliveredProductsQuantity)
                .append(deliveredProductsTotalPrice, rhs.deliveredProductsTotalPrice)
                .append(orderedProductsQuantity, rhs.orderedProductsQuantity)
                .append(orderedProductsTotalPrice, rhs.orderedProductsTotalPrice).isEquals();
    }

}
