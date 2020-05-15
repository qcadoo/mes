package com.qcadoo.mes.materialFlowResources.listeners;

import com.google.common.collect.Sets;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.PalletNumberFields;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.PalletStorageStateDtoFields;
import com.qcadoo.mes.materialFlowResources.constants.ResourceFields;
import com.qcadoo.mes.materialFlowResources.constants.StorageLocationFields;
import com.qcadoo.mes.materialFlowResources.service.PalletNumberDisposalService;
import com.qcadoo.mes.materialFlowResources.service.ResourceCorrectionService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.*;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static com.qcadoo.model.api.search.SearchRestrictions.eq;
import static com.qcadoo.view.api.ComponentState.MessageType.FAILURE;

@Service
public class PalletResourcesTransferHelperListeners {

    private static final String L_PALLET_STORAGE_STATE_DTOS = "palletStorageStateDtos";

    private static final String L_NEW_PALLET_NUMBER = "newPalletNumber";

    private static final String L_NEW_STORAGE_LOCATION = "newStorageLocation";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private PalletNumberDisposalService palletNumberDisposalService;

    @Autowired
    private ResourceCorrectionService resourceCorrectionService;

    private SearchCriterion typeOfPalletCriterion(Entity entity) {
        String typeOfPallet = entity.getStringField(PalletStorageStateDtoFields.TYPE_OF_PALLET);
        if (StringUtils.isBlank(typeOfPallet)) {
            return SearchRestrictions.isNull(ResourceFields.TYPE_OF_PALLET);
        } else {
            return eq(ResourceFields.TYPE_OF_PALLET, typeOfPallet);
        }
    }

    private SearchCriterion storageLocationCriterion(Entity entity) {
        String storageLocationNumber = entity.getStringField(PalletStorageStateDtoFields.STORAGE_LOCATION_NUMBER);
        if (StringUtils.isBlank(storageLocationNumber)) {
            return SearchRestrictions.isNull(ResourceFields.STORAGE_LOCATION + ".number");
        } else {
            return eq(ResourceFields.STORAGE_LOCATION + ".number", storageLocationNumber);
        }
    }

    @Transactional
    public void transferResources(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity helper = form.getPersistedEntityWithIncludedFormValues();
        CheckBoxComponent generated = (CheckBoxComponent) view.getComponentByReference("generated");
        List<Entity> dtos = helper.getHasManyField(L_PALLET_STORAGE_STATE_DTOS);
        if (!validate(view, dtos)) {
            generated.setChecked(false);
            return;
        }

        Set<Entity> palletNumbersToDispose = Sets.newHashSet();

        DataDefinition resourceDD = resourceDataDefinition();
        boolean hasErrors = false;
        for (Entity dto : dtos) {
            Entity selectedPallet = dto.getBelongsToField(PalletStorageStateDtoFields.NEW_PALLET_NUMBER);
            if (selectedPallet != null) {

                String oldPalletNumber = dto.getStringField(PalletStorageStateDtoFields.PALLET_NUMBER);

                final List<Entity> resources = resourceDD
                        .find()
                        .createAlias(ResourceFields.PALLET_NUMBER, ResourceFields.PALLET_NUMBER, JoinType.INNER)
                        .createAlias(ResourceFields.LOCATION, ResourceFields.LOCATION, JoinType.INNER)
                        .createAlias(ResourceFields.STORAGE_LOCATION, ResourceFields.STORAGE_LOCATION, JoinType.LEFT)
                        .add(eq(ResourceFields.PALLET_NUMBER + ".number",
                                dto.getStringField(PalletStorageStateDtoFields.PALLET_NUMBER)))
                        .add(eq(ResourceFields.LOCATION + ".number",
                                dto.getStringField(PalletStorageStateDtoFields.LOCATION_NUMBER)))
                        .add(storageLocationCriterion(dto)).add(typeOfPalletCriterion(dto)).list().getEntities();

                final Entity palletNumberEntity = findPalletNumberByNumber(selectedPallet
                        .getStringField(PalletStorageStateDtoFields.PALLET_NUMBER));
                final Entity storageLocationEntity = Optional
                        .ofNullable(selectedPallet.getStringField(PalletStorageStateDtoFields.STORAGE_LOCATION_NUMBER))
                        .map(this::findStorageLocationByNumber).orElse(null);

                boolean shouldDispose = true;
                for (Entity resource : resources) {
                    resource.setField(ResourceFields.PALLET_NUMBER, palletNumberEntity);
                    resource.setField(ResourceFields.STORAGE_LOCATION, storageLocationEntity);
                    resource.setField(ResourceFields.TYPE_OF_PALLET,
                            selectedPallet.getStringField(PalletStorageStateDtoFields.TYPE_OF_PALLET));
                    resource.setField(ResourceFields.VALIDATE_PALLET, false);
                    boolean corrected = resourceCorrectionService.createCorrectionForResource(resource, false).isPresent();
                    if (!corrected) {
                        resource.getGlobalErrors().forEach(view::addMessage);
                        view.addMessage("materialFlowResources.palletResourcesTransfer.resource.failure",
                                ComponentState.MessageType.FAILURE, false, resource.getStringField(ResourceFields.NUMBER));
                        hasErrors = true;
                        shouldDispose = false;
                    }
                }
                if (shouldDispose) {
                    palletNumbersToDispose.add(findPalletNumberByNumber(oldPalletNumber));

                }
            }
        }
        palletNumbersToDispose.forEach(pn -> palletNumberDisposalService.tryToDispose(pn));
        if (hasErrors) {
            view.addMessage("materialFlowResources.palletResourcesTransfer.partialSuccess", ComponentState.MessageType.INFO);
        } else {
            view.addMessage("materialFlowResources.palletResourcesTransfer.success", ComponentState.MessageType.SUCCESS);
        }
        generated.setChecked(true);
    }

