package com.qcadoo.mes.inventory.print.utils;

import java.io.Serializable;
import java.util.Comparator;

import com.qcadoo.model.api.Entity;

public class EntityInventoryComparator implements Comparator<Entity>, Serializable {

    private static final long serialVersionUID = 5633609537665913027L;

    @Override
    public final int compare(final Entity o1, final Entity o2) {
        int result = o1.getBelongsToField("product").getStringField("numer")
                .compareTo(o2.getBelongsToField("product").getStringField("number"));
        if (result == 0) {
            result = o1.getBelongsToField("product").getStringField("name")
                    .compareTo(o2.getBelongsToField("product").getStringField("name"));
            if (result == 0) {
                result = o1.getStringField("quantity").compareTo(o2.getStringField("quantity"));
                if (result == 0) {
                    return o1.getBelongsToField("product").getStringField("unit")
                            .compareTo(o2.getBelongsToField("product").getStringField("unit"));
                } else
                    return result;
            } else
                return result;
        }
        return result;
    }
}
