package com.qcadoo.mes.cmmsMachineParts.hooks;

import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class ToolDetailsHooks {

    public final void onBeforeRender(final ViewDefinitionState view) {
        setToolIdForMultiUploadField(view);
    }
    
    private void setToolIdForMultiUploadField(final ViewDefinitionState view) {
        FormComponent toolForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        FieldComponent toolIdForMultiUpload = (FieldComponent) view
                .getComponentByReference("toolIdForMultiUpload");
        FieldComponent toolMultiUploadLocale = (FieldComponent) view
                .getComponentByReference("toolMultiUploadLocale");

        Long toolId = toolForm.getEntityId();

        if (Objects.isNull(toolId)) {
            toolIdForMultiUpload.setFieldValue("");
        } else {
            toolIdForMultiUpload.setFieldValue(toolId);
        }

        toolIdForMultiUpload.requestComponentUpdateState();
        toolMultiUploadLocale.setFieldValue(LocaleContextHolder.getLocale());
        toolMultiUploadLocale.requestComponentUpdateState();
    }
}