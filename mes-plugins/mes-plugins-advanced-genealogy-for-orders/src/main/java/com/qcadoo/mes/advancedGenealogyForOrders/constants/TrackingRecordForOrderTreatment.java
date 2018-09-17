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
package com.qcadoo.mes.advancedGenealogyForOrders.constants;

public enum TrackingRecordForOrderTreatment {

    DURING_PRODUCTION("01duringProduction"), UNCHANGABLE_PLAN_AFTER_ORDER_ACCEPT("02unchangablePlanAfterOrderAccept"), UNCHANGABLE_PLAN_AFTER_ORDER_START(
            "03unchangablePlanAfterOrderStart");

    private String stringValue;

    private TrackingRecordForOrderTreatment(final String stringValue) {
        this.stringValue = stringValue;
    }

    public String getStringValue() {
        return stringValue;
    }

    public static TrackingRecordForOrderTreatment parseString(final String string) {
        if ("01duringProduction".equals(string)) {
            return DURING_PRODUCTION;
        } else if ("02unchangablePlanAfterOrderAccept".equals(string)) {
            return UNCHANGABLE_PLAN_AFTER_ORDER_ACCEPT;
        } else if ("03unchangablePlanAfterOrderStart".equals(string)) {
            return UNCHANGABLE_PLAN_AFTER_ORDER_START;
        } else {
            throw new IllegalStateException("Couldn't parse TrackingRecordForOrderTreatment from string");
        }
    }
}
