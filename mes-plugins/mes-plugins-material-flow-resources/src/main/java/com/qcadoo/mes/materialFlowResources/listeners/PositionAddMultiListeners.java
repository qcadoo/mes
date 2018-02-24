package com.qcadoo.mes.materialFlowResources.listeners;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.PositionFields;
import com.qcadoo.mes.materialFlowResources.constants.ResourceFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class PositionAddMultiListeners {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    public void addPositions(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent grid = (GridComponent) view.getComponentByReference("resourceGrid");
        Set<Long> selectedEntities = grid.getSelectedEntitiesIds();
        CheckBoxComponent generated = (CheckBoxComponent) view.getComponentByReference("generated");
        if (selectedEntities.isEmpty()) {
            generated.setChecked(false);
            view.addMessage("materialFlowResources.positionAddMulti.noSelectedResources", ComponentState.MessageType.INFO);
            return;
        }

        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity helper = form.getPersistedEntityWithIncludedFormValues();

        Entity document = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_DOCUMENT).get(helper.getLongField("documentId"));
        List<String> errorNumbers = Lists.newArrayList();
        for (Long resourceId : selectedEntities) {
            Entity resource = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                    MaterialFlowResourcesConstants.MODEL_RESOURCE).get(resourceId);
            Entity newPosition = createPosition(document, resource);
            if (!newPosition.isValid()) {
                errorNumbers.add(resource.getStringField(ResourceFields.NUMBER));
            }
        }
        if (!errorNumbers.isEmpty()) {
            view.addMessage("materialFlowResources.positionAddMulti.errorForResource", ComponentState.MessageType.INFO,
                    errorNumbers.stream().collect(Collectors.joining(", ")));

        }
        generated.setChecked(true);

    }

    private Entity createPosition(final Entity document, final Entity resource) {
        DataDefinition positionDD = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_POSITION);
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
        newPosition.setField(PositionFields.ADDITIONAL_CODE, resource.getField(ResourceFields.ADDITIONAL_CODE));
        newPosition.setField(PositionFields.CONVERSION, conversion);
        newPosition.setField(PositionFields.PALLET_NUMBER, resource.getField(ResourceFields.PALLET_NUMBER));
        newPosition.setField(PositionFields.TYPE_OF_PALLET, resource.getField(ResourceFields.TYPE_OF_PALLET));
        newPosition.setField(PositionFields.WASTE, resource.getField(ResourceFields.WASTE));
        return positionDD.save(newPosition);

    }
}
