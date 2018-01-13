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
package com.qcadoo.mes.productionCounting.hooks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionCounting.SetTrackingOperationProductsComponentsService;
import com.qcadoo.mes.productionCounting.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductOutComponentDtoFields;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductOutComponentFields;
import com.qcadoo.mes.productionCounting.listeners.TrackingOperationProductComponentDetailsListeners;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class TrackingOperationProductOutComponentDetailsHooks {

    private static final String L_FORM = "form";

    private static final String L_SET_TAB = "setTab";

    @Autowired
    private TrackingOperationProductComponentDetailsListeners trackingOperationProductComponentDetailsListeners;

    @Autowired
    private SetTrackingOperationProductsComponentsService setTrackingOperationProductsComponents;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void onBeforeRender(final ViewDefinitionState view) {
        trackingOperationProductComponentDetailsListeners.onBeforeRender(view);

        FormComponent trackingOperationProductOutComponentForm = (FormComponent) view.getComponentByReference(L_FORM);

        Entity trackingOperationProductOutComponent = trackingOperationProductOutComponentForm
                .getPersistedEntityWithIncludedFormValues();

        hideOrShowSetTab(view, trackingOperationProductOutComponent);

        Entity trackingOperationProductInComponentDto = dataDefinitionService
                .get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                        ProductionCountingConstants.MODEL_TRACKING_OPERATION_PRODUCT_OUT_COMPONENT_DTO)
                .get(trackingOperationProductOutComponentForm.getEntityId());
        FieldComponent plannedQuantity = (FieldComponent) view
                .getComponentByReference(TrackingOperationProductOutComponentDtoFields.PLANNED_QUANTITY);
        plannedQuantity.setFieldValue(trackingOperationProductInComponentDto
                .getDecimalField(TrackingOperationProductOutComponentDtoFields.PLANNED_QUANTITY));
        FieldComponent remainingQuantity = (FieldComponent) view
                .getComponentByReference(TrackingOperationProductOutComponentDtoFields.REMAINING_QUANTITY);
        remainingQuantity.setFieldValue(trackingOperationProductInComponentDto
                .getDecimalField(TrackingOperationProductOutComponentDtoFields.REMAINING_QUANTITY));
        FieldComponent producedSum = (FieldComponent) view
                .getComponentByReference(TrackingOperationProductOutComponentDtoFields.PRODUCED_SUM);
        producedSum.setFieldValue(trackingOperationProductInComponentDto
                .getDecimalField(TrackingOperationProductOutComponentDtoFields.PRODUCED_SUM));
        FieldComponent wastesSum = (FieldComponent) view
                .getComponentByReference(TrackingOperationProductOutComponentDtoFields.WASTES_SUM);
        wastesSum.setFieldValue(trackingOperationProductInComponentDto
                .getDecimalField(TrackingOperationProductOutComponentDtoFields.WASTES_SUM));
    }

    private void hideOrShowSetTab(final ViewDefinitionState view, final Entity trackingOperationProductOutComponent) {
        view.getComponentByReference(L_SET_TAB)
                .setVisible(setTrackingOperationProductsComponents.isSet(
                        trackingOperationProductOutComponent
                                .getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCTION_TRACKING),
                        trackingOperationProductOutComponent));
    }

}
