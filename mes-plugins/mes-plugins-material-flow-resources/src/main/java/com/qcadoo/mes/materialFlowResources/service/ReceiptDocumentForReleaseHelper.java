package com.qcadoo.mes.materialFlowResources.service;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.ParameterService;
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
        pzBuilder = pzBuilder.receipt(document.getBelongsToField(DocumentFields.LINKED_PZ_DOCUMENT_LOCATION));
        if (fillDescription) {
            pzBuilder = pzBuilder.setField(DocumentFields.DESCRIPTION,
                    buildDescription(documentDb.getStringField(DocumentFields.NUMBER)));
        }
        fillPositions(document, pzBuilder);
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

    private void fillPositions(Entity document, DocumentBuilder pzBuilder) {
        List<Entity> positions = document.getHasManyField(DocumentFields.POSITIONS);
        positions.forEach(pos -> {
            Entity pzPosition = pos.copy();
            pzPosition.setId(null);
            pzPosition.setField(PositionFields.DOCUMENT, null);
            pzPosition.setField(PositionFields.RESOURCE, null);
            pzPosition.setField(PositionFields.TYPE_OF_PALLET, null);
            pzPosition.setField(PositionFields.PALLET_NUMBER, null);
            pzPosition.setField(PositionFields.STORAGE_LOCATION, null);
            pzBuilder.addPosition(pzPosition);
        });
    }

    private String buildDescription(String number) {
        return translationService.translate("materialFlowResources.document.description.forTemplate",
                LocaleContextHolder.getLocale(), number);
    }

}
