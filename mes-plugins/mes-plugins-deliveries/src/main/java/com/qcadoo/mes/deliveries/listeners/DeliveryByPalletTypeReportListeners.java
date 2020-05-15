package com.qcadoo.mes.deliveries.listeners;

import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.validators.ErrorMessage;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class DeliveryByPalletTypeReportListeners {

    

    private static final String L_FROM_DATE = "fromDate";

    private static final String L_TO_DATE = "toDate";

    public final void generateReport(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity entity = form.getPersistedEntityWithIncludedFormValues();

        if (!validate(view, entity)) {
            return;
        }

        StringBuilder redirectUrl = new StringBuilder();
        redirectUrl.append("/deliveries/deliveryByPalletType.xlsx");
        redirectUrl.append("?");
        redirectUrl.append("from=").append(entity.getDateField(L_FROM_DATE).getTime());
        redirectUrl.append("&to=").append(entity.getDateField(L_TO_DATE).getTime());

        view.redirectTo(redirectUrl.toString(), true, false);
    }

    private boolean validate(final ViewDefinitionState view, final Entity entity) {
        if (Objects.isNull(entity.getDateField(L_FROM_DATE))) {
            FieldComponent dateFromField = (FieldComponent) view.getComponentByReference(L_FROM_DATE);

            dateFromField.addMessage(new ErrorMessage("qcadooView.validate.field.error.missing", false));

            return false;
        } else if (Objects.isNull(entity.getDateField(L_TO_DATE))) {
            FieldComponent dateFromField = (FieldComponent) view.getComponentByReference(L_TO_DATE);

            dateFromField.addMessage(new ErrorMessage("qcadooView.validate.field.error.missing", false));

            return false;
        }
        if (entity.getDateField(L_FROM_DATE).after(entity.getDateField(L_TO_DATE))) {
            FieldComponent dateFromField = (FieldComponent) view.getComponentByReference(L_FROM_DATE);

            dateFromField.addMessage(new ErrorMessage("deliveries.deliveryByPalletTypeReport.fromAfterTo", false));

            return false;
        }

        return true;
    }

}
