package com.qcadoo.mes.productFlowThruDivision.hooks;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.materialFlowResources.constants.ResourceFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class OrderProductResourceReservationRegisterDetailsHooks {

    @Autowired
    private NumberService numberService;

    public void onBeforeRender(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity trackingProductResourceReservation = form.getPersistedEntityWithIncludedFormValues();
        Entity orderProductResourceReservation = trackingProductResourceReservation.getBelongsToField("orderProductResourceReservation");
        Entity resource = orderProductResourceReservation.getBelongsToField("resource");
        Entity pcq = orderProductResourceReservation.getBelongsToField("productionCountingQuantity");
        Entity product = pcq.getBelongsToField(ProductionCountingQuantityFields.PRODUCT);

        ComponentState resourceLookup = view.getComponentByReference("resource");
        ComponentState resourcePlannedQuantity = view.getComponentByReference("planedQuantity");
        ComponentState resourcePlannedQuantityUnit = view.getComponentByReference("planedQuantityUnit");
        ComponentState usedQuantityUnit = view.getComponentByReference("usedQuantityUnit");

        resourceLookup.setFieldValue(resource.getStringField(ResourceFields.NUMBER));
        resourcePlannedQuantity.setFieldValue(numberService.format(orderProductResourceReservation.getDecimalField("planedQuantity")));
        resourcePlannedQuantityUnit.setFieldValue(product.getStringField(ProductFields.UNIT));
        usedQuantityUnit.setFieldValue(product.getStringField(ProductFields.UNIT));

    }

}
