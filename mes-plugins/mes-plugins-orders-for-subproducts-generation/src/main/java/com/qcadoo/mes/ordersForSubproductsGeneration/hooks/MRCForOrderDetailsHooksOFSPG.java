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
package com.qcadoo.mes.ordersForSubproductsGeneration.hooks;

import com.qcadoo.mes.materialRequirementCoverageForOrder.constans.CoverageForOrderFields;
import com.qcadoo.mes.orderSupplies.constants.MaterialRequirementCoverageFields;
import com.qcadoo.mes.ordersForSubproductsGeneration.OrdersForSubproductsGenerationService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MRCForOrderDetailsHooksOFSPG {

    private static final Logger LOG = LoggerFactory.getLogger(MRCForOrderDetailsHooksOFSPG.class);

    private static final String L_FORM = "form";

    private static final String L_WINDOW = "window";

    public static final String ORDERS_RIBBON_GROUP = "orders";

    public static final String ORDERS_RIBBON_GROUP_GENERATE_ORDERS = "generateOrders";

    public static final String GENERATED_ORDERS_GRID = "generatedOrders";

    @Autowired
    private OrdersForSubproductsGenerationService ordersForSubproductsGenerationService;

    public final void onBeforeRender(final ViewDefinitionState view) {
        updateRibbonState(view);
    }

    public void updateRibbonState(final ViewDefinitionState view) {
        FieldComponent generatedField = (FieldComponent) view.getComponentByReference(CoverageForOrderFields.GENERATED);
        boolean isEnabled = "1".equals(generatedField.getFieldValue());

        WindowComponent window = (WindowComponent) view.getComponentByReference(L_WINDOW);
        RibbonGroup coverage = (RibbonGroup) window.getRibbon().getGroupByName(ORDERS_RIBBON_GROUP);

        RibbonActionItem generateOrders = (RibbonActionItem) coverage.getItemByName(ORDERS_RIBBON_GROUP_GENERATE_ORDERS);
        generateOrders.setMessage("ordersForSubproductsGeneration.ordersForSubproducts.generateFromCoverage.msg");
        FormComponent coverageForm = (FormComponent) view.getComponentByReference(L_FORM);

        Long coverageId = coverageForm.getEntityId();
        Entity coverageEntity = coverageForm.getPersistedEntityWithIncludedFormValues();
        Entity order = coverageEntity.getBelongsToField(CoverageForOrderFields.ORDER);
        List<Entity> coverageOrders = coverageEntity.getHasManyField(MaterialRequirementCoverageFields.COVERAGE_ORDERS);

        if (coverageId != null) {
            if (ordersForSubproductsGenerationService.hasSubOrders(order)
                    || ordersForSubproductsGenerationService.hasSubOrders(getOrdersIds(coverageEntity))) {
                isEnabled = false;
            }
        }

        if (order == null && coverageOrders.isEmpty()) {
            isEnabled = false;
        }
        updateButtonState(generateOrders, isEnabled);
    }

    private void updateButtonState(final RibbonActionItem ribbonActionItem, final boolean isEnabled) {
        ribbonActionItem.setEnabled(isEnabled);
        ribbonActionItem.requestUpdate(true);
    }

    private List<Long> getOrdersIds(final Entity coverage) {
        List<Entity> coverageOrders = coverage.getHasManyField(MaterialRequirementCoverageFields.COVERAGE_ORDERS);
        List<Long> ids = coverageOrders.stream().map(co -> co.getId())
                .collect(Collectors.toList());
        return ids;
    }

}
