package com.qcadoo.mes.materialFlowResources.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialFlowResources.constants.ResourceFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class ResourceDetailsHooks {

    public void onBeforeRender(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity resource = form.getEntity();
        LookupComponent storageLocationLookup = (LookupComponent) view.getComponentByReference(ResourceFields.STORAGE_LOCATION);
        FilterValueHolder filter = storageLocationLookup.getFilterValue();
        Entity product = resource.getBelongsToField(ResourceFields.PRODUCT);
        Entity warehouse = resource.getBelongsToField(ResourceFields.LOCATION);
        filter.put("product", product.getId());
        filter.put("location", warehouse.getId());
        storageLocationLookup.setFilterValue(filter);
    }
}
