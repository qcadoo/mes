package com.qcadoo.mes.products.util;

import java.io.Serializable;
import java.util.Comparator;

import com.qcadoo.mes.api.Entity;

@SuppressWarnings("serial")
public class EntityOperationNumberComparator implements Comparator<Entity>, Serializable {

    @Override
    public int compare(final Entity o1, final Entity o2) {
        return ((Entity) o1.getField("operation")).getField("number").toString()
                .compareTo(((Entity) o2.getField("operation")).getField("number").toString());
    }

}
