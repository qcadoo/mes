package com.qcadoo.mes.genealogies.print.util;

import java.io.Serializable;
import java.util.Comparator;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.utils.Pair;

@SuppressWarnings("serial")
public class BatchOrderNrComparator implements Comparator<Pair<String, Entity>>, Serializable {

    @Override
    public int compare(final Pair<String, Entity> o1, final Pair<String, Entity> o2) {
        return o1.getValue().getField("number").toString().compareTo(o2.getValue().getField("number").toString());
    }

}
