package com.qcadoo.mes.materialFlowResources.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.materialFlowResources.constants.DocumentFields;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
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

    boolean tryBuildConnectedPZDocument(Entity document, boolean fillDescription) {
        DocumentBuilder pzBuilder = documentManagementService.getDocumentBuilder();
        Entity documentDb = document.getDataDefinition().get(document.getId());
        Entity location = document.getBelongsToField(DocumentFields.LINKED_PZ_DOCUMENT_LOCATION);
        pzBuilder = pzBuilder.receipt(location);
        if (fillDescription) {
            pzBuilder = pzBuilder.setField(DocumentFields.DESCRIPTION,
                    buildDescription(documentDb.getStringField(DocumentFields.NUMBER)));
        }
        fillPositions(location, document, pzBuilder);
        Entity connectedReceiptDocument = null;
        if (parameterService.getParameter().getStringField("documentsStatus").equals("01accepted")) {
            connectedReceiptDocument = pzBuilder.setAccepted().build();
        } else {
            connectedReceiptDocument = pzBuilder.build();
        }
        if (!connectedReceiptDocument.isValid()) {
            document.addGlobalError("materialFlowResources.document.error.creationConnectedDocument");
            return false;
        }
        return true;

    }

    public boolean buildConnectedPZDocument(final Entity document) {
        return document.getBooleanField(DocumentFields.CREATE_LINKED_PZ_DOCUMENT)
                && document.getBelongsToField(DocumentFields.LINKED_PZ_DOCUMENT_LOCATION) != null;
    }

    private void fillPositions(Entity location, Entity document, DocumentBuilder pzBuilder) {
        List<Entity> positions = document.getHasManyField(DocumentFields.POSITIONS);
        positions.forEach(pos -> {
            Entity pzPosition = pos.copy();
            pzPosition.setId(null);
            pzPosition.setField(PositionFields.DOCUMENT, null);
            pzPosition.setField(PositionFields.RESOURCE, null);
            pzPosition.setField(PositionFields.TYPE_OF_PALLET, null);
            pzPosition.setField(PositionFields.PALLET_NUMBER, null);
            Optional<Entity> maybyStorageLocation = findStorageLocationForProduct(pos.getBelongsToField(PositionFields.PRODUCT),
                    location);
            if (maybyStorageLocation.isPresent()) {
                pzPosition.setField(PositionFields.STORAGE_LOCATION, maybyStorageLocation.get());
            } else {
                pzPosition.setField(PositionFields.STORAGE_LOCATION, null);
            }
            pzBuilder.addPosition(pzPosition);
        });
    }

    private String buildDescription(String number) {
        return translationService.translate("materialFlowResources.document.description.forTemplate",
                LocaleContextHolder.getLocale(), number);
    }

    private Optional<Entity> findStorageLocationForProduct(final Entity product, final Entity location) {
        SearchCriteriaBuilder scb = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_STORAGE_LOCATION).find();
        scb.add(SearchRestrictions.belongsTo(StorageLocationFields.PRODUCT, product));
        scb.add(SearchRestrictions.belongsTo(StorageLocationFields.LOCATION, location));
        return Optional.ofNullable(scb.setMaxResults(1).uniqueResult());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void tryBuildPz(Entity documentToCreateResourcesFor, ViewDefinitionState view) {
        boolean created = tryBuildConnectedPZDocument(documentToCreateResourcesFor, true);
        if (created) {
            view.addMessage("materialFlow.document.info.createdConnectedPZ", ComponentState.MessageType.INFO);
        }
    }

}