    private Entity findStorageLocationByNumber(final String storageLocationNumber) {
        return Objects.requireNonNull(storageLocationDataDefinition().find()
                .add(eq(StorageLocationFields.NUMBER, storageLocationNumber)).uniqueResult());
    }

    private Entity findPalletNumberByNumber(final String palletNumber) {
        return Objects.requireNonNull(palletNumberDataDefinition().find().add(eq(PalletNumberFields.NUMBER, palletNumber))
                .uniqueResult());
    }

    private DataDefinition resourceDataDefinition() {
        return dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_RESOURCE);
    }

    private DataDefinition palletNumberDataDefinition() {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PALLET_NUMBER);
    }

    private DataDefinition storageLocationDataDefinition() {
        return dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_STORAGE_LOCATION);
    }

    private boolean validate(ViewDefinitionState view, List<Entity> dtos) {
        AwesomeDynamicListComponent adl = (AwesomeDynamicListComponent) view.getComponentByReference(L_PALLET_STORAGE_STATE_DTOS);
        boolean isValid = true;
        Set<String> ambiguousPalletNumbers = Sets.newHashSet();
        for (FormComponent form : adl.getFormComponents()) {
            LookupComponent newPalletNumber = (LookupComponent) form.findFieldComponentByName(L_NEW_PALLET_NUMBER);
            if (newPalletNumber.getFieldValue() == null) {
                newPalletNumber.addMessage("qcadooView.validate.field.error.missing", FAILURE);
                isValid = false;
                continue;
            }

            Entity newPalletNumberEntity = newPalletNumber.getEntity();
            if (isSelectedPalletNumberAmbiguous(newPalletNumberEntity, form)) {
                ambiguousPalletNumbers.add(newPalletNumberEntity.getStringField(PalletStorageStateDtoFields.PALLET_NUMBER));
                isValid = false;
            }
        }
        if (!ambiguousPalletNumbers.isEmpty()) {
            view.addMessage("materialFlowResources.palletResourcesTransferHelper.message.ambiguousPalletNumbers", FAILURE, false,
                    String.join(", ", ambiguousPalletNumbers));
        }
        return isValid;

    }

    private boolean isSelectedPalletNumberAmbiguous(Entity selectedPallet, FormComponent modifiedForm) {
        SearchResult searchResult = selectedPallet
                .getDataDefinition()
                .find()
                .add(eq(PalletStorageStateDtoFields.PALLET_NUMBER,
                        selectedPallet.getStringField(PalletStorageStateDtoFields.PALLET_NUMBER)))
                .add(eq(PalletStorageStateDtoFields.LOCATION_NUMBER,
                        modifiedForm.getEntity().getStringField(PalletStorageStateDtoFields.LOCATION_NUMBER))).list();
        return searchResult.getTotalNumberOfEntities() > 1;
    }

    public void onPalletNumberSelected(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        LookupComponent lookupComponent = (LookupComponent) state;
        Entity entity = lookupComponent.getEntity();

        if (null != entity) {

            AwesomeDynamicListComponent palletStorageStateDtos = (AwesomeDynamicListComponent) view
                    .getComponentByReference(L_PALLET_STORAGE_STATE_DTOS);

            for (FormComponent formComponent : palletStorageStateDtos.getFormComponents()) {
                LookupComponent newPalletNumber = (LookupComponent) formComponent.findFieldComponentByName(L_NEW_PALLET_NUMBER);
                if (newPalletNumber.getUuid().equals(state.getUuid())) {

                    if (isSelectedPalletNumberAmbiguous(entity, formComponent)) {
                        state.addMessage("materialFlowResources.palletResourcesTransferHelper.message.ambiguousPalletNumber",
                                FAILURE);
                    } else {
                        String storageLocationNumber = entity.getStringField(PalletStorageStateDtoFields.STORAGE_LOCATION_NUMBER);
                        FieldComponent newStorageLocationNumber = formComponent.findFieldComponentByName(L_NEW_STORAGE_LOCATION);
                        newStorageLocationNumber.setFieldValue(storageLocationNumber);
                        newStorageLocationNumber.requestComponentUpdateState();
                    }
                    break;
                }
            }
        }
    }

}
