package com.qcadoo.mes.workPlans.workPlanColumnExtension;

import java.util.HashMap;
import java.util.Map;

import com.qcadoo.model.api.Entity;

public class WorkPlanColumnFiller {

    private final static String PRODUCT_COLUMN = "product";

    private final static String QUANTITY_COLUMN = "quantity";

    public Map<String, String> getValues(Entity productComponent) {
        Map<String, String> values = new HashMap<String, String>();

        return values;
    }

    private String addProductValue(Entity productComponent) {
        Entity product = productComponent.getBelongsToField("product");

        StringBuilder productString = new StringBuilder(product.getStringField("name"));
        productString.append(" (");
        productString.append(product.getStringField("number"));
        productString.append(")");

        return productString.toString();
    }

}
