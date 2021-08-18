package com.qcadoo.mes.costNormsForProduct.hooks;

import java.math.BigDecimal;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.util.CurrencyService;
import com.qcadoo.mes.costNormsForProduct.constants.ProductFieldsCNFP;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class ProductHooksCNFP {

    @Autowired
    private CurrencyService currencyService;

    public void onSave(final DataDefinition productDD, final Entity product) {
        BigDecimal costForNumber = product.getDecimalField(ProductFieldsCNFP.COST_FOR_NUMBER);

        if (Objects.isNull(costForNumber)) {
            product.setField(ProductFieldsCNFP.COST_FOR_NUMBER, BigDecimal.ONE);
        }
    }

    public void onCreate(final DataDefinition productDD, final Entity product) {
        Entity currentCurrency = currencyService.getCurrentCurrency();
        if (product.getBelongsToField(ProductFieldsCNFP.NOMINAL_COST_CURRENCY) == null) {
            product.setField(ProductFieldsCNFP.NOMINAL_COST_CURRENCY, currentCurrency);
        }
        if (product.getBelongsToField(ProductFieldsCNFP.LAST_PURCHASE_COST_CURRENCY) == null) {
            product.setField(ProductFieldsCNFP.LAST_PURCHASE_COST_CURRENCY, currentCurrency);
        }
        if (product.getBelongsToField(ProductFieldsCNFP.AVERAGE_COST_CURRENCY) == null) {
            product.setField(ProductFieldsCNFP.AVERAGE_COST_CURRENCY, currentCurrency);
        }
    }
}
