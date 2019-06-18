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
package com.qcadoo.mes.advancedGenealogyForOrders.hooks;

import static com.qcadoo.mes.advancedGenealogyForOrders.AdvancedGenealogyForOrdersService.isTrackingRecordForOrder;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.advancedGenealogy.constants.AdvancedGenealogyConstants;
import com.qcadoo.mes.advancedGenealogyForOrders.AdvancedGenealogyForOrdersService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class TrackingForOrdersModelHooks {

    @Autowired
    private AdvancedGenealogyForOrdersService advancedGenealogyForOrdersService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    public final void addProductInComponents(final DataDefinition trackingRecordDD, final Entity trackingRecord) {
        if (!isTrackingRecordForOrder(trackingRecord) || !belongsToOrder(trackingRecord)
                || hasAnyProductInComponent(trackingRecord)) {
            return;
        }

        Entity order = trackingRecord.getBelongsToField("order");
        List<Entity> genealogyProductInComponents = advancedGenealogyForOrdersService.buildProductInComponentList(order);
        trackingRecord.setField("genealogyProductInComponents", genealogyProductInComponents);
    }

    @SuppressWarnings("unchecked")
    private boolean hasAnyProductInComponent(final Entity trackingRecord) {
        if (!(trackingRecord.getField("genealogyProductInComponents") instanceof List)) {
            return false;
        }

        return !((List<Entity>) trackingRecord.getField("genealogyProductInComponents")).isEmpty();
    }

    private boolean belongsToOrder(final Entity trackingRecord) {
        return trackingRecord.getBelongsToField("order") != null;
    }

    public final void setCreationDateAndWorker(final DataDefinition genealogyProductInBatchDD,
            final Entity genealogyProductInBatch) {
        genealogyProductInBatch.setField("worker", securityService.getCurrentUserName());
        genealogyProductInBatch.setField("dateAndTime", new Date());
    }

    public void generateOrderNumber(final ViewDefinitionState state) {
        numberGeneratorService.generateAndInsertNumber(state, AdvancedGenealogyConstants.PLUGIN_IDENTIFIER,
                AdvancedGenealogyConstants.MODEL_TRACKING_RECORD, AdvancedGenealogyConstants.FIELD_FORM,
                AdvancedGenealogyConstants.FIELD_NUMBER);
    }

}
