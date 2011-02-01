package com.qcadoo.mes.genealogies.print.util;

import java.util.Comparator;

import com.qcadoo.mes.api.Entity;

public class EntityOperationNumberComparator implements Comparator<Entity> {

    @Override
    public int compare(Entity o1, Entity o2) {
        return ((Entity) o1.getField("operation")).getField("number").toString()
                .compareTo(((Entity) o2.getField("operation")).getField("number").toString());
    }

}
