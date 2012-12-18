/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
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
package com.qcadoo.mes.deliveries.print;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class DeliveryProduct {

    private Long deliveredProductId;

    private Long orderedProductId;

    public Long getDeliveredProductId() {
        return deliveredProductId;
    }

    public void setDeliveredProductId(final Long deliveredProductId) {
        this.deliveredProductId = deliveredProductId;
    }

    public Long getOrderedProductId() {
        return orderedProductId;
    }

    public void setOrderedProductId(final Long orderedProductId) {
        this.orderedProductId = orderedProductId;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final DeliveryProduct other = (DeliveryProduct) obj;
        return new EqualsBuilder().append(deliveredProductId, other.getDeliveredProductId())
                .append(orderedProductId, other.getOrderedProductId()).isEquals();
    }

    @Override
    public int hashCode() {
        if (orderedProductId == null) {
            return new HashCodeBuilder(1, 31).append(deliveredProductId).toHashCode();
        }
        if (deliveredProductId == null) {
            return new HashCodeBuilder(1, 31).append(orderedProductId).toHashCode();
        }
        return new HashCodeBuilder(1, 31).append(orderedProductId).append(deliveredProductId).toHashCode();
    }
}
