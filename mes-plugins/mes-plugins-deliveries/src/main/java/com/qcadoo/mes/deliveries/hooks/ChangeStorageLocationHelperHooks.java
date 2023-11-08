package com.qcadoo.mes.deliveries.hooks;

import com.qcadoo.mes.deliveries.constants.DeliveredProductFields;
import com.qcadoo.mes.deliveries.constants.DeliveryFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class ChangeStorageLocationHelperHooks {

    public void onBeforeRender(final ViewDefinitionState view) {
        FormComponent deliveredProductForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity deliveredProduct = deliveredProductForm.getEntity();

        Entity delivery = deliveredProduct.getBelongsToField(DeliveredProductFields.DELIVERY);
        Entity location = delivery.getBelongsToField(DeliveryFields.LOCATION);

        if (Objects.nonNull(location)) {
            LookupComponent storageLocationLookup = (LookupComponent) view.getComponentByReference(DeliveredProductFields.STORAGE_LOCATION);

            FilterValueHolder filter = storageLocationLookup.getFilterValue();

            filter.put("location", location.getId());

            storageLocationLookup.setFilterValue(filter);
        }
    }

}
