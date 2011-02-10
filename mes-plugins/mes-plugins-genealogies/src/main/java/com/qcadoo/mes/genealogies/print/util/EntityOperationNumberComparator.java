package com.qcadoo.mes.genealogies.print.util;

import java.io.Serializable;
import java.util.Comparator;

import com.qcadoo.mes.api.Entity;

public class EntityOperationNumberComparator implements Comparator<Entity>, Serializable {

    private static final long serialVersionUID = -5494591766317495429L;

    @Override
    public int compare(final Entity o1, final Entity o2) {
        return ((Entity) o1.getField("operation")).getField("number").toString()
                .compareTo(((Entity) o2.getField("operation")).getField("number").toString());
    }

}
