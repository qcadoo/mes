package com.qcadoo.mes.technologies.validators;

import com.qcadoo.mes.technologies.constants.ModifyTechnologyHelperFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

import java.util.Objects;

import org.springframework.stereotype.Service;

@Service
public class ModifyTechnologyHelperValidators {

    public static final String L_REPLACE = "replace";

    public static final String L_REPLACE_PRODUCT_QUANTITY = "replaceProductQuantity";

    public static final String L_REPLACE_PRODUCT = "replaceProduct";

    public boolean validatesWith(final DataDefinition modifyTechnologyHelperDD, final Entity modifyTechnologyHelper) {

        boolean valid = true;
        
        if(modifyTechnologyHelper.getBooleanField(L_REPLACE)) {
            if(Objects.isNull(modifyTechnologyHelper.getBelongsToField(L_REPLACE_PRODUCT))) {
                modifyTechnologyHelper.addError(modifyTechnologyHelperDD.getField(L_REPLACE_PRODUCT),"qcadooView.validate.field.error.missing");
                valid = false;
            }
            if (Objects.isNull(modifyTechnologyHelper.getField(L_REPLACE_PRODUCT_QUANTITY))) {
                modifyTechnologyHelper.addError(modifyTechnologyHelperDD.getField(L_REPLACE_PRODUCT_QUANTITY),"qcadooView.validate.field.error.missing");
                valid = false;
            }
        }

        if(modifyTechnologyHelper.getBooleanField(ModifyTechnologyHelperFields.ADD_NEW)) {
            if(modifyTechnologyHelper.getHasManyField(ModifyTechnologyHelperFields.MODIFY_TECHNOLOGY_ADD_PRODUCTS).isEmpty()) {
                modifyTechnologyHelper.addGlobalError("technologies.modifyTechnology.error.addedListProductsEmpty");
                valid = false;
            }

        }

        return valid;
    }
}
