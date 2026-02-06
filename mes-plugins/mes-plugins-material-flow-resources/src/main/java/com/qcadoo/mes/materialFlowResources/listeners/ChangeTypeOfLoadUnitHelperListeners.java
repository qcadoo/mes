package com.qcadoo.mes.materialFlowResources.listeners;

import com.beust.jcommander.internal.Sets;
import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.constants.PalletNumberFields;
import com.qcadoo.mes.basic.constants.TypeOfLoadUnitFields;
import com.qcadoo.mes.materialFlowResources.PalletValidatorService;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.PalletStorageStateDtoFields;
import com.qcadoo.mes.materialFlowResources.constants.ResourceFields;
import com.qcadoo.mes.materialFlowResources.constants.StorageLocationFields;
import com.qcadoo.mes.materialFlowResources.service.ResourceCorrectionService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class ChangeTypeOfLoadUnitHelperListeners {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ResourceCorrectionService resourceCorrectionService;

    @Autowired
    private PalletValidatorService palletValidatorService;

    @Transactional
    public final void changeTypeOfLoadUnit(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity entity = form.getPersistedEntityWithIncludedFormValues();

        String typeOfLoadUnit = entity.getStringField(PalletStorageStateDtoFields.TYPE_OF_LOAD_UNIT);
        Entity newTypeOfLoadUnit = entity.getBelongsToField(PalletStorageStateDtoFields.NEW_TYPE_OF_LOAD_UNIT);
        String newTypeOfLoadUnitName = "";
        if (newTypeOfLoadUnit != null) {
            newTypeOfLoadUnitName = newTypeOfLoadUnit.getStringField(TypeOfLoadUnitFields.NAME);
        }
        if (typeOfLoadUnit.compareTo(newTypeOfLoadUnitName) == 0) {
            view.addMessage("materialFlowResources.changeTypeOfLoadUnitHelper.sameTypeOfLoadUnit", ComponentState.MessageType.FAILURE);
            return;
        }

        List<String> changedResources = Lists.newArrayList();
        List<String> failedResources = Lists.newArrayList();
        DataDefinition resourceDD = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_RESOURCE);
        List<Entity> resources = resourceDD.find().createAlias(ResourceFields.PALLET_NUMBER, ResourceFields.PALLET_NUMBER, JoinType.LEFT).
                add(SearchRestrictions.eq(ResourceFields.PALLET_NUMBER + "." + PalletNumberFields.NUMBER, entity.getStringField(PalletStorageStateDtoFields.PALLET_NUMBER))).list().getEntities();
        for (Entity resource : resources) {
            resource.setField(ResourceFields.TYPE_OF_LOAD_UNIT, newTypeOfLoadUnit);
            resource.setField(ResourceFields.VALIDATE_PALLET, false);
            String resourceNumber = resource.getStringField(ResourceFields.NUMBER);

            boolean corrected = resourceCorrectionService.createCorrectionForResource(resource, false).isPresent();
            if (corrected) {
                changedResources.add(resourceNumber);
            } else {
                failedResources.add(resourceNumber);
                resource.getErrors().forEach((key, message) -> view.addMessage(message));
                resource.getGlobalErrors().forEach(view::addMessage);
            }
        }
        if (!changedResources.isEmpty()) {
            view.addMessage("materialFlowResources.changeTypeOfLoadUnitHelper.success", ComponentState.MessageType.SUCCESS, false,
                    String.join(", ", changedResources));
        }
        if (!failedResources.isEmpty()) {
            view.addMessage("materialFlowResources.changeTypeOfLoadUnitHelper.error", ComponentState.MessageType.FAILURE, false,
                    String.join(", ", failedResources));
        }
        form.performEvent(view, "reset");
    }

}
