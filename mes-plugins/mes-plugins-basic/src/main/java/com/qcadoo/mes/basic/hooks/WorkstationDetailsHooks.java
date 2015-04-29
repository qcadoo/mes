package com.qcadoo.mes.basic.hooks;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class WorkstationDetailsHooks {

    private static final String L_FORM = "form";

    public void setWorkstationIdForMultiUploadField(final ViewDefinitionState view) {
        FormComponent workstationForm = (FormComponent) view.getComponentByReference(L_FORM);
        FieldComponent workstationIdForMultiUpload = (FieldComponent) view.getComponentByReference("workstationIdForMultiUpload");
        FieldComponent workstationMultiUploadLocale = (FieldComponent) view
                .getComponentByReference("workstationMultiUploadLocale");

        if (workstationForm.getEntityId() != null) {
            workstationIdForMultiUpload.setFieldValue(workstationForm.getEntityId());
            workstationIdForMultiUpload.requestComponentUpdateState();
        } else {
            workstationIdForMultiUpload.setFieldValue("");
            workstationIdForMultiUpload.requestComponentUpdateState();
        }
        workstationMultiUploadLocale.setFieldValue(LocaleContextHolder.getLocale());
        workstationMultiUploadLocale.requestComponentUpdateState();

    }
}
