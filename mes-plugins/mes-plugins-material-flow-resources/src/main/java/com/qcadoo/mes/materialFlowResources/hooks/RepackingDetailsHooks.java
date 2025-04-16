package com.qcadoo.mes.materialFlowResources.hooks;

import com.qcadoo.mes.materialFlowResources.constants.RepackingFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class RepackingDetailsHooks {

    public void onBeforeRender(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity repacking = form.getPersistedEntityWithIncludedFormValues();
        setStorageLocationLookupFilterValue(view, repacking);
    }

    private void setStorageLocationLookupFilterValue(final ViewDefinitionState view, final Entity repacking) {
        LookupComponent storageLocationLookup = (LookupComponent) view.getComponentByReference(RepackingFields.STORAGE_LOCATION);

        FilterValueHolder filter = storageLocationLookup.getFilterValue();

        Entity warehouse = repacking.getBelongsToField(RepackingFields.LOCATION);

        if (Objects.nonNull(warehouse)) {
            filter.put(RepackingFields.LOCATION, warehouse.getId());
        }

        storageLocationLookup.setFilterValue(filter);
    }

}
