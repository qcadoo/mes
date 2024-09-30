package com.qcadoo.mes.costNormsForProduct;

import com.qcadoo.mes.basic.constants.CurrencyFields;
import com.qcadoo.mes.basic.util.CurrencyService;
import com.qcadoo.mes.costNormsForProduct.constants.ProductFieldsCNFP;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;

@Service
public class CostNormsForProductService {

    @Autowired
    private CurrencyService currencyService;

    public BigDecimal getNominalCost(final Entity product) {
        BigDecimal nominalCost = BigDecimalUtils.convertNullToZero(product.getDecimalField(ProductFieldsCNFP.NOMINAL_COST));
        Entity currency = product.getBelongsToField(ProductFieldsCNFP.NOMINAL_COST_CURRENCY);

        if (Objects.nonNull(currency) && CurrencyService.PLN.equals(currencyService.getCurrencyAlphabeticCode())
                && !CurrencyService.PLN.equals(currency.getStringField(CurrencyFields.ALPHABETIC_CODE))) {
            nominalCost = currencyService.getConvertedValue(nominalCost, currency);
        }

        return nominalCost;
    }
}
