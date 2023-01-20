package com.qcadoo.mes.advancedGenealogy.hooks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.advancedGenealogy.constants.ParameterFieldsAG;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.NumberPatternFields;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class ProductHooksAG {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private ParameterService parameterService;

    public void setUsedInForNumberPattern(final DataDefinition productDD, final Entity product) {
        Entity numberPattern = product.getBelongsToField(ProductFields.BATCH_NUMBER_PATTERN);
        DataDefinition numberPatternDD = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER,
                BasicConstants.MODEL_NUMBER_PATTERN);
        String usedInValue = translationService.translate("basic.parameter.numberPattern.usedIn.value",
                LocaleContextHolder.getLocale());

        Entity dbNumberPattern = null;
        boolean usedInOtherPlaces = false;
        if (product.getId() != null) {
            dbNumberPattern = productDD.get(product.getId()).getBelongsToField(ProductFields.BATCH_NUMBER_PATTERN);
            if (dbNumberPattern != null) {
                int countProductsWithNumberPattern = productDD.find().add(SearchRestrictions.belongsTo(ProductFields.BATCH_NUMBER_PATTERN, dbNumberPattern))
                        .add(SearchRestrictions.idNe(product.getId())).list().getTotalNumberOfEntities();
                Entity parameterNumberPattern = parameterService.getParameter().getBelongsToField(ParameterFieldsAG.NUMBER_PATTERN);
                if (countProductsWithNumberPattern > 0 || parameterNumberPattern != null && parameterNumberPattern.getId().equals(dbNumberPattern.getId())) {
                    usedInOtherPlaces = true;
                }
            }
        }
        if (numberPattern != null) {
            numberPattern.setField(NumberPatternFields.USED, true);
            if (dbNumberPattern != null && !dbNumberPattern.getId().equals(numberPattern.getId())) {
                numberPattern.setField(NumberPatternFields.USED_IN, usedInValue);
                numberPatternDD.save(numberPattern);
                if (!usedInOtherPlaces) {
                    dbNumberPattern.setField(NumberPatternFields.USED_IN, null);
                    numberPatternDD.save(dbNumberPattern);
                }
            } else if (dbNumberPattern == null) {
                numberPattern.setField(NumberPatternFields.USED_IN, usedInValue);
                numberPatternDD.save(numberPattern);
            }
        } else if (dbNumberPattern != null && !usedInOtherPlaces) {
            dbNumberPattern.setField(NumberPatternFields.USED_IN, null);
            numberPatternDD.save(dbNumberPattern);
        }
    }

}
