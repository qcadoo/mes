package com.qcadoo.mes.genealogies.print.util;

import java.util.Comparator;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.utils.Pair;

public class BatchOrderNrComparator implements Comparator<Pair<String, Entity>> {

    @Override
    public int compare(Pair<String, Entity> o1, Pair<String, Entity> o2) {
        return o1.getValue().getField("number").toString().compareTo(o2.getValue().getField("number").toString());
    }

}
