package com.qcadoo.mes.productFlowThruDivision.service;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.CalculationQuantityService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.UnitConversionItemFieldsB;
import com.qcadoo.mes.materialFlow.constants.LocationFields;
import com.qcadoo.mes.materialFlowResources.constants.DocumentFields;
import com.qcadoo.mes.materialFlowResources.constants.DocumentType;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.PositionFields;
import com.qcadoo.mes.materialFlowResources.exceptions.DocumentBuildException;
import com.qcadoo.mes.materialFlowResources.service.DocumentBuilder;
import com.qcadoo.mes.materialFlowResources.service.DocumentManagementService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productFlowThruDivision.constants.DocumentsStatus;
import com.qcadoo.mes.productFlowThruDivision.constants.DrawnDocuments;
import com.qcadoo.mes.productFlowThruDivision.reservation.ReservationsServiceForProductsToIssue;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.CreationDocumentResponse;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.WarehouseIssueParameterService;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.constans.IssueFields;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.constans.WarehouseIssueFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.units.PossibleUnitConversions;
import com.qcadoo.model.api.units.UnitConversionService;
import com.qcadoo.model.api.validators.ErrorMessage;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.security.api.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class WarehouseIssueDocumentsService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private UnitConversionService unitConversionService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private UserService userService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private DocumentManagementService documentManagementService;

    @Autowired
    private WarehouseIssueParameterService warehouseIssueParameterService;

    @Autowired
    private ReservationsServiceForProductsToIssue reservationsServiceForProductsToIssue;

    @Autowired
    private CalculationQuantityService calculationQuantityService;

    public CreationDocumentResponse createWarehouseDocument(final Entity locationFrom, final Entity locationTo,
                                                            final Collection positions) {
        return createWarehouseDocument(locationFrom, locationTo, positions, null);
    }

    public CreationDocumentResponse createWarehouseDocument(final Entity locationFrom, final Entity locationTo,
                                                            final Collection positions, final String additionalInfo) {
        List<Entity> _issues = Lists.newArrayList(positions);

        for (Entity issue : _issues) {
            reservationsServiceForProductsToIssue.onIssue(issue);
        }

        DrawnDocuments drawnDocument = warehouseIssueParameterService.getDrawnDocument();
        DocumentsStatus documentsStatus = warehouseIssueParameterService.getDocuemtStatusCreatedDocuemnt();

        CreationDocumentResponse response = null;

        switch (drawnDocument) {
            case RECEIPT_RELEASE:
                response = buildReceiptReleasePairDocuments(locationFrom, locationTo, positions, documentsStatus, additionalInfo);
                break;

            case TRANSFER:
                response = buildTransferDocument(locationFrom, locationTo, positions, documentsStatus, additionalInfo);
                break;
        }

        if (Objects.isNull(response)) {
            response = new CreationDocumentResponse(false);
        }

        if (!response.isValid()) {
            for (Entity issue : _issues) {
                reservationsServiceForProductsToIssue.onIssueCompensation(issue);
            }
        }

        return response;
    }

    private CreationDocumentResponse buildReceiptReleasePairDocuments(final Entity locationFrom, final Entity locationTo,
                                                                      final Collection positions, final DocumentsStatus documentsStatus, final String additionalInfo) {
        CreationDocumentResponse validReleaseDocument = buildReleaseDocument(locationFrom, locationTo, positions, documentsStatus,
                additionalInfo);

        return validReleaseDocument;
    }

    private CreationDocumentResponse buildReleaseDocument(final Entity locationFrom, final Entity locationTo,
                                                          final Collection positions, final DocumentsStatus documentsStatus, final String additionalInfo) {
        Long currentUserId = securityService.getCurrentUserOrQcadooBotId();
        Entity user = userService.find(currentUserId);

        DocumentBuilder documentBuilder = documentManagementService.getDocumentBuilder(user);

        documentBuilder.setField(DocumentFields.TYPE, DocumentType.RELEASE.getStringValue());
        documentBuilder.setField(DocumentFields.LOCATION_FROM, locationFrom);
        documentBuilder.setField(DocumentFields.DESCRIPTION,
                buildDescriptionForReleaseDocument(locationTo, positions, additionalInfo));
        documentBuilder.setField(DocumentFields.CREATE_LINKED_DOCUMENT, true);
        documentBuilder.setField(DocumentFields.LINKED_DOCUMENT_LOCATION, locationTo);

        buildDocumentPositions(positions, documentBuilder);

        return build(documentsStatus, documentBuilder);
    }

    private String buildDescriptionForReleaseDocument(final Entity locationTo, final Collection positions,
                                                      final String additionalInfo) {
        String description = buildDescription(positions);

        description = Strings.isNullOrEmpty(description) ? "" : description + "\n";
        description += translationService.translate(
                "productFlowThruDivision.issue.documentGeneration.descriptionForReleaseDocument", LocaleContextHolder.getLocale(),
                locationTo.getStringField(LocationFields.NAME));

        if (Objects.nonNull(additionalInfo)) {
            description += "\n" + additionalInfo;
        }

        return description;
    }

    private String buildDescriptionForTransferDocument(final Collection positions, final String additionalInfo) {
        String description = buildDescription(positions);

        if (Objects.nonNull(additionalInfo)) {
            description += "\n" + additionalInfo;
        }

        return description;
    }

    private CreationDocumentResponse buildTransferDocument(final Entity locationFrom, final Entity locationTo,
                                                           final Collection positions, final DocumentsStatus documentsStatus, final String additionalInfo) {
        Long currentUserId = securityService.getCurrentUserOrQcadooBotId();
        Entity user = userService.find(currentUserId);

        DocumentBuilder documentBuilder = documentManagementService.getDocumentBuilder(user);

        documentBuilder.setField(DocumentFields.TYPE, DocumentType.TRANSFER.getStringValue());
        documentBuilder.setField(DocumentFields.LOCATION_FROM, locationFrom);
        documentBuilder.setField(DocumentFields.LOCATION_TO, locationTo);
        documentBuilder.setField(DocumentFields.DESCRIPTION, buildDescriptionForTransferDocument(positions, additionalInfo));

        buildDocumentPositions(positions, documentBuilder);

        return build(documentsStatus, documentBuilder);
    }

    private CreationDocumentResponse build(final DocumentsStatus documentsStatus, final DocumentBuilder documentBuilder) {
        Entity document;

        try {
            if (DocumentsStatus.ACCEPTED.getStrValue().equals(documentsStatus.getStrValue())) {
                document = documentBuilder.setAccepted().buildWithEntityRuntimeException();
            } else {
                document = documentBuilder.buildWithEntityRuntimeException();
            }

            if (!document.isValid()) {
                List<ErrorMessage> errors = Lists.newArrayList();

                errors.addAll(document.getGlobalErrors());

                return new CreationDocumentResponse(false, errors);
            }

            CreationDocumentResponse creationDocumentResponse = new CreationDocumentResponse(true);
            creationDocumentResponse.setDocument(document);

            return creationDocumentResponse;
        } catch (DocumentBuildException e) {
            return new CreationDocumentResponse(false, e.getGlobalErrors());
        }
    }

    private void buildDocumentPositions(final Collection positions, final DocumentBuilder documentBuilder) {
        for (Object obj : positions) {
            Entity issue = (Entity) obj;
            Entity product = issue.getBelongsToField(IssueFields.PRODUCT);
            BigDecimal quantity = issue.getDecimalField(IssueFields.ISSUE_QUANTITY);
            BigDecimal conversion = BigDecimal.ONE;

            Entity position = getPositionDD().create();

            String additionalUnit = product.getStringField(ProductFields.ADDITIONAL_UNIT);
            String unit = product.getStringField(ProductFields.UNIT);

            if (Objects.nonNull(issue.getDecimalField(IssueFields.CONVERSION))) {
                conversion = issue.getDecimalField(IssueFields.CONVERSION);
                position.setField(PositionFields.CONVERSION, conversion);

                if (StringUtils.isEmpty(additionalUnit)) {
                    position.setField(PositionFields.GIVEN_UNIT, unit);
                    position.setField(PositionFields.GIVEN_QUANTITY, quantity);
                } else {
                    position.setField(PositionFields.GIVEN_UNIT, additionalUnit);

                    BigDecimal newAdditionalQuantity = calculationQuantityService.calculateAdditionalQuantity(quantity,
                            conversion, additionalUnit);

                    position.setField(PositionFields.GIVEN_QUANTITY, newAdditionalQuantity);
                }
            } else if (!StringUtils.isEmpty(additionalUnit)) {
                PossibleUnitConversions unitConversions = unitConversionService.getPossibleConversions(unit,
                        searchCriteriaBuilder -> searchCriteriaBuilder
                                .add(SearchRestrictions.belongsTo(UnitConversionItemFieldsB.PRODUCT, product)));

                if (unitConversions.isDefinedFor(additionalUnit)) {
                    BigDecimal convertedQuantity = unitConversions.convertTo(quantity, additionalUnit);

                    position.setField(PositionFields.GIVEN_QUANTITY, convertedQuantity);
                    position.setField(PositionFields.GIVEN_UNIT, additionalUnit);
                    position.setField(PositionFields.CONVERSION,
                            numberService.setScaleWithDefaultMathContext(getConversion(product, unit, additionalUnit)));
                }
            } else {
                position.setField(PositionFields.GIVEN_UNIT, unit);
                position.setField(PositionFields.GIVEN_QUANTITY, quantity);
                position.setField(PositionFields.CONVERSION, conversion);
            }

            position.setField(PositionFields.QUANTITY, quantity);
            position.setField(PositionFields.PRODUCT, product);

            position.setField(PositionFields.DOCUMENT, documentBuilder.getDocument());

            documentBuilder.addPosition(position);
        }
    }

    private BigDecimal getConversion(final Entity product, final String unit, final String additionalUnit) {
        PossibleUnitConversions unitConversions = unitConversionService.getPossibleConversions(unit,
                searchCriteriaBuilder -> searchCriteriaBuilder
                        .add(SearchRestrictions.belongsTo(UnitConversionItemFieldsB.PRODUCT, product)));

        if (unitConversions.isDefinedFor(additionalUnit)) {
            return unitConversions.asUnitToConversionMap().get(additionalUnit);
        } else {
            return BigDecimal.ZERO;
        }
    }

    private String buildDescription(final Collection positions) {
        if (warehouseIssueParameterService.issueForOrder()) {
            Set<String> ordersName = Sets.newHashSet();

            for (Object obj : positions) {
                Entity issue = (Entity) obj;

                ordersName.add(issue.getBelongsToField(IssueFields.WAREHOUSE_ISSUE).getBelongsToField(WarehouseIssueFields.ORDER)
                        .getStringField(OrderFields.NUMBER));
            }

            StringJoiner joiner = new StringJoiner(",");

            ordersName.forEach(joiner::add);

            return translationService.translate("productFlowThruDivision.issue.documentGeneration.forOrder",
                    LocaleContextHolder.getLocale(), joiner.toString());
        }

        return "";
    }

    private DataDefinition getPositionDD() {
        return dataDefinitionService
                .get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_POSITION);
    }

}
