/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
package com.qcadoo.mes.deliveriesToMaterialFlow.states;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.deliveries.constants.DeliveredProductFields;
import com.qcadoo.mes.deliveries.constants.DeliveryFields;
import com.qcadoo.mes.deliveriesToMaterialFlow.constants.DeliveryFieldsDTMF;
import com.qcadoo.mes.deliveriesToMaterialFlow.constants.TransferFieldsDTMF;
import com.qcadoo.mes.materialFlow.MaterialFlowService;
import com.qcadoo.mes.materialFlow.constants.LocationFields;
import com.qcadoo.mes.materialFlow.constants.MaterialFlowConstants;
import com.qcadoo.mes.materialFlow.constants.TransferFields;
import com.qcadoo.mes.materialFlow.constants.TransferType;
import com.qcadoo.mes.materialFlowDocuments.constants.DocumentFields;
import com.qcadoo.mes.materialFlowDocuments.constants.DocumentState;
import com.qcadoo.mes.materialFlowDocuments.constants.DocumentType;
import com.qcadoo.mes.materialFlowDocuments.constants.MaterialFlowDocumentsConstants;
import com.qcadoo.mes.materialFlowDocuments.constants.PositionFields;
import com.qcadoo.mes.materialFlowDocuments.service.ResourceManagementService;
import com.qcadoo.mes.materialFlowResources.constants.TransferFieldsMFR;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.security.api.UserService;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class DeliveryStateServiceMF {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private MaterialFlowService materialFlowService;

    @Autowired
    private ResourceManagementService resourceManagementService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private UserService userService;

    public void createTransfersForTheReceivedProducts(final StateChangeContext stateChangeContext) {
        final Entity delivery = stateChangeContext.getOwner();

        if (delivery == null) {
            return;
        }

        Entity location = delivery.getBelongsToField(DeliveryFieldsDTMF.LOCATION);

        if (location == null) {
            return;
        }

        if (StringUtils.isEmpty(location.getStringField(LocationFields.EXTERNAL_NUMBER))) {
            List<Entity> deliveredProducts = delivery.getHasManyField(DeliveryFields.DELIVERED_PRODUCTS);

            DataDefinition transferDD = dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER,
                    MaterialFlowConstants.MODEL_TRANSFER);

            for (Entity deliveredProduct : deliveredProducts) {
                Entity transfer = transferDD.create();

                transfer.setField(TransferFields.NUMBER, materialFlowService.generateNumberFromProduct(
                        deliveredProduct.getBelongsToField(DeliveredProductFields.PRODUCT), MaterialFlowConstants.MODEL_TRANSFER));
                transfer.setField(TransferFields.TYPE, TransferType.TRANSPORT.getStringValue());
                transfer.setField(TransferFields.TIME, delivery.getDateField(DeliveryFields.DELIVERY_DATE));
                transfer.setField(TransferFields.LOCATION_TO, delivery.getBelongsToField(DeliveryFieldsDTMF.LOCATION));
                transfer.setField(TransferFields.PRODUCT, deliveredProduct.getBelongsToField(DeliveredProductFields.PRODUCT));
                transfer.setField(TransferFields.QUANTITY,
                        deliveredProduct.getDecimalField(DeliveredProductFields.DELIVERED_QUANTITY));
                transfer.setField(TransferFieldsMFR.PRICE,
                        deliveredProduct.getDecimalField(DeliveredProductFields.PRICE_PER_UNIT));
                transfer.setField(TransferFieldsDTMF.FROM_DELIVERY, delivery);

                transfer.getDataDefinition().save(transfer);
            }
        }
    }

    public void createDocumentsForTheReceivedProducts(final StateChangeContext stateChangeContext) {
        final Entity delivery = stateChangeContext.getOwner();

        if (delivery == null) {
            return;
        }

        Entity location = delivery.getBelongsToField(DeliveryFieldsDTMF.LOCATION);

        if (location == null) {
            return;
        }

        List<Entity> deliveredProducts = delivery.getHasManyField(DeliveryFields.DELIVERED_PRODUCTS);

        DataDefinition documentDD = dataDefinitionService.get(MaterialFlowDocumentsConstants.PLUGIN_IDENTIFIER,
                MaterialFlowDocumentsConstants.MODEL_DOCUMENT);
        Entity document = documentDD.create();
        document.setField(DocumentFields.LOCATION_TO,location);
        document.setField(DocumentFields.DELIVERY,delivery);
        document.setField(DocumentFields.STATE, DocumentState.ACCEPTED.getStringValue());
        document.setField(DocumentFields.TIME, new Date());
        document.setField(DocumentFields.USER, userService.getCurrentUserEntity().getId());
        document.setField(DocumentFields.TYPE, DocumentType.RECEIPT.getStringValue());
        document.setField(DocumentFields.NUMBER, numberGeneratorService.generateNumber(MaterialFlowDocumentsConstants.PLUGIN_IDENTIFIER,
                MaterialFlowDocumentsConstants.MODEL_DOCUMENT));

        DataDefinition positionDD = dataDefinitionService.get(MaterialFlowDocumentsConstants.PLUGIN_IDENTIFIER,
                MaterialFlowDocumentsConstants.MODEL_POSITION);

        List<Entity> positions = Lists.newArrayList();

        for (Entity deliveredProduct : deliveredProducts) {
            Entity position = positionDD.create();

            position.setField(PositionFields.PRODUCT, deliveredProduct.getBelongsToField(DeliveredProductFields.PRODUCT));
            position.setField(PositionFields.QUANTITY,
                    deliveredProduct.getDecimalField(DeliveredProductFields.DELIVERED_QUANTITY));
            position.setField(PositionFields.PRICE,
                    deliveredProduct.getDecimalField(DeliveredProductFields.PRICE_PER_UNIT));
            positions.add(position);
        }
        
        document.setField(DocumentFields.POSITIONS, positions);

        Entity savedDocument = documentDD.save(document);

        resourceManagementService.createResourcesForReceiptDocuments(savedDocument);
    }

}
