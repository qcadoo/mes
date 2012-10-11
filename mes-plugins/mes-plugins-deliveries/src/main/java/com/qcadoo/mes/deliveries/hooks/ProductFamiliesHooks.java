package com.qcadoo.mes.deliveries.hooks;

import java.util.List;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class ProductFamiliesHooks {

    public boolean checkIfProductIsFamily(final DataDefinition productDD, final Entity product) {

        List<Entity> productsFamily = product.getHasManyField("productsFamily");

        if (productsFamily != null) {
            for (Entity productFamily : productsFamily) {
                if (!productFamily.getStringField("entityType").equals("02productsFamily")) {
                    product.addGlobalError("basic.companies.message.productIsNotFamily", productFamily.getStringField("number"));
                }
            }
            return false;
        }
        return true;

    }

}
