package com.qcadoo.mes.materialFlowResources.listeners;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.materialFlowResources.constants.*;
import com.qcadoo.mes.materialFlowResources.exceptions.DocumentBuildException;
import com.qcadoo.mes.materialFlowResources.service.DocumentBuilder;
import com.qcadoo.mes.materialFlowResources.service.DocumentManagementService;
import com.qcadoo.model.api.*;
import com.qcadoo.model.api.exception.EntityRuntimeException;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.validators.ErrorMessage;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.security.constants.QcadooSecurityConstants;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PalletLoadUnitsTransferHelperListeners {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private DocumentManagementService documentManagementService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private NumberService numberService;

    @Transactional
    public void transferLoadUnits(final ViewDefinitionState view, final ComponentState state, final String[] args) throws JSONException {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity entity = form.getPersistedEntityWithIncludedFormValues();
        DataDefinition resourceDD = resourceDataDefinition();
        JSONObject context = view.getJsonContext();
        Set<Long> palletIds = Arrays.stream(
                        context.getString("window.mainTab.form.gridLayout.selectedEntities").replaceAll("[\\[\\]]", "").split(","))
                .map(Long::valueOf).collect(Collectors.toSet());
        List<Entity> resources = resourceDD
                .find()
                .createAlias(ResourceFields.PALLET_NUMBER, ResourceFields.PALLET_NUMBER, JoinType.INNER)
                .add(SearchRestrictions.in(ResourceFields.PALLET_NUMBER + ".id",
                        palletIds)).list().getEntities();
        Entity locationFrom = resources.get(0).getBelongsToField(ResourceFields.LOCATION);
        Entity locationTo = entity.getBelongsToField(DocumentFields.LOCATION_TO);

        FieldComponent locationToField = (FieldComponent) view.getComponentByReference(DocumentFields.LOCATION_TO);
        if (locationTo == null) {
            locationToField.addMessage(new ErrorMessage("qcadooView.validate.field.error.missing", false));
            return;
        }
        if (locationFrom.getId().equals(locationTo.getId())) {
            view.addMessage("materialFlow.error.document.warehouse.sameForTransfer", ComponentState.MessageType.FAILURE);

            return;
        }
        Entity user = dataDefinitionService.get(QcadooSecurityConstants.PLUGIN_IDENTIFIER, QcadooSecurityConstants.MODEL_USER)
                .get(securityService.getCurrentUserId());

        DocumentBuilder documentBuilder = documentManagementService.getDocumentBuilder(user);
        documentBuilder.transfer(locationTo, locationFrom);



        try {
            Entity document = documentBuilder.buildWithEntityRuntimeException();
            tryCreatePositions(document, view, resources);
            document = document.getDataDefinition().get(document.getId());
            redirectToCreatedDocument(document, view);
        } catch (DocumentBuildException exc) {
            exc.getGlobalErrors().forEach(errorMessage -> {
                if (!errorMessage.getMessage().equals("qcadooView.validate.global.error.custom")) {
                    view.addMessage(errorMessage.getMessage(), ComponentState.MessageType.FAILURE);
                }
            });
        }
    }

    private void tryCreatePositions(Entity document, ViewDefinitionState view, List<Entity> resources) {
        List<String> errorNumbers = Lists.newArrayList();

        for (Entity resource : resources) {
            try {
                Entity newPosition = createPosition(document, resource);
                if (!newPosition.isValid()) {
                    errorNumbers.add(resource.getStringField(ResourceFields.NUMBER));
                }
            } catch (EntityRuntimeException ere) {
                Entity pos = ere.getEntity();
                view.addMessage("documentPositions.error.position.quantity.notEnoughResources",
                        ComponentState.MessageType.FAILURE,
                        pos.getBelongsToField(PositionFields.PRODUCT).getStringField(ProductFields.NUMBER),
                        pos.getBelongsToField(PositionFields.RESOURCE).getStringField(ResourceFields.NUMBER));
            }

        }

        if (!errorNumbers.isEmpty()) {
            view.addMessage("materialFlowResources.positionAddMulti.errorForResource", ComponentState.MessageType.INFO,
                    String.join(", ", errorNumbers));
        }
    }

    private Entity createPosition(final Entity document, final Entity resource) {
        DataDefinition positionDD = getPositionDD();

        Entity newPosition = positionDD.create();

        BigDecimal conversion = resource.getDecimalField(ResourceFields.CONVERSION);
        BigDecimal availableQuantity = resource.getDecimalField(ResourceFields.AVAILABLE_QUANTITY);
        BigDecimal givenQuantity = availableQuantity.multiply(conversion, numberService.getMathContext());

        newPosition.setField(PositionFields.DOCUMENT, document);
        newPosition.setField(PositionFields.PRODUCT, resource.getBelongsToField(ResourceFields.PRODUCT));
        newPosition.setField(PositionFields.QUANTITY, availableQuantity);
        newPosition.setField(PositionFields.GIVEN_QUANTITY, givenQuantity);
        newPosition.setField(PositionFields.GIVEN_UNIT, resource.getStringField(ResourceFields.GIVEN_UNIT));
        newPosition.setField(PositionFields.PRICE, resource.getField(ResourceFields.PRICE));
        newPosition.setField(PositionFields.BATCH, resource.getField(ResourceFields.BATCH));
        newPosition.setField(PositionFields.PRODUCTION_DATE, resource.getField(ResourceFields.PRODUCTION_DATE));
        newPosition.setField(PositionFields.EXPIRATION_DATE, resource.getField(ResourceFields.EXPIRATION_DATE));
        newPosition.setField(PositionFields.RESOURCE, resource);
        newPosition.setField(PositionFields.RESOURCE_NUMBER, resource.getStringField(ResourceFields.NUMBER));
        newPosition.setField(PositionFields.STORAGE_LOCATION, resource.getField(ResourceFields.STORAGE_LOCATION));
        newPosition.setField(PositionFields.CONVERSION, conversion);
        newPosition.setField(PositionFields.PALLET_NUMBER, resource.getField(ResourceFields.PALLET_NUMBER));
        newPosition.setField(PositionFields.TYPE_OF_LOAD_UNIT, resource.getField(ResourceFields.TYPE_OF_LOAD_UNIT));
        newPosition.setField(PositionFields.WASTE, resource.getField(ResourceFields.WASTE));

        if (!validateAvailableQuantity(document, newPosition)) {
            throw new EntityRuntimeException(newPosition);
        }
        return positionDD.save(newPosition);
    }

    private boolean validateAvailableQuantity(Entity document, Entity position) {
        String type = document.getStringField(DocumentFields.TYPE);
        if (DocumentType.isOutbound(type)) {
            Entity location = document.getBelongsToField(DocumentFields.LOCATION_FROM);
            boolean enabled = location.getBooleanField(LocationFieldsMFR.DRAFT_MAKES_RESERVATION);
            if (enabled) {
                BigDecimal availableQuantity = getAvailableQuantityForProductAndLocation(
                        position.getBelongsToField(PositionFields.PRODUCT), location);
                BigDecimal quantity = position.getDecimalField(PositionFields.QUANTITY);
                if (availableQuantity == null || quantity.compareTo(availableQuantity) > 0) {
                    return false;
                } else if (Objects.nonNull(position.getBelongsToField(PositionFields.RESOURCE))) {
                    BigDecimal resourceAvailableQuantity = getAvailableQuantityForResource(
                            position.getBelongsToField(PositionFields.RESOURCE));
                    return resourceAvailableQuantity != null && quantity.compareTo(resourceAvailableQuantity) <= 0;
                }
            }
        }
        return true;
    }

    private BigDecimal getAvailableQuantityForResource(Entity resource) {
        return resource.getDecimalField(ResourceFields.AVAILABLE_QUANTITY);
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

    private void redirectToCreatedDocument(Entity document, ViewDefinitionState view) {
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("form.id", document.getId());

        String url = "/page/materialFlowResources/documentDetails.html";
        view.redirectTo(url, false, true, parameters);
    }

    private DataDefinition resourceDataDefinition() {
        return dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_RESOURCE);
    }

    private DataDefinition getPositionDD() {
        return dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_POSITION);
    }

}
