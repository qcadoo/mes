package com.qcadoo.mes.basic.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.AdditionalCodeFields;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class AdditionalCodeValidators {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public boolean validateCodeUniqueness(final DataDefinition additionalCodeDD, final Entity additionalCode) {
        Entity duplicateCode = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT).find()
                .add(SearchRestrictions.eq(ProductFields.NUMBER, additionalCode.getStringField(AdditionalCodeFields.CODE)))
                .setMaxResults(1).uniqueResult();

        if (duplicateCode != null) {
            additionalCode.addError(additionalCodeDD.getField(AdditionalCodeFields.CODE),
                    "qcadooView.validate.field.error.duplicated");
            return false;
        }
        return true;
    }
}
