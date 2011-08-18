package com.qcadoo.mes.technologies.print.utils;

import java.io.Serializable;
import java.util.Comparator;

import com.qcadoo.model.api.Entity;

public class EntityOperationProductInOutComponentComparator implements Comparator<Entity>, Serializable {

    private static final long serialVersionUID = 1371136330724619528L;

    @Override
    public final int compare(final Entity o1, final Entity o2) {
        int result = o1.getBelongsToField("operationComponent").getBelongsToField("operation").getId().toString()
                .compareTo(o2.getBelongsToField("operationComponent").getBelongsToField("operation").getId().toString());
        if (result == 0) {
            result = o1.getDataDefinition().getName().toString().compareTo(o2.getDataDefinition().getName().toString());
            if (result == 0) {
                result = o1.getBelongsToField("product").getStringField("name")
                        .compareTo(o2.getBelongsToField("product").getStringField("name"));
                if (result == 0) {
                    result = o1.getField("quantity").toString().compareTo(o2.getField("quantity").toString());
                    if (result == 0) {
                        return o1.getBelongsToField("product").getStringField("unit")
                                .compareTo(o2.getBelongsToField("product").getStringField("unit"));
                    } else
                        return result;
                } else
                    return result;
            } else
                return result;
        }
        return result;
    }
}
