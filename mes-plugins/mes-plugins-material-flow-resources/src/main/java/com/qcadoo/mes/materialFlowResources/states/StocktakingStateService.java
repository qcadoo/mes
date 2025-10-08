package com.qcadoo.mes.materialFlowResources.states;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.materialFlowResources.constants.*;
import com.qcadoo.mes.materialFlowResources.print.StocktakingReportService;
import com.qcadoo.mes.materialFlowResources.print.helper.Resource;
import com.qcadoo.mes.materialFlowResources.print.helper.ResourceDataProvider;
import com.qcadoo.mes.materialFlowResources.service.DocumentBuilder;
import com.qcadoo.mes.materialFlowResources.service.DocumentManagementService;
import com.qcadoo.mes.materialFlowResources.states.constants.StocktakingStateChangeDescriber;
import com.qcadoo.mes.materialFlowResources.states.constants.StocktakingStateStringValues;
import com.qcadoo.mes.newstates.BasicStateService;
import com.qcadoo.mes.states.StateChangeEntityDescriber;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.exception.EntityRuntimeException;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.security.constants.QcadooSecurityConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StocktakingStateService extends BasicStateService implements StocktakingServiceMarker {

    private static final Logger LOG = LoggerFactory.getLogger(StocktakingStateService.class);

    @Autowired
    private StocktakingStateChangeDescriber stocktakingStateChangeDescriber;

    @Autowired
    private StocktakingReportService reportService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ResourceDataProvider resourceDataProvider;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private DocumentManagementService documentManagementService;

    @Autowired
    private TranslationService translationService;

    @Override
    public StateChangeEntityDescriber getChangeEntityDescriber() {
        return stocktakingStateChangeDescriber;
    }

    @Override
    public Entity onValidate(Entity entity, String sourceState, String targetState, Entity stateChangeEntity, StateChangeEntityDescriber describer) {
        switch (targetState) {
            case StocktakingStateStringValues.IN_PROGRESS:
                if (StorageLocationMode.SELECTED.getStringValue().equals(entity.getStringField(StocktakingFields.STORAGE_LOCATION_MODE))
                        && entity.getHasManyField(StocktakingFields.STORAGE_LOCATIONS).isEmpty()) {
                    entity.addGlobalError("materialFlowResources.error.stocktaking.storageLocationsRequired");
                }

                break;
        }

        return entity;
    }

    @Override
    public Entity onAfterSave(Entity entity, String sourceState, String targetState, Entity stateChangeEntity,
                              StateChangeEntityDescriber describer) {
        switch (targetState) {
            case StocktakingStateStringValues.IN_PROGRESS:
                List<Resource> resources = resourceDataProvider.findResourcesAndGroup(entity
                        .getBelongsToField(StocktakingFields.LOCATION).getId(), entity.getHasManyField(StocktakingFields.STORAGE_LOCATIONS).stream().map(Entity::getId).collect(Collectors.toList()), entity
                        .getStringField(StocktakingFields.CATEGORY), true);
                boolean positionStockTooBig = false;
                for (Resource resource : resources) {
                    if (resource.getQuantity().precision() > 9) {
                        entity.addGlobalError("materialFlowResources.error.stocktakingPosition.stock.invalidPrecision", resource.getProductNumber());
                        positionStockTooBig = true;
                        break;
                    }
                }
                if (!positionStockTooBig) {
                    List<Entity> positions = new ArrayList<>();
                    DataDefinition positionDD = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                            MaterialFlowResourcesConstants.MODEL_STOCKTAKING_POSITION);
                    for (Resource resource : resources) {
                        Entity position = positionDD.create();
                        position.setField(StocktakingPositionFields.STORAGE_LOCATION, resource.getStorageLocationId());
                        position.setField(StocktakingPositionFields.PALLET_NUMBER, resource.getPalletNumberId());
                        position.setField(StocktakingPositionFields.TYPE_OF_LOAD_UNIT, resource.getTypeOfLoadUnitId());
                        position.setField(StocktakingPositionFields.PRODUCT, resource.getProductId());
                        position.setField(StocktakingPositionFields.BATCH, resource.getBatchId());
                        position.setField(StocktakingPositionFields.EXPIRATION_DATE, resource.getExpirationDate());
                        position.setField(StocktakingPositionFields.STOCK, resource.getQuantity());
                        position.setField(StocktakingPositionFields.CONVERSION, resource.getConversion());
                        positions.add(position);
                    }
                    entity.setField(StocktakingFields.POSITIONS, positions);
                    entity.setField(StocktakingFields.GENERATION_DATE, new Date());
                    entity = entity.getDataDefinition().save(entity);
                    try {
                        reportService.generateReport(entity);
                    } catch (Exception e) {
                        LOG.error("Error when generate stocktaking report", e);
                        throw new IllegalStateException(e.getMessage(), e);
                    }
                }
                break;

            case StocktakingStateStringValues.FINISHED:
                acceptInboundDocumentsForStocktaking(entity);

                break;
        }

        return entity;
    }

    private void acceptInboundDocumentsForStocktaking(Entity entity) {
        Entity user = getUserDD().get(securityService.getCurrentUserId());
        Entity location = entity.getBelongsToField(StocktakingFields.LOCATION);

        try {
            DocumentBuilder internalOutboundBuilder = documentManagementService.getDocumentBuilder(user);

            internalOutboundBuilder.internalOutbound(location);

            for (Entity difference : entity.getHasManyField(StocktakingFields.DIFFERENCES).stream()
                    .filter(e -> StocktakingDifferenceType.SHORTAGE.getStringValue().equals(e.getStringField(StocktakingDifferenceFields.TYPE)))
                    .collect(Collectors.toList())) {
                internalOutboundBuilder.addPosition(difference.getBelongsToField(StocktakingDifferenceFields.PRODUCT),
                        difference.getDecimalField(StocktakingDifferenceFields.QUANTITY).abs(), null, null,
                        difference.getDecimalField(StocktakingDifferenceFields.CONVERSION), null,
                        difference.getBelongsToField(StocktakingDifferenceFields.BATCH), null,
                        difference.getDateField(StocktakingDifferenceFields.EXPIRATION_DATE), null,
                        difference.getBelongsToField(StocktakingDifferenceFields.STORAGE_LOCATION),
                        difference.getBelongsToField(StocktakingDifferenceFields.PALLET_NUMBER),
                        difference.getBelongsToField(StocktakingDifferenceFields.TYPE_OF_LOAD_UNIT), false);
            }

            internalOutboundBuilder.setField(DocumentFields.STOCKTAKING, entity);
            internalOutboundBuilder.setField(DocumentFields.DESCRIPTION, translationService.translate(
                    "materialFlowResources.document.description.stocktakingInternalOutbound",
                    LocaleContextHolder.getLocale()));

            internalOutboundBuilder.setAccepted().buildWithEntityRuntimeException();

            DocumentBuilder internalInboundBuilder = documentManagementService.getDocumentBuilder(user);

            internalInboundBuilder.internalInbound(location);

            for (Entity difference : entity.getHasManyField(StocktakingFields.DIFFERENCES).stream()
                    .filter(e -> StocktakingDifferenceType.SURPLUS.getStringValue().equals(e.getStringField(StocktakingDifferenceFields.TYPE)))
                    .collect(Collectors.toList())) {
                internalInboundBuilder.addPosition(difference.getBelongsToField(StocktakingDifferenceFields.PRODUCT),
                        difference.getDecimalField(StocktakingDifferenceFields.QUANTITY), null, null,
                        difference.getDecimalField(StocktakingDifferenceFields.CONVERSION), null,
                        difference.getBelongsToField(StocktakingDifferenceFields.BATCH), null,
                        difference.getDateField(StocktakingDifferenceFields.EXPIRATION_DATE), null,
                        difference.getBelongsToField(StocktakingDifferenceFields.STORAGE_LOCATION),
                        difference.getBelongsToField(StocktakingDifferenceFields.PALLET_NUMBER),
                        difference.getBelongsToField(StocktakingDifferenceFields.TYPE_OF_LOAD_UNIT), false);
            }

            internalInboundBuilder.setField(DocumentFields.STOCKTAKING, entity);
            internalInboundBuilder.setField(DocumentFields.DESCRIPTION, translationService.translate(
                    "materialFlowResources.document.description.stocktakingInternalInbound",
                    LocaleContextHolder.getLocale()));

            internalInboundBuilder.setAccepted().buildWithEntityRuntimeException();
        } catch (EntityRuntimeException ex) {
            ex.getGlobalErrors().forEach(e -> entity.addGlobalError(e.getMessage(), e.getVars()));
        }
    }

    private DataDefinition getUserDD() {
        return dataDefinitionService.get(QcadooSecurityConstants.PLUGIN_IDENTIFIER, QcadooSecurityConstants.MODEL_USER);
    }
}
