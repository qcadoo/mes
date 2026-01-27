package com.qcadoo.mes.materialFlowResources.listeners;

import com.beust.jcommander.internal.Sets;
import com.google.common.collect.Lists;
import com.qcadoo.mes.materialFlowResources.PalletValidatorService;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
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
public class ChangeStorageLocationHelperListenersMFR {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ResourceCorrectionService resourceCorrectionService;

    @Autowired
    private PalletValidatorService palletValidatorService;

    @Transactional
    public final void changeStorageLocation(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity entity = form.getEntity();
        String ids = entity.getStringField("resourceIds");

        String[] splitIds = ids.split(",");
        DataDefinition resourceDD = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_RESOURCE);
        Entity storageLocation = entity.getBelongsToField(ResourceFields.STORAGE_LOCATION);
        if (storageLocation == null) {
            view.addMessage("materialFlowResources.changeStorageLocationHelper.storageLocationRequired", ComponentState.MessageType.FAILURE);
            return;
        }
        List<String> changedResources = Lists.newArrayList();
        List<String> failedResources = Lists.newArrayList();
        List<String> omittedResources = Lists.newArrayList();
        Set<Long> palletNumbers = Sets.newHashSet();
        Set<Entity> resources = Sets.newHashSet();
        for (String id : splitIds) {
            Long resourceId = Long.parseLong(id);
            Entity resource = resourceDD.get(resourceId);
            Entity actualStorageLocation = resource.getBelongsToField(ResourceFields.STORAGE_LOCATION);
            Long actualStorageLocationId = null;
            if (actualStorageLocation != null) {
                actualStorageLocationId = actualStorageLocation.getId();
            }
            if (actualStorageLocationId == null || storageLocation.getId().compareTo(actualStorageLocationId) != 0) {
                Entity palletNumber = resource.getBelongsToField(ResourceFields.PALLET_NUMBER);
                if (palletNumber != null) {
                    palletNumbers.add(palletNumber.getId());
                }

                resources.add(resource);
            } else {
                omittedResources.add(resource.getStringField(ResourceFields.NUMBER));
            }
            if (storageLocation.getBooleanField(StorageLocationFields.PLACE_STORAGE_LOCATION) && Objects.isNull(resource.getBelongsToField(ResourceFields.PALLET_NUMBER))) {
                view.addMessage("materialFlowResources.changeStorageLocationHelper.palletNumberRequired", ComponentState.MessageType.FAILURE, false, resource.getStringField(ResourceFields.NUMBER));

                return;
            }
        }
        if (!palletNumbers.isEmpty()) {
            resources.addAll(resourceDD.find().createAlias(ResourceFields.PALLET_NUMBER, ResourceFields.PALLET_NUMBER, JoinType.LEFT).
                    add(SearchRestrictions.in(ResourceFields.PALLET_NUMBER + ".id", palletNumbers)).list().getEntities());
        }
        if (palletValidatorService.checkMaximumNumberOfPallets(storageLocation, palletNumbers.size())) {
            view.addMessage("materialFlowResources.storageLocation.maximumNumberOfPallets.toManyPallets", ComponentState.MessageType.FAILURE);

            return;
        }
        for (Entity resource : resources) {
            resource.setField(ResourceFields.STORAGE_LOCATION, storageLocation);
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
            view.addMessage("materialFlowResources.changeStorageLocationHelper.success", ComponentState.MessageType.SUCCESS, false,
                    String.join(", ", changedResources));
        }
        if (!failedResources.isEmpty()) {
            view.addMessage("materialFlowResources.changeStorageLocationHelper.error", ComponentState.MessageType.FAILURE, false,
                    String.join(", ", failedResources));
        }
        if (!omittedResources.isEmpty()) {
            view.addMessage("materialFlowResources.changeStorageLocationHelper.info", ComponentState.MessageType.INFO, false,
                    String.join(", ", omittedResources));
        }
    }

}
