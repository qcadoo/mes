package com.qcadoo.mes.deliveries.hooks;

import com.google.common.collect.Lists;
import com.qcadoo.mes.deliveries.DeliveriesService;
import com.qcadoo.mes.deliveries.constants.DeliveredProductFields;
import com.qcadoo.mes.deliveries.constants.DeliveredProductReservationFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DeliveredProductReservationDetailsHooks {

    @Autowired
    private DeliveriesService deliveriesService;

    public void onBeforeRender(final ViewDefinitionState view) {
        List<String> referenceNames = Lists.newArrayList("deliveredQuantityUnit");
        List<String> additionalUnitNames = Lists.newArrayList("additionalQuantityUnit");

        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity deliveredProductReservation = form.getPersistedEntityWithIncludedFormValues();
        Entity deliveredProduct = deliveredProductReservation.getBelongsToField(DeliveredProductReservationFields.DELIVERED_PRODUCT);
        Entity product = deliveredProduct.getBelongsToField(DeliveredProductFields.PRODUCT);

        deliveriesService.fillUnitFields(view, product, referenceNames, additionalUnitNames);
    }
}
