package com.qcadoo.mes.materialFlowResources.service;

import java.util.List;
import java.util.Optional;

import com.qcadoo.security.api.SecurityService;
import com.qcadoo.security.api.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.materialFlowResources.constants.DocumentFields;
import com.qcadoo.mes.materialFlowResources.constants.DocumentPositionParametersFields;
import com.qcadoo.mes.materialFlowResources.constants.DocumentType;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.ParameterFieldsMFR;
import com.qcadoo.mes.materialFlowResources.constants.PositionAttributeValueFields;
import com.qcadoo.mes.materialFlowResources.constants.PositionFields;
import com.qcadoo.mes.materialFlowResources.constants.StorageLocationFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Component
public class ReceiptDocumentForReleaseHelper {

    @Autowired
    private DocumentManagementService documentManagementService;

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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void tryBuildConnectedDocument(final Entity document, final ViewDefinitionState view) {
        boolean created = tryBuildConnectedDocument(document, true);

        if (created) {
            view.addMessage("materialFlow.document.info.createdConnected", ComponentState.MessageType.INFO);
        }
    }

    protected boolean tryBuildConnectedDocument(final Entity document, final boolean fillDescription) {
        if (buildConnectedDocument(document)) {

            Long id = securityService.getCurrentUserOrQcadooBotId();
            Entity user = userService.find(id);

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

            Optional<Entity> maybyStorageLocation = findStorageLocationForProduct(
                    position.getBelongsToField(PositionFields.PRODUCT), location);

            if (maybyStorageLocation.isPresent()) {
                copiedPosition.setField(PositionFields.STORAGE_LOCATION, maybyStorageLocation.get());
            } else {
                copiedPosition.setField(PositionFields.STORAGE_LOCATION, null);
            }
            List<Entity> attributeValues = Lists.newArrayList();
            position.getHasManyField(PositionFields.POSITION_ATTRIBUTE_VALUES).forEach(pav -> {
                Entity av = pav.getDataDefinition().create();
                av.setField(PositionAttributeValueFields.VALUE, pav.getStringField(PositionAttributeValueFields.VALUE));
                av.setField(PositionAttributeValueFields.ATTRIBUTE,
                        pav.getBelongsToField(PositionAttributeValueFields.ATTRIBUTE));
                av.setField(PositionAttributeValueFields.ATTRIBUTE_VALUE,
                        pav.getBelongsToField(PositionAttributeValueFields.ATTRIBUTE_VALUE));
                attributeValues.add(av);
            });
            copiedPosition.setField(PositionFields.POSITION_ATTRIBUTE_VALUES, attributeValues);
            documentBuilder.addPosition(copiedPosition);
        });
    }

    private String buildDescription(final String number) {
        return translationService.translate("materialFlowResources.document.description.forTemplate",
                LocaleContextHolder.getLocale(), number);
    }

    private Optional<Entity> findStorageLocationForProduct(final Entity product, final Entity location) {
        SearchCriteriaBuilder scb = dataDefinitionService
                .get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_STORAGE_LOCATION)
                .find();

        scb.add(SearchRestrictions.belongsTo(StorageLocationFields.PRODUCT, product));
        scb.add(SearchRestrictions.belongsTo(StorageLocationFields.LOCATION, location));

        return Optional.ofNullable(scb.setMaxResults(1).uniqueResult());
    }

}
