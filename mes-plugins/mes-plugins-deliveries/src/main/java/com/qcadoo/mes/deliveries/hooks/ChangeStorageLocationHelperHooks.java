package com.qcadoo.mes.deliveries.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.deliveries.constants.DeliveredProductFields;
import com.qcadoo.mes.deliveries.constants.DeliveryFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class ChangeStorageLocationHelperHooks {

    public void onBeforeRender(final ViewDefinitionState view) {

        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity entity = form.getEntity();
        Entity delivery = entity.getBelongsToField(DeliveredProductFields.DELIVERY);
        Entity location = delivery.getBelongsToField(DeliveryFields.LOCATION);
        if (location != null) {
            LookupComponent storageLocationLookup = (LookupComponent) view.getComponentByReference("storageLocation");
            FilterValueHolder filter = storageLocationLookup.getFilterValue();
            filter.put("location", location.getId());
            storageLocationLookup.setFilterValue(filter);
        }
    }

}
