package com.qcadoo.mes.workPlans.hooks;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public interface ValidatorService {

    boolean checkIfAttachmentExtensionIsValid(final DataDefinition modelDD, final Entity model, final String attachmentName);

    boolean checkIfColumnForProductsIsNotUsed(final DataDefinition componentDD, final Entity component, final String modelName,
            final String columnForProductsName, final String componentName);

}
