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
package com.qcadoo.mes.productionPerShift.hooks;

import com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields;
import com.qcadoo.mes.lineChangeoverNormsForOrders.LineChangeoverNormsForOrdersService;
import com.qcadoo.mes.lineChangeoverNormsForOrders.constants.OrderFieldsLCNFO;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionCounting.constants.ProductionTrackingFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class ProductionTrackingDetailsHooksPPS {

    private static final String L_CHANGEOVER = "changeover";

    private static final String L_SHOW_CHANGEOVER = "showChangeover";

    private static final String L_CHANGEOVER_DURATION = "changeoverDuration";

    @Autowired
    private LineChangeoverNormsForOrdersService lineChangeoverNormsForOrdersService;

    public void onBeforeRender(final ViewDefinitionState view) {
        updateButtonsState(view);
        setChangeoverTime(view);
    }

    public void updateButtonsState(final ViewDefinitionState view) {
        FormComponent productionTrackingForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
        Ribbon ribbon = window.getRibbon();

        RibbonGroup changeoverRibbonGroup = ribbon.getGroupByName(L_CHANGEOVER);

        RibbonActionItem showChangeoverRibbonActionItem = changeoverRibbonGroup.getItemByName(L_SHOW_CHANGEOVER);

        Long productionTrackingId = productionTrackingForm.getEntityId();

        boolean isSaved = Objects.nonNull(productionTrackingId);

        showChangeoverRibbonActionItem.setEnabled(isSaved);
        showChangeoverRibbonActionItem.requestUpdate(true);
    }

    private void setChangeoverTime(final ViewDefinitionState view) {
        FormComponent productionTrackingForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        FieldComponent changeoverTimeField = (FieldComponent) view.getComponentByReference(L_CHANGEOVER_DURATION);

        Long productionTrackingId = productionTrackingForm.getEntityId();

        Integer duration = null;

        if (Objects.nonNull(productionTrackingId)) {
            Entity productionTracking = productionTrackingForm.getEntity();

            Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);

            if (Objects.nonNull(order)) {
                Entity previousOrder = lineChangeoverNormsForOrdersService.getPreviousOrderFromDB(order);

                if (Objects.nonNull(previousOrder)) {
                    Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);
                    Entity productionLine = order.getBelongsToField(OrderFields.PRODUCTION_LINE);

                    Entity changeOver = lineChangeoverNormsForOrdersService.getChangeover(previousOrder, technology, productionLine);

                    if (Objects.nonNull(changeOver)) {
                        duration = changeOver.getIntegerField(LineChangeoverNormsFields.DURATION);

                        if (order.getBooleanField(OrderFieldsLCNFO.OWN_LINE_CHANGEOVER)) {
                            duration = order.getIntegerField(OrderFieldsLCNFO.OWN_LINE_CHANGEOVER_DURATION);
                        }
                    }
                }
            }
        }

        changeoverTimeField.setFieldValue(duration);
        changeoverTimeField.requestComponentUpdateState();
    }

}
