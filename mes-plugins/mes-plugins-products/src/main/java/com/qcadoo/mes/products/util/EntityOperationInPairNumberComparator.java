package com.qcadoo.mes.products.util;

import java.util.Comparator;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.utils.Pair;

public class EntityOperationInPairNumberComparator implements Comparator<Pair<Entity, Entity>> {

    @Override
    public int compare(final Pair<Entity, Entity> o1, final Pair<Entity, Entity> o2) {
        return ((Entity) o1.getKey().getField("operation")).getField("number").toString()
                .compareTo(((Entity) o2.getKey().getField("operation")).getField("number").toString());
    }

}
