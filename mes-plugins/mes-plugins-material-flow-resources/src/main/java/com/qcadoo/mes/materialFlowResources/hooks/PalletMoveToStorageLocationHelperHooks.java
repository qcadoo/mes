package com.qcadoo.mes.materialFlowResources.hooks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Strings;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.StorageLocationFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.AwesomeDynamicListComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class PalletMoveToStorageLocationHelperHooks extends PalletStorageStateHooks {

    @Autowired
    public PalletMoveToStorageLocationHelperHooks(DataDefinitionService dataDefinitionService) {
        super(dataDefinitionService);
    }

    @Override
    protected void setStorageLocationFilters(final ViewDefinitionState view) {
        DataDefinition storageLocationDD = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_STORAGE_LOCATION);
        AwesomeDynamicListComponent adl = (AwesomeDynamicListComponent) view.getComponentByReference("palletStorageStateDtos");

        for (FormComponent form : adl.getFormComponents()) {
            LookupComponent newStorageLocation = (LookupComponent) form.findFieldComponentByName("newStorageLocation");
            FilterValueHolder filter = newStorageLocation.getFilterValue();
            Entity dto = form.getPersistedEntityWithIncludedFormValues();
            String oldStorageLocationNumber = dto.getStringField("storageLocationNumber");
            if (!Strings.isNullOrEmpty(oldStorageLocationNumber)) {
                Entity oldStorageLocation = storageLocationDD.find()
                        .add(SearchRestrictions.eq(StorageLocationFields.NUMBER, oldStorageLocationNumber)).setMaxResults(1)
                        .uniqueResult();
                Entity location = oldStorageLocation.getBelongsToField(StorageLocationFields.LOCATION);
                filter.put(StorageLocationFields.LOCATION, location.getId());
                newStorageLocation.setFilterValue(filter);
            }

        }
    }
}
