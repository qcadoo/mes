package com.qcadoo.mes.workPlans.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.workPlans.constants.WorkPlansConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;

@Service
public class ValidatorServiceImpl implements ValidatorService {

    public boolean checkIfAttachmentExtensionIsValid(final DataDefinition modelDD, final Entity model, final String attachmentName) {

        String attachment = model.getStringField(attachmentName);

        if (attachment != null) {
            String attachemntExtension = attachment.substring((attachment.lastIndexOf(".") + 1), attachment.length());

            boolean contains = false;

            for (String fileExtension : WorkPlansConstants.FILE_EXTENSIONS) {
                if (fileExtension.equals(attachemntExtension)) {
                    contains = true;
                }
            }

            if (!contains) {
                model.addError(modelDD.getField(attachmentName),
                        "workPlans.imageUrlInWorkPlan.message.attachmentExtensionIsNotValid");

                return false;
            }
        }

        return true;
    }

    public boolean checkIfColumnForProductsIsNotUsed(final DataDefinition componentDD, final Entity component,
            final String modelName, final String columnForProductsName, final String componentName) {

        if (component.getId() == null) {
            Entity columnForProducts = component.getBelongsToField(columnForProductsName);

            if (columnForProducts == null) {
                return true;
            } else {
                Entity model = component.getBelongsToField(modelName);

                if (model == null) {
                    return true;
                } else {
                    EntityList modelComponents = model.getHasManyField(componentName);

                    if (modelComponents == null) {
                        return true;
                    } else {
                        for (Entity modelComponent : modelComponents) {
                            Entity column = modelComponent.getBelongsToField(columnForProductsName);

                            if (column.getId().equals(columnForProducts.getId())) {
                                component.addError(componentDD.getField(columnForProductsName),
                                        "workPlans.columnForProducts.message.columnForProductsIsAlreadyUsed");

                                return false;
                            }
                        }
                    }
                }
            }
        }

        return true;
    }

}
