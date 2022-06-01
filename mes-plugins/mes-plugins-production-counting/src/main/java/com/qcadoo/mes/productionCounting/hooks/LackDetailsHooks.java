package com.qcadoo.mes.productionCounting.hooks;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.productionCounting.constants.ProductionTrackingFields;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductOutComponentFields;
import com.qcadoo.mes.productionCounting.states.constants.ProductionTrackingStateStringValues;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.AwesomeDynamicListComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.stereotype.Service;

@Service
public class LackDetailsHooks {

    public static final String L_UNIT = "unit";
    public static final String TRACKING_OPERATION_PRODUCT_OUT_COMPONENT = "trackingOperationProductOutComponent";
    public static final String L_LACK_REASONS = "lackReasons";

    public void onBeforeRender(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity lack = form.getEntity();
        Entity tooc = lack.getBelongsToField(TRACKING_OPERATION_PRODUCT_OUT_COMPONENT);
        if(view.isViewAfterRedirect()) {
            FieldComponent unitField = (FieldComponent) view.getComponentByReference(L_UNIT);
            Entity product = tooc.getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCT);
            String unit = product.getStringField(ProductFields.UNIT);
            unitField.setFieldValue(unit);
            unitField.requestComponentUpdateState();
        }

        Entity productionTracking = tooc.getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCTION_TRACKING);
        if(!productionTracking.getStringField(ProductionTrackingFields.STATE).equals(ProductionTrackingStateStringValues.DRAFT)) {
            AwesomeDynamicListComponent lackReasons = (AwesomeDynamicListComponent) view.getComponentByReference(L_LACK_REASONS);
            lackReasons.setEnabled(false);
            for (FormComponent formLackReason : lackReasons.getFormComponents()) {
                formLackReason.setFormEnabled(false);
            }
        }
    }
}
