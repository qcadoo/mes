package com.qcadoo.mes.costNormsForProduct.hooks;

import com.qcadoo.mes.costNormsForProduct.constants.ProductFieldsCNFP;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

import java.math.BigDecimal;
import java.util.Objects;

import org.springframework.stereotype.Service;

@Service
public class ProductHooksCNFP {

    public void onSave(final DataDefinition productDD, final Entity product) {
        BigDecimal costForNumber = product.getDecimalField(ProductFieldsCNFP.COST_FOR_NUMBER);

        if (Objects.isNull(costForNumber)) {
            product.setField(ProductFieldsCNFP.COST_FOR_NUMBER, BigDecimal.ONE);
        }
    }
}
