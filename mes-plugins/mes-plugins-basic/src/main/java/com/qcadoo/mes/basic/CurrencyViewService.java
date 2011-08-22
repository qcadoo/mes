package com.qcadoo.mes.basic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.util.CurrencyService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class CurrencyViewService {
    
    @Autowired
    private DataDefinitionService dataDefinitionService;
    
    @Autowired
    private CurrencyService currencyService;

    private static final Logger LOG = LoggerFactory.getLogger(CurrencyService.class);
    
    public void activateSelectedCurrency(final ViewDefinitionState viewDefinitionState, final ComponentState componentState, final String[] args) {
        ComponentState lookup = viewDefinitionState.getComponentByReference("currency");
        Long currencyId = (Long) lookup.getFieldValue();
        
        if(currencyId == null) {
            return;
        }
        
        DataDefinition dd = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_CURRENCY);
        
        Entity oldCurrency = currencyService.getCurrentCurrency();
        Entity newCurrency = dd.get(currencyId);
        
        if(LOG.isDebugEnabled()) {
            LOG.debug("new currency = " + newCurrency);
        }
        
        newCurrency.setField("isActive", true);
        oldCurrency.setField("isActive", false);

        dd.save(newCurrency);
        dd.save(oldCurrency);
    }
    
    public void applyCurrentCurrency(final ViewDefinitionState viewDefinitionState) {
        FieldComponent lookup = (FieldComponent) viewDefinitionState.getComponentByReference("currency");
        lookup.setFieldValue(currencyService.getCurrentCurrency().getId());
    }
}
