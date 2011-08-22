package com.qcadoo.mes.basic.util;

import java.util.Currency;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class CurrencyService {

    @Autowired
    DataDefinitionService dataDefinitionService;
    
    private static final Logger LOG = LoggerFactory.getLogger(CurrencyService.class);
    
    public Entity getCurrentCurrency() {
        DataDefinition dd = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_CURRENCY);
        Entity currency = dd.find().add(SearchRestrictions.eq("isActive", true)).uniqueResult();
        
        if(LOG.isDebugEnabled()) {
            LOG.debug("actual currency = " + currency);
        }
        
        if(currency != null) {
            return currency;
        }
        
        String alphabeticCode = Currency.getInstance(Locale.getDefault()).getCurrencyCode();
        currency = dd.find().add(SearchRestrictions.eq("alphabeticCode", alphabeticCode)).uniqueResult();
        currency.setField("isActive", true);
        return dd.save(currency);
    }

}
