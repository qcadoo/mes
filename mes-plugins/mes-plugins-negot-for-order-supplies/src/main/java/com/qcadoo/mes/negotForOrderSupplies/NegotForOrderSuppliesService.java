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
package com.qcadoo.mes.negotForOrderSupplies;

import java.math.BigDecimal;

import com.qcadoo.model.api.Entity;

public interface NegotForOrderSuppliesService {

    /**
     * Creates negotiation for given material requirement coverage
     * 
     * @param materialRequirementCoverage
     *            material requirement coverage
     * 
     * @return negotiation
     */
    Entity createNegotiation(final Entity materialRequirementCoverage);

    /**
     * Creates negotiation product for given coverage product
     * 
     * @param coverageProduct
     * 
     * @return negotiation product
     */
    Entity createNegotiationProduct(final Entity coverageProduct);

    /**
     * Gets needed quantity for given coverage product
     * 
     * @param coverageProduct
     * 
     * @return needed quantity
     */
    BigDecimal getNeededQuantity(final Entity coverageProduct);

}
