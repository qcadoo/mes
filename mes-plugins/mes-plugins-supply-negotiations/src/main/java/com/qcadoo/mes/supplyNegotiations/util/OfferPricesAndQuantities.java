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
package com.qcadoo.mes.supplyNegotiations.util;

import java.math.BigDecimal;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Preconditions;
import com.qcadoo.mes.supplyNegotiations.constants.OfferProductFields;
import com.qcadoo.mes.supplyNegotiations.constants.SupplyNegotiationsConstants;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchQueryBuilder;

public class OfferPricesAndQuantities {

    private static final String L_SUM_QUANTITY = "sumQuantity";

    private static final String L_SUM_PRICE = "sumPrice";

    private static final String QUERY_TEMPLATE = "SELECT '' as nullResultProtector, SUM(op.%s) AS %s, SUM(op.%s) AS %s "
            + " FROM #%s_%s as op where op.%s.id = :offerId";

    private static final String QUERY = String.format(QUERY_TEMPLATE, OfferProductFields.QUANTITY, L_SUM_QUANTITY,
            OfferProductFields.TOTAL_PRICE, L_SUM_PRICE, SupplyNegotiationsConstants.PLUGIN_IDENTIFIER,
            SupplyNegotiationsConstants.MODEL_OFFER_PRODUCT, OfferProductFields.OFFER);

    private final transient NumberService numberService;

    private final Long offerId;

    private final BigDecimal offerCumulatedQuantity;

    private final BigDecimal offerTotalPrice;

    private Integer hashCode = null;

    public OfferPricesAndQuantities(final Entity offer, final NumberService numberService) {
        Preconditions.checkNotNull(offer);
        Preconditions.checkNotNull(numberService);

        this.numberService = numberService;
        this.offerId = offer.getId();

        Entity resultEntity = createQueryBuilder(offer).uniqueResult();
        this.offerCumulatedQuantity = extractDecimalValue(L_SUM_QUANTITY, resultEntity);
        this.offerTotalPrice = extractDecimalValue(L_SUM_PRICE, resultEntity);

    }

    private SearchQueryBuilder createQueryBuilder(final Entity offer) {
        return offer.getDataDefinition().find(QUERY).setLong("offerId", offer.getId()).setMaxResults(1);
    }

    private BigDecimal extractDecimalValue(final String fieldName, final Entity dynamicEntity) {
        BigDecimal sum = dynamicEntity.getDecimalField(fieldName);
        return numberService.setScaleWithDefaultMathContext(BigDecimalUtils.convertNullToZero(sum));
    }

    public BigDecimal getOfferCumulatedQuantity() {
        return offerCumulatedQuantity;
    }

    public BigDecimal getOfferTotalPrice() {
        return offerTotalPrice;
    }

    @Override
    public int hashCode() {
        if (this.hashCode == null) {
            this.hashCode = new HashCodeBuilder(1, 31).append(offerId).append(offerCumulatedQuantity).append(offerTotalPrice)
                    .toHashCode();
        }
        return this.hashCode;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        OfferPricesAndQuantities rhs = (OfferPricesAndQuantities) obj;
        return new EqualsBuilder().append(offerId, rhs.offerId).append(offerCumulatedQuantity, rhs.offerCumulatedQuantity)
                .append(offerTotalPrice, rhs.offerTotalPrice).isEquals();
    }

}
