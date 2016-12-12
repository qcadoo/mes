/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.materialFlowResources.listeners;

import static com.qcadoo.mes.basic.constants.ProductFields.UNIT;

import java.math.BigDecimal;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.commons.functional.Either;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.UnitConversionItemFieldsB;
import com.qcadoo.mes.materialFlowResources.MaterialFlowResourcesService;
import com.qcadoo.mes.materialFlowResources.constants.DocumentFields;
import com.qcadoo.mes.materialFlowResources.constants.DocumentState;
import com.qcadoo.mes.materialFlowResources.constants.DocumentType;
import com.qcadoo.mes.materialFlowResources.constants.LocationFieldsMFR;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.ParameterFieldsMFR;
import com.qcadoo.mes.materialFlowResources.constants.PositionFields;
import com.qcadoo.mes.materialFlowResources.constants.WarehouseAlgorithm;
import com.qcadoo.mes.materialFlowResources.hooks.DocumentDetailsHooks;
import com.qcadoo.mes.materialFlowResources.service.ReceiptDocumentForReleaseHelper;
import com.qcadoo.mes.materialFlowResources.service.ResourceManagementService;
import com.qcadoo.mes.materialFlowResources.service.ResourceReservationsService;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.units.PossibleUnitConversions;
import com.qcadoo.model.api.units.UnitConversionService;
import com.qcadoo.security.api.UserService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.AwesomeDynamicListComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class DocumentDetailsListeners {

    private static final String L_FORM = "form";

    private static final String L_RESOURCE = "resource";

    private static final String L_BATCH = "batch";

    public static final String L_POSITIONS = "positions";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private UnitConversionService unitConversionService;

    @Autowired
    private UserService userService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private ResourceManagementService resourceManagementService;

    @Autowired
    private MaterialFlowResourcesService materialFlowResourcesService;

    @Autowired
    private DocumentDetailsHooks documentDetailsHooks;

    @Autowired
    private ResourceReservationsService resourceReservationsService;

    public void printDocument(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        FormComponent documentForm = (FormComponent) view.getComponentByReference(L_FORM);

        Entity document = documentForm.getEntity();

        view.redirectTo("/materialFlowResources/document." + args[0] + "?id=" + document.getId(), true, false);
    }

    public void printDispositionOrder(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        Entity documentPositionParameters = parameterService.getParameter()
                .getBelongsToField(ParameterFieldsMFR.DOCUMENT_POSITION_PARAMETERS);

        boolean acceptanceOfDocumentBeforePrinting = documentPositionParameters
                .getBooleanField("acceptanceOfDocumentBeforePrinting");

        if (acceptanceOfDocumentBeforePrinting) {
            createResourcesForDocuments(view, componentState, args);
        }

        FormComponent documentForm = (FormComponent) view.getComponentByReference(L_FORM);

        Entity document = documentForm.getEntity();

        if (documentForm.isValid()) {
            view.redirectTo("/materialFlowResources/dispositionOrder." + args[0] + "?id=" + document.getId(), true, false);
        }
    }

    public void onSave(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        FormComponent documentForm = (FormComponent) view.getComponentByReference(L_FORM);

        Entity document = documentForm.getEntity();

        DataDefinition documentDD = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_DOCUMENT);

        String documentName = document.getStringField(DocumentFields.NAME);

        if (StringUtils.isNotEmpty(documentName)) {
            SearchCriteriaBuilder searchCriteriaBuilder = documentDD.find()
                    .add(SearchRestrictions.eq(DocumentFields.NAME, documentName));

            if (document.getId() != null) {
                searchCriteriaBuilder.add(SearchRestrictions.ne("id", document.getId()));
            }

            boolean duplicateName = searchCriteriaBuilder.list().getTotalNumberOfEntities() > 0;

            if (duplicateName) {
                view.addMessage("materialFlow.info.document.name.duplicate", MessageType.INFO, documentName);
            }
        }
    }

    public void createResourcesForDocuments(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        DataDefinition documentDD = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_DOCUMENT);

        FormComponent documentForm = (FormComponent) view.getComponentByReference(L_FORM);

        Entity document = documentForm.getPersistedEntityWithIncludedFormValues();

        String documentState = document.getStringField(DocumentFields.STATE);

        if (!DocumentState.DRAFT.getStringValue().equals(documentState)) {
            return;
        }

        document.setField(DocumentFields.STATE, DocumentState.ACCEPTED.getStringValue());

        Entity documentToCreateResourcesFor = documentDD.save(document);

        if (!documentToCreateResourcesFor.isValid()) {
            documentToCreateResourcesFor.setField(DocumentFields.STATE, DocumentState.DRAFT.getStringValue());

            documentForm.setEntity(documentToCreateResourcesFor);

            return;
        }

        if (!validateResourceAttribute(document)) {
            documentForm.addMessage("materialFlow.error.position.batch.required", MessageType.FAILURE);

            documentToCreateResourcesFor.setField(DocumentFields.STATE, DocumentState.DRAFT.getStringValue());

            documentForm.setEntity(documentToCreateResourcesFor);

            return;
        }
        boolean emptyPositions = false;

        if (!documentToCreateResourcesFor.getHasManyField(DocumentFields.POSITIONS).isEmpty()) {
            emptyPositions = false;
            createResources(documentToCreateResourcesFor);
        } else {
            emptyPositions = true;
            documentToCreateResourcesFor.setNotValid();

            documentForm.addMessage("materialFlow.document.validate.global.error.emptyPositions", MessageType.FAILURE);
        }

        if (!documentToCreateResourcesFor.isValid()) {
            Entity recentlySavedDocument = documentDD.get(document.getId());

            recentlySavedDocument.setField(DocumentFields.STATE, DocumentState.DRAFT.getStringValue());

            documentDD.save(recentlySavedDocument);

            documentToCreateResourcesFor.setField(DocumentFields.STATE, DocumentState.DRAFT.getStringValue());
        } else {
            documentForm.addMessage("materialFlowResources.success.documentAccepted", MessageType.SUCCESS);
        }

        documentToCreateResourcesFor = documentToCreateResourcesFor.getDataDefinition().save(documentToCreateResourcesFor);

        updatePositions(documentToCreateResourcesFor);

        Entity recentlySavedDocument = documentDD.get(document.getId());

        if (!emptyPositions && documentToCreateResourcesFor.isValid() && buildConnectedPZDocument(recentlySavedDocument)) {
            ReceiptDocumentForReleaseHelper receiptDocumentForReleaseHelper = new ReceiptDocumentForReleaseHelper(
                    dataDefinitionService, resourceManagementService, userService, numberGeneratorService, translationService,
                    parameterService);

            boolean created = tryBuildPz(documentToCreateResourcesFor, receiptDocumentForReleaseHelper);

            if (created) {
                view.addMessage("materialFlow.document.info.createdConnectedPZ", MessageType.INFO);
            }
        }

        documentForm.setEntity(documentToCreateResourcesFor);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private boolean tryBuildPz(Entity documentToCreateResourcesFor,
            ReceiptDocumentForReleaseHelper receiptDocumentForReleaseHelper) {
        return receiptDocumentForReleaseHelper.tryBuildConnectedPZDocument(documentToCreateResourcesFor, true);
    }

    private boolean buildConnectedPZDocument(final Entity document) {
        if (document.getBooleanField(DocumentFields.CREATE_LINKED_PZ_DOCUMENT)
                && document.getBelongsToField(DocumentFields.LINKED_PZ_DOCUMENT_LOCATION) != null) {
            return true;
        }

        return false;
    }

    private void updatePositions(Entity document) {
        String query = "UPDATE materialflowresources_position "
                + "SET type = (SELECT type FROM materialflowresources_document WHERE id=:document_id), state = (SELECT state FROM materialflowresources_document WHERE id=:document_id) "
                + "WHERE document_id = :document_id ";

        Map<String, Object> params = Maps.newHashMap();

        params.put("document_id", document.getId());

        jdbcTemplate.update(query, params);
    }

    @Transactional
    public void createResources(Entity documentToCreateResourcesFor) {
        DocumentType documentType = DocumentType.of(documentToCreateResourcesFor);

        if (DocumentType.RECEIPT.equals(documentType) || DocumentType.INTERNAL_INBOUND.equals(documentType)) {
            resourceManagementService.createResourcesForReceiptDocuments(documentToCreateResourcesFor);
        } else if (DocumentType.INTERNAL_OUTBOUND.equals(documentType) || DocumentType.RELEASE.equals(documentType)) {
            resourceManagementService.updateResourcesForReleaseDocuments(documentToCreateResourcesFor);
        } else if (DocumentType.TRANSFER.equals(documentType)) {
            resourceManagementService.moveResourcesForTransferDocument(documentToCreateResourcesFor);
        } else {
            throw new IllegalStateException("Unsupported document type");
        }

        if (!documentToCreateResourcesFor.isValid()) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
    }

    public void clearWarehouseFields(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FieldComponent locationFromField = (FieldComponent) view.getComponentByReference(DocumentFields.LOCATION_FROM);
        locationFromField.setFieldValue(null);
        locationFromField.requestComponentUpdateState();

        FieldComponent locationToField = (FieldComponent) view.getComponentByReference(DocumentFields.LOCATION_TO);
        locationToField.setFieldValue(null);
        locationToField.requestComponentUpdateState();
    }

    public void updateAttributes(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        clearAttributes(view);
        createNewAttributes(view);
    }

    private void clearAttributes(final ViewDefinitionState view) {
        AwesomeDynamicListComponent positionsADL = (AwesomeDynamicListComponent) view.getComponentByReference(L_POSITIONS);

        for (FormComponent positionForm : positionsADL.getFormComponents()) {
            AwesomeDynamicListComponent attributeADL = (AwesomeDynamicListComponent) positionForm
                    .findFieldComponentByName("additionalAttributes");

            attributeADL.setFieldValue(Lists.newArrayList());
            attributeADL.requestComponentUpdateState();
        }
    }

    private void createNewAttributes(final ViewDefinitionState view) {
        AwesomeDynamicListComponent positionsADL = (AwesomeDynamicListComponent) view.getComponentByReference(L_POSITIONS);

        FormComponent documentForm = (FormComponent) view.getComponentByReference(L_FORM);

        Entity document = documentForm.getEntity();

        Entity warehouse = document.getBelongsToField(DocumentFields.LOCATION_TO);

        for (FormComponent positionForm : positionsADL.getFormComponents()) {
            Entity position = positionForm.getEntity();

            if (position.getId() != null) {
                position.setField(PositionFields.ATRRIBUTE_VALUES,
                        materialFlowResourcesService.getAttributesForPosition(position, warehouse));

                positionForm.setEntity(position);
            }
        }
    }

    public void refreshView(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent documentForm = (FormComponent) view.getComponentByReference(L_FORM);

        documentForm.performEvent(view, "refresh");
    }

    public void showAndSetRequiredForResourceLookup(final ViewDefinitionState view) {
        boolean visible = checkIfResourceLookupShouldBeVisible(view);

        showResourceLookupOrBatchInput(view, visible, false);
    }

    public void showAndSetRequiredForResourceLookup(final ViewDefinitionState view, final ComponentState state,
            final String[] args) {
        showAndSetRequiredForResourceLookup(view);
    }

    public void setCriteriaModifiersParameters(final ViewDefinitionState view, final ComponentState state, final String[] args) {

    }

    private void showResourceLookupOrBatchInput(final ViewDefinitionState view, boolean visible, boolean shouldClear) {
        AwesomeDynamicListComponent positionsADL = (AwesomeDynamicListComponent) view.getComponentByReference(L_POSITIONS);

        for (FormComponent positionForm : positionsADL.getFormComponents()) {
            FieldComponent resourceField = positionForm.findFieldComponentByName(L_RESOURCE);
            FieldComponent batchField = positionForm.findFieldComponentByName(L_BATCH);

            resourceField.setVisible(visible);
            resourceField.setRequired(visible);

            batchField.setVisible(!visible);

            if (shouldClear) {
                resourceField.setFieldValue(null);
            }
        }
    }

    private boolean checkIfResourceLookupShouldBeVisible(final ViewDefinitionState view) {
        FormComponent documentForm = (FormComponent) view.getComponentByReference(L_FORM);

        Entity document = documentForm.getPersistedEntityWithIncludedFormValues();
        Entity locationFrom = document.getBelongsToField(DocumentFields.LOCATION_FROM);

        DocumentState state = DocumentState.of(document);

        if (locationFrom != null) {
            DocumentType type = DocumentType.of(document);

            String algorithm = locationFrom.getStringField(LocationFieldsMFR.ALGORITHM);

            return algorithm.equalsIgnoreCase(WarehouseAlgorithm.MANUAL.getStringValue())
                    && ((DocumentType.RELEASE.equals(type)) || (DocumentType.TRANSFER.equals(type)))
                    && DocumentState.DRAFT.equals(state);
        }

        return false;
    }

    private boolean validateResourceAttribute(Entity document) {
        DocumentType type = DocumentType.of(document);

        if (DocumentType.TRANSFER.equals(type) || DocumentType.RELEASE.equals(type)) {
            Entity warehouseFrom = document.getBelongsToField(DocumentFields.LOCATION_FROM);
            String algorithm = warehouseFrom.getStringField(LocationFieldsMFR.ALGORITHM);

            boolean result = true;

            for (Entity position : document.getHasManyField(DocumentFields.POSITIONS)) {
                boolean resultForPosition = (algorithm.equalsIgnoreCase(WarehouseAlgorithm.MANUAL.getStringValue())
                        && position.getField(PositionFields.RESOURCE) != null)
                        || !algorithm.equalsIgnoreCase(WarehouseAlgorithm.MANUAL.getStringValue());
                if (!resultForPosition) {
                    result = false;

                    position.addError(position.getDataDefinition().getField(PositionFields.RESOURCE),
                            "materialFlow.error.position.batch.required");
                }
            }

            return result;
        }

        return true;
    }

    public void calculateQuantity(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        AwesomeDynamicListComponent positionsADL = (AwesomeDynamicListComponent) view.getComponentByReference(L_POSITIONS);

        for (FormComponent positionForm : positionsADL.getFormComponents()) {
            Entity position = positionForm.getPersistedEntityWithIncludedFormValues();

            String givenUnit = position.getStringField(PositionFields.GIVEN_UNIT);
            Entity product = position.getBelongsToField(PositionFields.PRODUCT);

            FieldComponent givenQuantityField = positionForm.findFieldComponentByName(PositionFields.GIVEN_QUANTITY);

            if (product == null || givenUnit == null || givenUnit.isEmpty() || givenQuantityField.getFieldValue() == null) {
                return;
            }

            Either<Exception, Optional<BigDecimal>> maybeQuantity = BigDecimalUtils
                    .tryParse((String) givenQuantityField.getFieldValue(), view.getLocale());

            if (maybeQuantity.isRight()) {
                if (maybeQuantity.getRight().isPresent()) {
                    BigDecimal givenQuantity = maybeQuantity.getRight().get();
                    String baseUnit = product.getStringField(ProductFields.UNIT);

                    if (baseUnit.equals(givenUnit)) {
                        position.setField(PositionFields.QUANTITY, givenQuantity);
                    } else {
                        PossibleUnitConversions unitConversions = unitConversionService.getPossibleConversions(givenUnit,
                                searchCriteriaBuilder -> searchCriteriaBuilder
                                        .add(SearchRestrictions.belongsTo(UnitConversionItemFieldsB.PRODUCT, product)));

                        if (unitConversions.isDefinedFor(baseUnit)) {
                            BigDecimal convertedQuantity = unitConversions.convertTo(givenQuantity, baseUnit);

                            position.setField(PositionFields.QUANTITY, convertedQuantity);
                        } else {
                            if (!givenQuantityField.isHasError()) {
                                position.addError(position.getDataDefinition().getField(PositionFields.GIVEN_QUANTITY),
                                        "materialFlowResources.position.validate.error.missingUnitConversion");
                            }

                            position.setField(PositionFields.QUANTITY, null);
                        }
                    }
                } else {
                    position.setField(PositionFields.QUANTITY, null);
                }
            } else {
                position.setField(PositionFields.QUANTITY, null);
            }

            String unit = product.getStringField(UNIT);

            position.setField(PositionFields.UNIT, unit);
            positionForm.setEntity(position);
        }
    }

    public void fillResources(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        Entity document = form.getPersistedEntityWithIncludedFormValues();
        document = resourceReservationsService.fillResourcesInDocument(document);
        form.setEntity(document);
        view.performEvent(view, "reset");
    }

}
