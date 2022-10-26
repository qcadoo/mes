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
package com.qcadoo.mes.masterOrders.constants;

public final class MasterOrdersConstants {

    private MasterOrdersConstants() {

    }

    public static final String[] FILE_EXTENSIONS = new String[] { "bmp", "gif", "jpg", "jpeg", "png", "tiff", "wmf", "eps" };

    public static final String PLUGIN_IDENTIFIER = "masterOrders";

    public static final String MODEL_MASTER_ORDER = "masterOrder";

    public static final String MODEL_MASTER_ORDER_DEFINITION = "masterOrderDefinition";

    public static final String MODEL_MASTER_ORDER_PRODUCT = "masterOrderProduct";

    public static final String MODEL_MASTER_ORDER_POSITION_DTO = "masterOrderPositionDto";

    public static final String MASTER_ORDER_PRODUCT_ATTR_VALUE = "masterOrderProductAttrValue";

    public static final String MODEL_MASTER_ORDER_DTO = "masterOrderDto";

    public static final String GENERATING_ORDERS_HELPER = "generatingOrdersHelper";

    public static final String MODEL_SALES_PLAN_PRODUCT = "salesPlanProduct";

    public static final String MODEL_SALES_PLAN_PRODUCT_DTO = "salesPlanProductDto";

    public static final String MODEL_SALES_PLAN_STATE_CHANGE = "salesPlanStateChange";

    public static final String MODEL_PRODUCTS_BY_SIZE_HELPER = "productsBySizeHelper";

    public static final String MODEL_PRODUCTS_BY_SIZE_ENTRY_HELPER = "productsBySizeEntryHelper";

    public static final String MODEL_PRODUCTS_BY_ATTRIBUTE_HELPER = "productsByAttributeHelper";

    public static final String MODEL_PRODUCTS_BY_ATTRIBUTE_ENTRY_HELPER = "productsByAttributeEntryHelper";

    public static final String MODEL_SALES_PLAN = "salesPlan";

    public static final String MODEL_SALES_PLAN_MATERIAL_REQUIREMENT = "salesPlanMaterialRequirement";

    public static final String MODEL_SALES_PLAN_MATERIAL_REQUIREMENT_PRODUCT = "salesPlanMaterialRequirementProduct";

    public static final String MODEL_SALES_PLAN_ORDERS_GROUP_HELPER = "salesPlanOrdersGroupHelper";

    public static final String MODEL_SALES_PLAN_ORDERS_GROUP_ENTRY_HELPER = "salesPlanOrdersGroupEntryHelper";

    public static final String MODEL_MASTER_ORDERS_MATERIAL_REQUIREMENT = "masterOrdersMaterialRequirement";

    public static final String MODEL_MASTER_ORDERS_MATERIAL_REQUIREMENT_PRODUCT = "masterOrdersMaterialRequirementProduct";

    public static final String MODEL_ORDERS_GENERATION_HELPER = "ordersGenerationHelper";

    public static final String MODEL_OUTSOURCE_PROCESSING_COMPONENT_HELPER = "outsourceProcessingComponentHelper";

    public static final String MODEL_SALES_VOLUME = "salesVolume";

    public static final String MODEL_SALES_VOLUME_MULTI = "salesVolumeMulti";

    public static final String MODEL_ORDERED_PRODUCT_CONFIGURATOR = "orderedProductConfigurator";

    public static final String MODEL_ORDERED_PRODUCT_CONFIGURATOR_ATTRIBUTE = "orderedProductConfiguratorAttribute";

    public static String masterOrderDetailsUrl(final Long id) {
        return "#page/" + PLUGIN_IDENTIFIER + "/" + MODEL_MASTER_ORDER + "Details.html?context=%7B%22form.id%22%3A%22" + id
                + "%22%2C%22form.undefined%22%3Anull%7D";
    }

}
