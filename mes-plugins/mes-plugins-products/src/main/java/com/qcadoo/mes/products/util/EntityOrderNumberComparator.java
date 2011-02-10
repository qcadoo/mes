package com.qcadoo.mes.products.util;

import java.io.Serializable;
import java.util.Comparator;

import com.qcadoo.mes.api.Entity;

public class EntityOrderNumberComparator implements Comparator<Entity>, Serializable {

    private static final long serialVersionUID = 3235899660901016113L;

    @Override
    public int compare(final Entity o1, final Entity o2) {
        return ((Entity) o1.getField("order")).getField("number").toString()
                .compareTo(((Entity) o2.getField("order")).getField("number").toString());
    }

}
