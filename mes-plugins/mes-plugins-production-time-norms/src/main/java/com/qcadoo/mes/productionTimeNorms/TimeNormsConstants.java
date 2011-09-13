package com.qcadoo.mes.productionTimeNorms;

import java.util.Set;

import com.google.common.collect.Sets;

public interface TimeNormsConstants {

    public static final Set<String> FIELDS_OPERATION = Sets.newHashSet("tpz", "tj", "productionInOneCycle", "countRealized",
            "countMachine", "timeNextOperation");
    
    public static final Set<String> FIELDS_TECHNOLOGY = Sets.newHashSet("tpz", "tj", "productionInOneCycle", "countRealized",
            "countMachine", "timeNextOperation");
}
