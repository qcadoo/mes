package com.qcadoo.mes.materialFlowResources.service;

import com.google.common.collect.Lists;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.materialFlowResources.MaterialFlowResourcesService;
import com.qcadoo.mes.materialFlowResources.constants.*;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.security.api.UserService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
public class ReceiptDocumentForReleaseHelper {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private UserService userService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private DocumentManagementService documentManagementService;

    @Autowired
    private MaterialFlowResourcesService materialFlowResourcesService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void tryBuildConnectedDocument(final Entity document, final ViewDefinitionState view) {
        boolean created = tryBuildConnectedDocument(document, true);

        if (created) {
            view.addMessage("materialFlow.document.info.createdConnected", ComponentState.MessageType.INFO);
        }
    }

    protected boolean tryBuildConnectedDocument(final Entity document, final boolean fillDescription) {
        if (buildConnectedDocument(document)) {
            Entity user = userService.find(securityService.getCurrentUserOrQcadooBotId());

            DocumentBuilder documentBuilder = documentManagementService.getDocumentBuilder(user);

            Entity documentFromDB = document.getDataDefinition().get(document.getId());

            Entity linkedDocumentLocation = document.getBelongsToField(DocumentFields.LINKED_DOCUMENT_LOCATION);
            String type = document.getStringField(DocumentFields.TYPE);

            if (DocumentType.INTERNAL_OUTBOUND.getStringValue().equals(type)) {
                documentBuilder = documentBuilder.internalInbound(linkedDocumentLocation);
            } else {
                documentBuilder = documentBuilder.receipt(linkedDocumentLocation);
            }

            if (fillDescription) {
                documentBuilder = documentBuilder.setField(DocumentFields.DESCRIPTION,
                        buildDescription(documentFromDB.getStringField(DocumentFields.NUMBER)));
            }

            fillPositions(linkedDocumentLocation, document, documentBuilder);

            Entity connectedDocument;

            if (parameterService.getParameter().getStringField("documentsStatus").equals("01accepted")) {
                connectedDocument = documentBuilder.setAccepted().build();
            } else {
                connectedDocument = documentBuilder.build();
            }

            if (!connectedDocument.isValid()) {
                document.addGlobalError("materialFlowResources.document.error.creationConnectedDocument");

                return false;
            }

            return true;
        } else {
            return false;
        }
    }

    private boolean buildConnectedDocument(final Entity document) {
        return document.getBooleanField(DocumentFields.CREATE_LINKED_DOCUMENT)
                && document.getBelongsToField(DocumentFields.LINKED_DOCUMENT_LOCATION) != null;
    }

    private void fillPositions(final Entity location, final Entity document, final DocumentBuilder documentBuilder) {
        Entity documentPositionParameters = parameterService.getParameter()
                .getBelongsToField(ParameterFieldsMFR.DOCUMENT_POSITION_PARAMETERS);

        boolean transferPalletToReceivingWarehouse = documentPositionParameters
                .getBooleanField(DocumentPositionParametersFields.TRANSFER_PALLET_TO_RECEIVING_WAREHOUSE);

        List<Entity> positions = document.getHasManyField(DocumentFields.POSITIONS);

        positions.forEach(position -> {
            Entity copiedPosition = position.copy();

            copiedPosition.setId(null);
            copiedPosition.setField(PositionFields.DOCUMENT, null);
            copiedPosition.setField(PositionFields.RESOURCE, null);
            copiedPosition.setField(PositionFields.RESOURCE_NUMBER, null);
            copiedPosition.setField(PositionFields.TYPE_OF_PALLET, null);

            if (transferPalletToReceivingWarehouse) {
                copiedPosition.setField(PositionFields.PALLET_NUMBER, position.getBelongsToField(PositionFields.PALLET_NUMBER));
            } else {
                copiedPosition.setField(PositionFields.PALLET_NUMBER, null);
            }

            copiedPosition.setField(PositionFields.POSITION_ATTRIBUTE_VALUES, null);

            Optional<Entity> maybeStorageLocation = materialFlowResourcesService.findStorageLocationForProduct(location,
                    position.getBelongsToField(PositionFields.PRODUCT));

            if (maybeStorageLocation.isPresent()) {
                copiedPosition.setField(PositionFields.STORAGE_LOCATION, maybeStorageLocation.get());
            } else {
                copiedPosition.setField(PositionFields.STORAGE_LOCATION, null);
            }

            List<Entity> attributeValues = Lists.newArrayList();

            position.getHasManyField(PositionFields.POSITION_ATTRIBUTE_VALUES).forEach(pav -> {
                Entity attributeValue = pav.getDataDefinition().create();

                attributeValue.setField(PositionAttributeValueFields.VALUE, pav.getStringField(PositionAttributeValueFields.VALUE));
                attributeValue.setField(PositionAttributeValueFields.ATTRIBUTE,
                        pav.getBelongsToField(PositionAttributeValueFields.ATTRIBUTE));
                attributeValue.setField(PositionAttributeValueFields.ATTRIBUTE_VALUE,
                        pav.getBelongsToField(PositionAttributeValueFields.ATTRIBUTE_VALUE));

                attributeValues.add(attributeValue);
            });

            copiedPosition.setField(PositionFields.POSITION_ATTRIBUTE_VALUES, attributeValues);

            documentBuilder.addPosition(copiedPosition);
        });
    }

    private String buildDescription(final String number) {
        return translationService.translate("materialFlowResources.document.description.forTemplate",
                LocaleContextHolder.getLocale(), number);
    }

}
