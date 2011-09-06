package com.qcadoo.mes.costNormsForOperation.constants;

import java.util.Set;

import com.google.common.collect.Sets;

public interface CostNormsForOperationConstants {

    public static final String PLUGIN_IDENTIFIER = "costNormsForOperation";

    public static final String MODEL_CALCULATION_OPERATION_COMPONENT = "calculationOperationComponent";
    
    public static final Set<String> FIELDS = Sets.newHashSet("pieceworkCost", "numberOfOperations", "laborHourlyCost",
            "machineHourlyCost");
}
