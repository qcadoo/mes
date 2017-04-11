package com.qcadoo.mes.deliveries.hooks;

import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class DeliveryByPalletTypeReportHooks {

    public void onBeforeRender(final ViewDefinitionState view) {

        if (view.isViewAfterRedirect()) {
            FieldComponent dateToField = (FieldComponent) view.getComponentByReference("to");
            dateToField.setFieldValue(DateUtils.toDateTimeString(new Date()));
            dateToField.requestComponentUpdateState();
        }

    }
}
