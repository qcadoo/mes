package com.qcadoo.mes.advancedGenealogy.criteriaModifier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.NumberPatternFields;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class NumberPatternCriteriaModifiersAG {

	@Autowired
	private TranslationService translationService;

	public void restrictNumberPatternForUnused(final SearchCriteriaBuilder searchCriteriaBuilder) {
		searchCriteriaBuilder.add(SearchRestrictions.or(SearchRestrictions.isNull(NumberPatternFields.USED_IN),
				SearchRestrictions.eq(NumberPatternFields.USED_IN, translationService
						.translate("basic.parameter.numberPattern.usedIn.value", LocaleContextHolder.getLocale()))));
	}

	public void restrictNumberPatternForUnusedDelivery(final SearchCriteriaBuilder searchCriteriaBuilder) {
		searchCriteriaBuilder.add(SearchRestrictions.or(SearchRestrictions.isNull(NumberPatternFields.USED_IN),
				SearchRestrictions.eq(NumberPatternFields.USED_IN, translationService
						.translate("basic.parameter.numberPattern.usedInDeliveryProductBatch.value", LocaleContextHolder.getLocale()))));
	}
}
