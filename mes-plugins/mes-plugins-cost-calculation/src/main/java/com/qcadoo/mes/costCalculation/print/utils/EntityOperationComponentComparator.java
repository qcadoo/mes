package com.qcadoo.mes.costCalculation.print.utils;

import java.io.Serializable;
import java.util.Comparator;

import com.qcadoo.model.api.Entity;

public class EntityOperationComponentComparator implements Comparator<Entity>, Serializable {

    private static final long serialVersionUID = 2360961924344935922L;

    @Override
    public final int compare(final Entity o1, final Entity o2) {
        int result = o1.getBelongsToField("operation").getStringField("number")
                .compareTo(o2.getBelongsToField("operation").getStringField("number"));
        if (result == 0) {
            result = o1.getBelongsToField("operation").getStringField("name")
                    .compareTo(o2.getBelongsToField("operation").getStringField("name"));
            if (result == 0) {
                return result = o1.getBelongsToField("operation").getId().toString()
                        .compareTo(o2.getBelongsToField("operation").getId().toString());
            } else
                return result;
        }
        return result;
    }

}
