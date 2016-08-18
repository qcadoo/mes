package com.qcadoo.mes.materialFlowResources.service;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.materialFlowResources.constants.DocumentFields;
import com.qcadoo.mes.materialFlowResources.constants.PositionFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.security.api.UserService;
import com.qcadoo.view.api.utils.NumberGeneratorService;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.List;

public class ReceiptDocumentForReleaseHelper {

    private final DataDefinitionService dataDefinitionService;

    private final ResourceManagementService resourceManagementService;

    private final UserService userService;

    private final NumberGeneratorService numberGeneratorService;

    private final TranslationService translationService;

    public ReceiptDocumentForReleaseHelper(final DataDefinitionService dataDefinitionService,
            final ResourceManagementService resourceManagementService, final UserService userService,
            NumberGeneratorService numberGeneratorService, final TranslationService translationService) {
        this.dataDefinitionService = dataDefinitionService;
        this.resourceManagementService = resourceManagementService;
        this.userService = userService;
        this.numberGeneratorService = numberGeneratorService;
        this.translationService = translationService;
    }

    public void tryBuildConnectedPZDocument(Entity document, boolean fillDescription) {
        DocumentBuilder pzBuilder = new DocumentBuilder(dataDefinitionService, resourceManagementService, userService,
                numberGeneratorService, translationService);
        Entity documentDb = document.getDataDefinition().get(document.getId());
        pzBuilder = pzBuilder.receipt(document.getBelongsToField(DocumentFields.LINKED_PZ_DOCUMENT_LOCATION));
        if (fillDescription) {
            pzBuilder = pzBuilder.setField(DocumentFields.DESCRIPTION,
                    buildDescription(documentDb.getStringField(DocumentFields.NUMBER)));
        }
        fillPositions(document, pzBuilder);
        Entity connectedReceiptDocument = pzBuilder.build();
        if (!connectedReceiptDocument.isValid()) {
            document.addGlobalError("materialFlowResources.document.error.creationConnectedDocument");
        }
    }

    private void fillPositions(Entity document, DocumentBuilder pzBuilder) {
        List<Entity> positions = document.getHasManyField(DocumentFields.POSITIONS);
        positions.forEach(pos -> {
            Entity pzPosition = pos.copy();
            pzPosition.setId(null);
            pzPosition.setField(PositionFields.DOCUMENT, null);
            pzPosition.setField(PositionFields.RESOURCE, null);
            pzBuilder.addPosition(pzPosition);
        });
    }

    private String buildDescription(String number) {
        return translationService.translate("materialFlowResources.document.description.forTemplate",
                LocaleContextHolder.getLocale(), number);
    }

}
