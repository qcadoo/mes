package com.qcadoo.mes.materialFlowResources.listeners;

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
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity helper = form.getPersistedEntityWithIncludedFormValues();
        Entity document = getDocumentDD().get(helper.getLongField(L_DOCUMENT_ID));

        List<String> errorNumbers = Lists.newArrayList();

        for (Long resourceId : selectedEntities) {
            Entity resource = getResourceDD().get(resourceId);

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
        newPosition.setField(PositionFields.ADDITIONAL_CODE, resource.getField(ResourceFields.ADDITIONAL_CODE));
        newPosition.setField(PositionFields.CONVERSION, conversion);
        newPosition.setField(PositionFields.PALLET_NUMBER, resource.getField(ResourceFields.PALLET_NUMBER));
        newPosition.setField(PositionFields.TYPE_OF_PALLET, resource.getField(ResourceFields.TYPE_OF_PALLET));
        newPosition.setField(PositionFields.WASTE, resource.getField(ResourceFields.WASTE));

        return positionDD.save(newPosition);
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
