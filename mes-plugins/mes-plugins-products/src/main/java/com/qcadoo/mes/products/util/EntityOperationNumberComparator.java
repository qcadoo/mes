package com.qcadoo.mes.products.util;

import java.io.Serializable;
import java.util.Comparator;

import com.qcadoo.mes.api.Entity;

public class EntityOperationNumberComparator implements Comparator<Entity>, Serializable {

    private static final long serialVersionUID = -3569221006218524772L;

    @Override
    public int compare(final Entity o1, final Entity o2) {
        return ((Entity) o1.getField("operation")).getField("number").toString()
                .compareTo(((Entity) o2.getField("operation")).getField("number").toString());
    }

}
