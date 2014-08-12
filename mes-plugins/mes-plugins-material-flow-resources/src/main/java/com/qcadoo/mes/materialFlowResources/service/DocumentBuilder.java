package com.qcadoo.mes.materialFlowResources.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.qcadoo.mes.materialFlowResources.constants.DocumentFields;
import com.qcadoo.mes.materialFlowResources.constants.DocumentState;
import com.qcadoo.mes.materialFlowResources.constants.DocumentType;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.PositionFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.security.api.UserService;
import com.qcadoo.view.api.utils.NumberGeneratorService;

public class DocumentBuilder {

    private final DataDefinitionService dataDefinitionService;

    private final ResourceManagementService resourceManagementService;

    private final Entity document;

    private final List<Entity> positions = Lists.newArrayList();

    DocumentBuilder(final DataDefinitionService dataDefinitionService, final ResourceManagementService resourceManagementService,
            final UserService userService, NumberGeneratorService numberGeneratorService) {
        this.dataDefinitionService = dataDefinitionService;
        this.resourceManagementService = resourceManagementService;
        this.document = createDocument(userService, numberGeneratorService);
    }

    public DocumentBuilder receipt(final Entity locationTo) {
        document.setField(DocumentFields.LOCATION_TO, locationTo);
        document.setField(DocumentFields.TYPE, DocumentType.RECEIPT.getStringValue());
        return this;
    }

    public DocumentBuilder internalOutbound(Entity locationFrom) {
        document.setField(DocumentFields.LOCATION_FROM, locationFrom);
        document.setField(DocumentFields.TYPE, DocumentType.INTERNAL_OUTBOUND.getStringValue());
        return this;
    }

    public DocumentBuilder internalInbound(Entity locationTo) {
        document.setField(DocumentFields.LOCATION_TO, locationTo);
        document.setField(DocumentFields.TYPE, DocumentType.INTERNAL_INBOUND.getStringValue());
        return this;
    }

    /**
     * Add position to document, use this method for outbound and transfer documents where additional attributes should not been
     * set.
     * 
     * @param product
     * @param quantity
     * @return DocumentBuilder.this
     */
    public DocumentBuilder addPosition(final Entity product, final BigDecimal quantity) {
        return addPosition(product, quantity, null, null, null, null);
    }

    /**
     * Add position to document, use this method for inbound documents where additional attributes are required sometimes.
     * 
     * @param product
     * @param quantity
     * @param price
     * @param batch
     * @param expirationDate
     * @param productionDate
     * @return DocumentBuilder.this
     */
    public DocumentBuilder addPosition(final Entity product, final BigDecimal quantity, final BigDecimal price,
            final String batch, final Date productionDate, final Date expirationDate) {
        Preconditions.checkArgument(product != null, "Product argument is required.");
        Preconditions.checkArgument(quantity != null, "Quantity argument is required.");

        DataDefinition positionDD = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_POSITION);
        Entity position = positionDD.create();

        position.setField(PositionFields.PRODUCT, product);
        position.setField(PositionFields.QUANTITY, quantity);
        position.setField(PositionFields.PRICE, price);
        position.setField(PositionFields.BATCH, batch);
        position.setField(PositionFields.PRODUCTION_DATE, productionDate);
        position.setField(PositionFields.EXPIRATION_DATE, expirationDate);
        positions.add(position);
        return this;
    }

    public DocumentBuilder setAccepted() {
        document.setField(DocumentFields.STATE, DocumentState.ACCEPTED.getStringValue());
        return this;
    }

    /**
     * Use this method to set document fields added by any extending plugins.
     * 
     * @param field
     *            field name
     * @param value
     *            field value
     * @return this builder
     */
    public DocumentBuilder setField(String field, Object value) {
        document.setField(field, value);
        return this;
    }

    /**
     * Save document in database and creates resources if document is accepted.
     * 
     * @return Created document entity.
     */
    public Entity build() {
        DataDefinition documentDD = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_DOCUMENT);

        document.setField(DocumentFields.POSITIONS, positions);
        Entity savedDocument = documentDD.save(document);
        if (savedDocument.isValid() && DocumentState.of(document) == DocumentState.ACCEPTED) {
            createResources(savedDocument);
            if (!savedDocument.isValid()) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            }
        }

        return savedDocument;
    }

    private void createResources(Entity savedDocument) {
        DocumentType documentType = DocumentType.of(savedDocument);
        if (DocumentType.RECEIPT.equals(documentType) || DocumentType.INTERNAL_INBOUND.equals(documentType)) {
            resourceManagementService.createResourcesForReceiptDocuments(savedDocument);
        } else if (DocumentType.INTERNAL_OUTBOUND.equals(documentType) || DocumentType.RELEASE.equals(documentType)) {
            resourceManagementService.updateResourcesForReleaseDocuments(savedDocument);
        } else if (DocumentType.TRANSFER.equals(documentType)) {
            resourceManagementService.moveResourcesForTransferDocument(savedDocument);
        } else {
            throw new IllegalStateException("Unsupported document type");
        }
    }

    private Entity createDocument(UserService userService, NumberGeneratorService numberGeneratorService) {
        DataDefinition documentDD = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_DOCUMENT);
        Entity document = documentDD.create();
        document.setField(DocumentFields.TIME, new Date());
        document.setField(DocumentFields.USER, userService.getCurrentUserEntity().getId());
        document.setField(DocumentFields.STATE, DocumentState.DRAFT.getStringValue());
        document.setField(DocumentFields.NUMBER, numberGeneratorService.generateNumber(
                MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_DOCUMENT));
        document.setField(DocumentFields.POSITIONS, Lists.newArrayList());
        return document;
    }
}
