package com.qcadoo.mes.genealogies.print.util;

import java.util.Comparator;

import com.qcadoo.mes.api.Entity;

public class EntityNumberComparator implements Comparator<Entity> {

    @Override
    public int compare(Entity o1, Entity o2) {
        return o1.getField("number").toString().compareTo(o2.getField("number").toString());
    }

}
