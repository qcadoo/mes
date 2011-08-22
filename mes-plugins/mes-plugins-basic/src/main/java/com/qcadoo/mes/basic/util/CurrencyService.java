package com.qcadoo.mes.basic.util;

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
    
    public Entity getCurrentCurrency() {
        DataDefinition dd = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_CURRENCY);
        return dd.find().add(SearchRestrictions.eq("isActive", true)).uniqueResult();
    }
}
