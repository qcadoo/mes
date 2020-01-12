package com.qcadoo.mes.advancedGenealogyForOrders.criteriaModifiers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.NumberPatternFields;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class NumberPatternCriteriaModifiers {

    @Autowired
    private TranslationService translationService;

    public void restrictNumberPatternForUnused(final SearchCriteriaBuilder searchCriteriaBuilder) {
        searchCriteriaBuilder.add(SearchRestrictions.or(SearchRestrictions.isNull(NumberPatternFields.USED_IN),
                SearchRestrictions.eq(NumberPatternFields.USED_IN, translationService
                        .translate("basic.parameter.numberPattern.usedIn.value", LocaleContextHolder.getLocale()))));
    }
}
