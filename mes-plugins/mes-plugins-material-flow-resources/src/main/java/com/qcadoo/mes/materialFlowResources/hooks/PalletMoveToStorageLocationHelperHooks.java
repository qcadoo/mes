package com.qcadoo.mes.materialFlowResources.hooks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialFlow.constants.LocationFields;
import com.qcadoo.mes.materialFlow.constants.MaterialFlowConstants;
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
        DataDefinition locationDD = dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER,
                MaterialFlowConstants.MODEL_LOCATION);
        AwesomeDynamicListComponent adl = (AwesomeDynamicListComponent) view.getComponentByReference("palletStorageStateDtos");

        for (FormComponent form : adl.getFormComponents()) {
            LookupComponent newStorageLocation = (LookupComponent) form.findFieldComponentByName("newStorageLocation");
            FilterValueHolder filter = newStorageLocation.getFilterValue();
            Entity dto = form.getPersistedEntityWithIncludedFormValues();

            String locationNumber = dto.getStringField("locationNumber");
            Entity location = locationDD.find().add(SearchRestrictions.eq(LocationFields.NUMBER, locationNumber))
                    .setMaxResults(1).uniqueResult();
            filter.put(StorageLocationFields.LOCATION, location.getId());
            newStorageLocation.setFilterValue(filter);

        }

    }
}
