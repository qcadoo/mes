package com.qcadoo.mes.materialFlowResources.listeners;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.materialFlowResources.constants.*;
import com.qcadoo.model.api.*;
import com.qcadoo.model.api.exception.EntityRuntimeException;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class PositionAddMultiListeners {

    private static final String L_RESOURCE_GRID = "resourceGrid";

    private static final String L_GENERATED = "generated";

    private static final String L_DOCUMENT_ID = "documentId";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    public void addPositions(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent resourceGrid = (GridComponent) view.getComponentByReference(L_RESOURCE_GRID);
        CheckBoxComponent generated = (CheckBoxComponent) view.getComponentByReference(L_GENERATED);

        Set<Long> selectedEntities = resourceGrid.getSelectedEntitiesIds();

        if (selectedEntities.isEmpty()) {
            generated.setChecked(false);

            view.addMessage("materialFlowResources.positionAddMulti.noSelectedResources", ComponentState.MessageType.INFO);

            return;
        }

        tryCreatePositions(view, selectedEntities);

        generated.setChecked(true);
    }

    public void tryCreatePositions(ViewDefinitionState view, Set<Long> selectedEntities) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity helper = form.getPersistedEntityWithIncludedFormValues();
        Entity document = getDocumentDD().get(helper.getLongField(L_DOCUMENT_ID));

        List<String> errorNumbers = Lists.newArrayList();

        for (Long resourceId : selectedEntities) {
            Entity resource = getResourceDD().get(resourceId);

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
        newPosition.setField(PositionFields.TYPE_OF_PALLET, resource.getField(ResourceFields.TYPE_OF_PALLET));
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

    private DataDefinition getResourceDD() {
        return dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_RESOURCE);
    }

    private DataDefinition getDocumentDD() {
        return dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_DOCUMENT);
    }

    private DataDefinition getPositionDD() {
        return dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_POSITION);
    }

}
