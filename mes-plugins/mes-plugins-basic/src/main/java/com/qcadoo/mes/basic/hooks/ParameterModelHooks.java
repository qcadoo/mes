package com.qcadoo.mes.basic.hooks;

import java.util.Currency;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.types.BelongsToType;

@Service
public class ParameterModelHooks {

    private static final String FIELD_CURRENCY = "currency";

    public void setDefaultCurrency(final DataDefinition dataDefinition, final Entity parameter) {
        String defaultCurrencyAlphabeticCode = Currency.getInstance(Locale.getDefault()).getCurrencyCode();
        DataDefinition currencyDataDef = ((BelongsToType) dataDefinition.getField(FIELD_CURRENCY).getType()).getDataDefinition();
        Entity defaultCurrency = currencyDataDef.find()
                .add(SearchRestrictions.eq("alphabeticCode", defaultCurrencyAlphabeticCode)).setMaxResults(1).uniqueResult();
        parameter.setField(FIELD_CURRENCY, defaultCurrency);
    }

}
