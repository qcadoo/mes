package com.qcadoo.mes.materialFlowResources.states;

import com.google.common.collect.Maps;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.materialFlowResources.PalletValidatorService;
import com.qcadoo.mes.materialFlowResources.constants.*;
import com.qcadoo.mes.materialFlowResources.print.StocktakingReportService;
import com.qcadoo.mes.materialFlowResources.print.helper.Resource;
import com.qcadoo.mes.materialFlowResources.print.helper.ResourceDataProvider;
import com.qcadoo.mes.materialFlowResources.service.DocumentBuilder;
import com.qcadoo.mes.materialFlowResources.service.DocumentManagementService;
import com.qcadoo.mes.materialFlowResources.service.ResourceManagementService;
import com.qcadoo.mes.materialFlowResources.states.constants.StocktakingStateChangeDescriber;
import com.qcadoo.mes.materialFlowResources.states.constants.StocktakingStateStringValues;
import com.qcadoo.mes.newstates.BasicStateService;
import com.qcadoo.mes.states.StateChangeEntityDescriber;
import com.qcadoo.model.api.*;
import com.qcadoo.model.api.exception.EntityRuntimeException;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.validators.ErrorMessage;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.security.constants.QcadooSecurityConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
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

    @Autowired
    private PalletValidatorService palletValidatorService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private ResourceManagementService resourceManagementService;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

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
                    if (resource.getQuantity().precision() > 14) {
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
            List<Entity> shortages = entity.getHasManyField(StocktakingFields.DIFFERENCES).stream()
                    .filter(e -> StocktakingDifferenceType.SHORTAGE.getStringValue().equals(e.getStringField(StocktakingDifferenceFields.TYPE)))
                    .collect(Collectors.toList());
            if (!shortages.isEmpty()) {
                Map<Long, BigDecimal> productQuantities = new HashMap<>();
                for (Entity shortage : shortages) {
                    productQuantities.compute(shortage.getBelongsToField(StocktakingDifferenceFields.PRODUCT).getId(),
                            (k, v) -> (v == null) ? shortage.getDecimalField(StocktakingDifferenceFields.QUANTITY).abs()
                                    : v.add(shortage.getDecimalField(StocktakingDifferenceFields.QUANTITY).abs(), numberService.getMathContext()));
                }
                for (Map.Entry<Long, BigDecimal> productQuantity : productQuantities.entrySet()) {
                    Entity product = getProductDD().get(productQuantity.getKey());
                    BigDecimal availableQuantity = getAvailableQuantityForProductAndLocation(product, location);
                    if (productQuantity.getValue().compareTo(availableQuantity) > 0) {
                        entity.addGlobalError("materialFlowResources.stocktaking.document.quantity.notEnoughResources",
                                product.getStringField(ProductFields.NUMBER));
                        return;
                    }
                }
                DocumentBuilder internalOutboundBuilder = documentManagementService.getDocumentBuilder(user);

                internalOutboundBuilder.internalOutbound(location);

                for (Entity difference : shortages) {
                    Entity product = difference.getBelongsToField(StocktakingDifferenceFields.PRODUCT);
                    internalOutboundBuilder.addPosition(product,
                            difference.getDecimalField(StocktakingDifferenceFields.QUANTITY).abs(),
                            null,
                            null,
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

                Entity document = internalOutboundBuilder.buildWithEntityRuntimeException();
                document = document.getDataDefinition().get(document.getId());
                resourceManagementService.fillResourcesInStocktaking(document);
                document = document.getDataDefinition().get(document.getId());
                document.setField(DocumentFields.STATE, DocumentState.ACCEPTED.getStringValue());
                String documentNumber = getDocumentNumber(document.getId());
                document.setField(DocumentFields.NUMBER, documentNumber);
                document.setField(DocumentFields.NAME, documentNumber);
                document = document.getDataDefinition().save(document);
                resourceManagementService.createResources(document);
            }

            List<Entity> surpluses = entity.getHasManyField(StocktakingFields.DIFFERENCES).stream()
                    .filter(e -> StocktakingDifferenceType.SURPLUS.getStringValue().equals(e.getStringField(StocktakingDifferenceFields.TYPE)))
                    .collect(Collectors.toList());

            if (!surpluses.isEmpty()) {
                DocumentBuilder internalInboundBuilder = documentManagementService.getDocumentBuilder(user);

                internalInboundBuilder.internalInbound(location);

                for (Entity difference : surpluses) {
                    Entity storageLocation = difference.getBelongsToField(StocktakingDifferenceFields.STORAGE_LOCATION);
                    if (storageLocation != null) {
                        if (palletValidatorService.tooManyPalletsInStorageLocationAndStocktaking(storageLocation.getStringField(StorageLocationFields.NUMBER), entity.getId())) {
                            entity.addGlobalError("materialFlowResources.stocktaking.document.storageLocation.morePalletsExists",
                                    storageLocation.getStringField(StorageLocationFields.NUMBER));
                            return;
                        }
                    }
                    Entity product = difference.getBelongsToField(StocktakingDifferenceFields.PRODUCT);
                    internalInboundBuilder.addPosition(product,
                            difference.getDecimalField(StocktakingDifferenceFields.QUANTITY),
                            difference.getDecimalField(StocktakingDifferenceFields.QUANTITY_IN_ADDITIONAL_UNIT),
                            Optional.ofNullable(product.getStringField(ProductFields.ADDITIONAL_UNIT)).orElse(product.getStringField(ProductFields.UNIT)),
                            difference.getDecimalField(StocktakingDifferenceFields.CONVERSION), difference.getDecimalField(StocktakingDifferenceFields.PRICE),
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
            }
        } catch (EntityRuntimeException ex) {
            ex.getGlobalErrors().forEach(e -> entity.addGlobalError(e.getMessage(), e.getVars()));
            for (Map.Entry<String, ErrorMessage> error : ex.getErrors().entrySet()) {
                ErrorMessage message = error.getValue();
                if (ResourceFields.PALLET_NUMBER.equals(error.getKey()) && "qcadooView.validate.field.error.missing".equals(message.getMessage())) {
                    entity.addGlobalError("materialFlowResources.stocktaking.document.palletNumberRequired", ex.getEntity().getBelongsToField(ResourceFields.STORAGE_LOCATION).getStringField(StorageLocationFields.NUMBER));
                } else if (ResourceFields.PALLET_NUMBER.equals(error.getKey()) && "documentGrid.error.position.existsOtherResourceForLoadUnitAndTypeOfLoadUnit".equals(message.getMessage())) {
                    entity.addGlobalError("materialFlowResources.stocktaking.document.existsOtherResourceForLoadUnitAndTypeOfLoadUnit", ex.getEntity().getBelongsToField(ResourceFields.STORAGE_LOCATION).getStringField(StorageLocationFields.NUMBER));
                } else {
                    entity.addGlobalError(message.getMessage(), message.getVars());
                }
            }
        } catch (IllegalStateException ex) {
            entity.addGlobalError("materialFlow.document.fillResources.global.error.documentNotValid", false);
        }
    }

    public String getDocumentNumber(final Long documentId) {
        String sql = "SELECT number FROM materialflowresources_document WHERE id = :id;";
        Map<String, Object> parameters = Maps.newHashMap();

        parameters.put("id", documentId);

        return jdbcTemplate.queryForObject(sql, parameters, String.class);
    }

    private BigDecimal getAvailableQuantityForProductAndLocation(Entity product, Entity location) {
        Entity resourceStockDto = dataDefinitionService
                .get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_RESOURCE_STOCK_DTO)
                .find().add(SearchRestrictions.eq(ResourceStockDtoFields.PRODUCT_ID, product.getId().intValue()))
                .add(SearchRestrictions.eq(ResourceStockDtoFields.LOCATION_ID, location.getId().intValue())).setMaxResults(1)
                .uniqueResult();
        if (Objects.isNull(resourceStockDto)) {
            return BigDecimal.ZERO;
        }
        return BigDecimalUtils.convertNullToZero(resourceStockDto.getDecimalField(ResourceStockDtoFields.AVAILABLE_QUANTITY));
    }

    private DataDefinition getUserDD() {
        return dataDefinitionService.get(QcadooSecurityConstants.PLUGIN_IDENTIFIER, QcadooSecurityConstants.MODEL_USER);
    }

    private DataDefinition getProductDD() {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT);
    }
}
