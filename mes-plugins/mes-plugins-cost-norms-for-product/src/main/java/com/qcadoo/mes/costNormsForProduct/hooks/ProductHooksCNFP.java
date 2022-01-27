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

        if (Objects.isNull(product.getId())) {
            if (product.getDecimalField(ProductFieldsCNFP.AVERAGE_COST) == null) {
                product.setField(ProductFieldsCNFP.AVERAGE_COST, BigDecimal.ZERO);
            }

            if (product.getDecimalField(ProductFieldsCNFP.AVERAGE_OFFER_COST) == null) {
                product.setField(ProductFieldsCNFP.AVERAGE_OFFER_COST, BigDecimal.ZERO);
            }

            if (product.getDecimalField(ProductFieldsCNFP.NOMINAL_COST) == null) {
                product.setField(ProductFieldsCNFP.NOMINAL_COST, BigDecimal.ZERO);
            }

            if (product.getDecimalField(ProductFieldsCNFP.LAST_OFFER_COST) == null) {
                product.setField(ProductFieldsCNFP.LAST_OFFER_COST, BigDecimal.ZERO);
            }

            if (product.getDecimalField(ProductFieldsCNFP.LAST_PURCHASE_COST) == null) {
                product.setField(ProductFieldsCNFP.LAST_PURCHASE_COST, BigDecimal.ZERO);
            }
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

    public boolean validatesWith(final DataDefinition productDD, final Entity product) {
        if (product.getBelongsToField(ProductFieldsCNFP.NOMINAL_COST_CURRENCY) == null) {
            product.addError(productDD.getField(ProductFieldsCNFP.NOMINAL_COST_CURRENCY),
                    "qcadooView.validate.field.error.missing");
            return false;
        }
        return true;
    }

}
