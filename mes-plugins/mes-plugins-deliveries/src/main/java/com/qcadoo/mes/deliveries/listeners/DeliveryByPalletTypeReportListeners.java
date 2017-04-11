package com.qcadoo.mes.deliveries.listeners;

import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.validators.ErrorMessage;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class DeliveryByPalletTypeReportListeners {

    public final void generateReport(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity entity = form.getPersistedEntityWithIncludedFormValues();
        if (Objects.isNull(entity.getDateField("fromDate"))) {
            FieldComponent dateFromField = (FieldComponent) view.getComponentByReference("fromDate");
            dateFromField.addMessage(new ErrorMessage("qcadooView.validate.field.error.missing", false));
            return;
        } else if (Objects.isNull(entity.getDateField("toDate"))) {
            FieldComponent dateFromField = (FieldComponent) view.getComponentByReference("toDate");
            dateFromField.addMessage(new ErrorMessage("qcadooView.validate.field.error.missing", false));
            return;
        }
        if (entity.getDateField("from").after(entity.getDateField("to"))) {
            FieldComponent dateFromField = (FieldComponent) view.getComponentByReference("from");
            dateFromField.addMessage(new ErrorMessage("deliveries.deliveryByPalletTypeReport.fromAfterTo", false));
            return;
        }


        StringBuilder redirectUrl = new StringBuilder();
        redirectUrl.append("/deliveries/deliveryByPalletType.xlsx");
        redirectUrl.append("?");
        redirectUrl.append("from=").append(entity.getDateField("fromDate").getTime());
        redirectUrl.append("&to=").append(entity.getDateField("toDate").getTime());
        view.redirectTo(redirectUrl.toString(), true, false);
    }
}
