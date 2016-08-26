package com.qcadoo.mes.materialFlowResources.service;

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
import com.qcadoo.security.api.UserService;
import com.qcadoo.view.api.utils.NumberGeneratorService;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.List;
import java.util.Optional;

public class ReceiptDocumentForReleaseHelper {

    private final DataDefinitionService dataDefinitionService;

    private final ResourceManagementService resourceManagementService;

    private final UserService userService;

    private final NumberGeneratorService numberGeneratorService;

    private final TranslationService translationService;

    private final ParameterService parameterService;

    public ReceiptDocumentForReleaseHelper(final DataDefinitionService dataDefinitionService,
            final ResourceManagementService resourceManagementService, final UserService userService,
            NumberGeneratorService numberGeneratorService, final TranslationService translationService,
            final ParameterService parameterService) {
        this.dataDefinitionService = dataDefinitionService;
        this.resourceManagementService = resourceManagementService;
        this.userService = userService;
        this.numberGeneratorService = numberGeneratorService;
        this.translationService = translationService;
        this.parameterService = parameterService;
    }

    public boolean tryBuildConnectedPZDocument(Entity document, boolean fillDescription) {
        DocumentBuilder pzBuilder = new DocumentBuilder(dataDefinitionService, resourceManagementService, userService,
                numberGeneratorService, translationService, parameterService);
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

    public Optional<Entity> findStorageLocationForProduct(final Entity product, final Entity location) {
        SearchCriteriaBuilder scb = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_STORAGE_LOCATION).find();
        scb.add(SearchRestrictions.belongsTo(StorageLocationFields.PRODUCT, product));
        scb.add(SearchRestrictions.belongsTo(StorageLocationFields.LOCATION, location));
        return Optional.ofNullable(scb.setMaxResults(1).uniqueResult());
    }

}
