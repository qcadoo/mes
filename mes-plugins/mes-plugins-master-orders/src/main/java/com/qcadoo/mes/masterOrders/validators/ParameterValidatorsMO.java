package com.qcadoo.mes.masterOrders.validators;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.masterOrders.constants.MasterOrdersConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;

@Service
public class ParameterValidatorsMO {

    public boolean validatesWith(final DataDefinition parameterDD, final FieldDefinition attachmentFieldDef,
            final Entity parameter, final Object oldValue, final Object newValue) {
        return checkAttachmentExtension(parameterDD, attachmentFieldDef, parameter, oldValue, newValue);
    }

    private boolean checkAttachmentExtension(final DataDefinition parameterDD, final FieldDefinition attachmentFieldDef,
            final Entity parameter, final Object oldValue, final Object newValue) {
        if (StringUtils.equals((String) oldValue, (String) newValue) || checkAttachmentExtension((String) newValue)) {
            return true;
        }
        parameter.addError(attachmentFieldDef, "basic.parameter.additionalImage.message.attachmentExtensionIsNotValid");
        return false;
    }

    private boolean checkAttachmentExtension(final String attachementPathValue) {
        return StringUtils.isBlank(attachementPathValue)
                || StringUtils.endsWithAny(attachementPathValue, MasterOrdersConstants.FILE_EXTENSIONS);

    }
}
