package com.qcadoo.mes.deliveries.hooks;

import java.util.List;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class CompanyHooksB {

    public boolean checkIfProductIsNotFamily(final DataDefinition productDD, final Entity product) {

        List<Entity> productsFamily = product.getHasManyField("productsFamily");

        for (Entity productFamily : productsFamily) {
            if (!productFamily.getStringField("entityType").equals("02productsFamily")) {
                product.addGlobalError("basic.companies.message.productIsNotFamily", productFamily.getStringField("number"));
            }
        }
        return true;

    }

    public boolean checkIfProductIsFamily(final DataDefinition productDD, final Entity entity) {

        List<Entity> products = entity.getHasManyField("products");

        for (Entity product : products) {
            if (product.getStringField("entityType").equals("02productsFamily")) {
                entity.addGlobalError("basic.companies.message.productIsFamily", product.getStringField("number"));
            }
        }
        return true;

    }
}
