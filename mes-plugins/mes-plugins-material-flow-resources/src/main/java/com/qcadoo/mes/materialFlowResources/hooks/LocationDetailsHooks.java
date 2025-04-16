package com.qcadoo.mes.materialFlowResources.hooks;

import com.qcadoo.mes.materialFlowResources.constants.LocationFieldsMFR;
import com.qcadoo.mes.materialFlowResources.constants.StorageLocationFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class LocationDetailsHooks {

    public void onBeforeRender(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        LookupComponent transferStorageLocationLookup = (LookupComponent) view.getComponentByReference(LocationFieldsMFR.TRANSFER_STORAGE_LOCATION);

        FilterValueHolder filter = transferStorageLocationLookup.getFilterValue();

        Entity location = form.getPersistedEntityWithIncludedFormValues();

        if (Objects.nonNull(location.getId())) {
            filter.put(StorageLocationFields.LOCATION, location.getId());
        }

        transferStorageLocationLookup.setFilterValue(filter);
    }
}
