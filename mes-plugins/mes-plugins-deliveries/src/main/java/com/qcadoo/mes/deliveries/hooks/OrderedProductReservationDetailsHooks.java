package com.qcadoo.mes.deliveries.hooks;

import com.google.common.collect.Lists;
import com.qcadoo.mes.deliveries.DeliveriesService;
import com.qcadoo.mes.deliveries.constants.OrderedProductFields;
import com.qcadoo.mes.deliveries.constants.OrderedProductReservationFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderedProductReservationDetailsHooks {

    @Autowired
    private DeliveriesService deliveriesService;

    public void onBeforeRender(final ViewDefinitionState view) {
        List<String> referenceNames = Lists.newArrayList("orderedQuantityUnit");
        List<String> additionalUnitNames = Lists.newArrayList("additionalQuantityUnit");

        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity orderedProductReservation = form.getPersistedEntityWithIncludedFormValues();
        Entity orderedProduct = orderedProductReservation.getBelongsToField(OrderedProductReservationFields.ORDERED_PRODUCT);
        Entity product = orderedProduct.getBelongsToField(OrderedProductFields.PRODUCT);
        
        deliveriesService.fillUnitFields(view, product, referenceNames, additionalUnitNames);
    }    
}
