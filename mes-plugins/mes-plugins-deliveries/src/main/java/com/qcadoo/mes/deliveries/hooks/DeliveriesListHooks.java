/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 * <p>
 * This file is part of Qcadoo.
 * <p>
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.deliveries.hooks;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.deliveries.DeliveriesService;
import com.qcadoo.mes.deliveries.constants.ParameterFieldsD;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.qcadoo.mes.deliveries.constants.DeliveryFields.RELATED_DELIVERIES;

@Service
public class DeliveriesListHooks {

    private static final String L_DELIVERY = "delivery";

    @Autowired
    private DeliveriesService deliveriesService;

    @Autowired
    private ParameterService parameterService;

    public void fillGridWithRelatedDeliveries(final ViewDefinitionState view) {
        GridComponent deliveriesGrid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);
        FormComponent deliveryForm = (FormComponent) view.getComponentByReference(L_DELIVERY);

        Long deliveryId = deliveryForm.getEntityId();

        if (deliveryId == null) {
            return;
        }

        Entity delivery = deliveriesService.getDelivery(deliveryId);

        deliveriesGrid.setEntities(getRelatedDeliveries(delivery));
    }

    public void disableSendEmailButton(final ViewDefinitionState view) {
        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
        RibbonGroup email = window.getRibbon().getGroupByName("email");
        RibbonActionItem sendEmail = email.getItemByName("sendEmail");
        if (!parameterService.getParameter().getBooleanField(ParameterFieldsD.SEND_EMAIL_TO_SUPPLIER)) {
            sendEmail.setEnabled(false);
            sendEmail.requestUpdate(true);
        }
    }

    private List<Entity> getRelatedDeliveries(final Entity delivery) {
        List<Entity> relatedDeliveries = Lists.newArrayList();

        List<Entity> deliveryRelatedDeliveries = delivery.getHasManyField(RELATED_DELIVERIES);

        for (Entity relatedDelivery : deliveryRelatedDeliveries) {
            relatedDeliveries.add(relatedDelivery);

            relatedDeliveries.addAll(getRelatedDeliveries(relatedDelivery));
        }

        return relatedDeliveries;
    }

}
