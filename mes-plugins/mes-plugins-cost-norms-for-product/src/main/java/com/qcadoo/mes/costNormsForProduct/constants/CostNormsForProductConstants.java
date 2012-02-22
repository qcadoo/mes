/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.3
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
package com.qcadoo.mes.costNormsForProduct.constants;

import java.util.Set;

import com.google.common.collect.Sets;

public interface CostNormsForProductConstants {

    String PLUGIN_IDENTIFIER = "costNormsForProduct";

    // MODEL
    String MODEL_ORDER_OPERATION_PRODUCT_IN_COMPONENT = "orderOperationProductInComponent";

    String ORDER_OPERATION_PRODUCT_IN_COMPONENTS = "orderOperationProductInComponents";

    Set<String> CURRENCY_FIELDS_PRODUCT = Sets.newHashSet("nominalCostCurrency", "lastPurchaseCostCurrency",
            "averageCostCurrency");

    Set<String> CURRENCY_FIELDS_ORDER = Sets.newHashSet("nominalCostCurrency", "lastPurchaseCostCurrency", "averageCostCurrency",
            "costForOrderCurrency");
}
