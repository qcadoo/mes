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
package com.qcadoo.mes.productionPerShift.factory;

import com.qcadoo.mes.productionPerShift.domain.*;
import com.qcadoo.model.api.Entity;
import org.joda.time.LocalDate;

import java.math.BigDecimal;

public class ProductionProgressDTOFactory {

    public static final String ORDER_ID_ALIAS = "orderId";

    public static final String ORDER_NUMBER_ALIAS = "orderNumber";

    public static final String PRODUCT_ID_ALIAS = "productId";

    public static final String PRODUCT_NUMBER_ALIAS = "productNumber";

    public static final String PRODUCT_UNIT_ALIAS = "productUnit";

    public static final String SHIFT_ID_ALIAS = "shiftId";

    public static final String SHIFT_NAME_ALIAS = "shiftName";

    public static final String SHIFT_START_DAY_ALIAS = "shiftStartDay";

    public static final String QUANTITY_ALIAS = "quantity";

    private ProductionProgressDTOFactory() {
    }

    private static Order orderFrom(final Entity projection) {
        Long orderId = (Long) projection.getField(ORDER_ID_ALIAS);
        String orderNumber = projection.getStringField(ORDER_NUMBER_ALIAS);
        return new Order(orderId, orderNumber);
    }

    private static Shift shiftFrom(final Entity projection) {
        Long shiftId = (Long) projection.getField(SHIFT_ID_ALIAS);
        String shiftName = projection.getStringField(SHIFT_NAME_ALIAS);
        return new Shift(shiftId, shiftName);
    }

    private static Product productFrom(final Entity projection) {
        Long productId = (Long) projection.getField(PRODUCT_ID_ALIAS);
        String productNumber = projection.getStringField(PRODUCT_NUMBER_ALIAS);
        String productUnit = projection.getStringField(PRODUCT_UNIT_ALIAS);
        return new Product(productId, productNumber, productUnit);
    }

    private static ProductionProgressScope scopeFrom(final Entity projection) {
        LocalDate day = LocalDate.fromDateFields(projection.getDateField(SHIFT_START_DAY_ALIAS));
        return new ProductionProgressScope(day, orderFrom(projection), shiftFrom(projection),
                productFrom(projection));
    }

    public static ProductionProgress from(final Entity projection) {
        BigDecimal quantity = projection.getDecimalField(QUANTITY_ALIAS);
        ProductionProgressScope scope = scopeFrom(projection);
        return new ProductionProgress(scope, quantity);
    }

}
