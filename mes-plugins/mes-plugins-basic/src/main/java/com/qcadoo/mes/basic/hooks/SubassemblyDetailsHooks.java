package com.qcadoo.mes.basic.hooks;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class SubassemblyDetailsHooks {

    private static final String L_FORM = "form";

    public void setSubassemblyIdForMultiUploadField(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        FieldComponent subassemblyIdForMultiUpload = (FieldComponent) view.getComponentByReference("subassemblyIdForMultiUpload");
        FieldComponent subassemblyMultiUploadLocale = (FieldComponent) view
                .getComponentByReference("subassemblyMultiUploadLocale");

        if (form.getEntityId() != null) {
            subassemblyIdForMultiUpload.setFieldValue(form.getEntityId());
            subassemblyIdForMultiUpload.requestComponentUpdateState();
        } else {
            subassemblyIdForMultiUpload.setFieldValue("");
            subassemblyIdForMultiUpload.requestComponentUpdateState();
        }
        subassemblyMultiUploadLocale.setFieldValue(LocaleContextHolder.getLocale());
        subassemblyMultiUploadLocale.requestComponentUpdateState();

    }
}
